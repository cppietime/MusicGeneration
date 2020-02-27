package com.funguscow.musie.structure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import com.funguscow.musie.filter.CascadeFilter;
import com.funguscow.musie.instrument.Instrument;
import com.funguscow.musie.random.Randomizer;

/**
 * A class representing the note messages in this project's representation (i.e.
 * not quite MIDI)
 * 
 * @author alpac
 *
 */
public class Sequential {

	private static final int DEFAULT_PPQ = 128 * 3;
	private static final double DEFAULT_BPM = 180;

	private List<NoteMessage> msgs;
	private double bpm;
	private Fraction timeSig;
	private int ppq;
	
	private Sequential() {
		msgs = new ArrayList<NoteMessage>();
		bpm = DEFAULT_BPM;
		timeSig = new Fraction(4, 4);
		ppq = DEFAULT_PPQ;
	}

	public Sequential(List<NoteMessage> msgs, double bpm, int ppq, Fraction timeSig) {
		this.msgs = msgs;
		msgs.sort(NoteMessage::compareTo);
		this.bpm = bpm;
		this.timeSig = timeSig;
		this.ppq = ppq;
	}
	
	public Sequential(List<NoteMessage> msgs, Fraction timeSig) {
		this(msgs, DEFAULT_BPM, DEFAULT_PPQ, timeSig);
	}
	
	protected void findLens() {
		Map<Integer[], NoteMessage> playing = new HashMap<Integer[], NoteMessage>();
		for(NoteMessage msg : msgs) {
			if(msg.on && msg.duration == 0) {
				playing.put(new Integer[] {msg.channel, msg.note}, msg);
			} else {
				Integer[] key = new Integer[] {msg.channel, msg.note};
				NoteMessage start = playing.get(key);
				if(start != null) {
					start.duration = msg.time - start.time;
					playing.remove(key);
				}
			}
		}
	}
	
	/**
	 * Get the total number of samples for this sequence
	 * @param sampleRate Samples per second
	 * @param padding Extra time
	 * @return
	 */
	public int samples(int sampleRate, double padding) {
		msgs.sort(NoteMessage::compareTo);
		double last = msgs.get(msgs.size() - 1).time;
		double secs = last * timeSig.asReal() * 4 * 60 / bpm;
		System.out.println(secs + " seconds long");
		return (int)((padding + secs) * sampleRate);
	}
	
	/**
	 * Render a single track/instrument to track
	 * @param track
	 * @param instrument
	 * @param sampleRate
	 * @param num
	 * @param effect
	 */
	public void renderTrack(double track[], Instrument instrument, int sampleRate, int num, CascadeFilter effect) {
		findLens();
		for(NoteMessage msg : msgs) {
			if(!msg.on || msg.channel != num || msg.duration == 0)
				continue;
			double startTime = timeSig.asReal() * msg.time * 4 * 60 / bpm;
			double duration = msg.duration * timeSig.asReal() * 4 * 60 / bpm;
			double frequency = 8.176 * Math.pow(2, msg.note / 12.0);
			instrument.playNote(track, frequency, 1.0, duration, startTime, sampleRate);
		}
		double maxval = 0;
		if(effect != null)
			effect.reset();
		for(double d : track) {
			if(Math.abs(d) > maxval)
				maxval = Math.abs(d);
		}
		if(maxval > 1) {
			maxval = 1.0 / maxval;
			for(int i = 0; i < track.length; i++) {
				if(effect != null) {
					track[i] = effect.filter(track[i]);
				}
				track[i] *= maxval;
			}
		}
	}
	
	/**
	 * Create an array of samples
	 * @param sampleRate
	 * @param pad0
	 * @param pad1
	 * @return
	 */
	public double[] render(int sampleRate, double pad0, double pad1) {
		int sampsTotal = samples(sampleRate, pad0 + pad1);
		int samps = samples(sampleRate, pad1);
		System.out.println("Requires " + (samps + sampsTotal) + " samples");
		double work[] = new double[samps];
		double sound[] = new double[sampsTotal];
		int maxInstr = 0;
		for(NoteMessage msg : msgs) {
			if(msg.channel > maxInstr)
				maxInstr = msg.channel;
		}
		System.out.println("Found " + maxInstr + " instruments");
		CascadeFilter effect = new CascadeFilter();
		for(int i = 0; i <= maxInstr; i++) {
			Arrays.fill(work, 0);
			Instrument instr = i > 0 ? Randomizer.randomInstrument() : Randomizer.randomDrums();
			effect.clear();
			Randomizer.randomEffects(effect);
			renderTrack(work, instr, sampleRate, i, effect);
			for(int j = 0; j < samps; j++) {
				sound[j + sampsTotal - samps] += work[j];
			}
			System.out.println("Rendered track " + i);
		}
		double maxval = 0;
		for(double d : sound) {
			if(Math.abs(d) > maxval)
				maxval = Math.abs(d);
		}
		if(maxval > 1) {
			maxval = 1.0 / maxval;
			for(int i = 0; i < sampsTotal; i++) {
				sound[i] *= maxval;
			}
		}
		return sound;
	}

	/**
	 * Convert this sequence into a 1-track MIDI Sequence
	 * 
	 * @return
	 * @throws InvalidMidiDataException
	 */
	public Sequence toMidi() throws InvalidMidiDataException {
		Sequence seq = new Sequence(Sequence.PPQ, ppq);
		Track track = seq.createTrack();
		double qpm = bpm * 4.0 / timeSig.denominator;
		int uspq = (int) (60.0 * 1E6 / qpm);
		MidiEvent tempoEvent = new MidiEvent(new MetaMessage(0x51,
				new byte[] { (byte) ((uspq >> 16) & 0xff), (byte) ((uspq >> 8) & 0xff), (byte) (uspq & 0xff) }, 3), 0);
		track.add(tempoEvent);
		for(int i = 0; i < 16; i++) {
			ShortMessage prog = new ShortMessage(ShortMessage.PROGRAM_CHANGE, i, i, 0);
			track.add(new MidiEvent(prog, 0));
		}
		for (NoteMessage msg : msgs) {
			double qtrs = msg.time * timeSig.asReal() * 2;
			long ticks = (long) (ppq * qtrs);
			long delta = ticks;
			ShortMessage smsg = new ShortMessage();
			int channel = msg.channel == 0 ? 0 : msg.channel - 0;
			int cmd = msg.on ? ShortMessage.NOTE_ON : ShortMessage.NOTE_OFF;
			smsg.setMessage(cmd, channel, msg.note, 64);
			track.add(new MidiEvent(smsg, delta));
		}
		return seq;
	}
	
	/**
	 * Convert a MIDI sequence to this type
	 * @param seq
	 * @return
	 */
	public static Sequential fromMidi(Sequence seq) {
		Sequential ret = new Sequential();
		for(Track track : seq.getTracks()) {
			int msgnum = track.size();
			int programs[] = new int[16];
			for(int i = 0; i < msgnum; i++) {
				MidiEvent event = track.get(i);
				long time = event.getTick();
				MidiMessage msg = event.getMessage();
				/* Meta message, check if tempo */
				if(msg.getStatus() == 0xff) {
					MetaMessage meta = (MetaMessage)msg;
					/* It IS tempo! */
					if(meta.getType() == 0x51) {
						byte[] data = meta.getData();
						int uspq = 0;
						for(byte b : data) {
							uspq <<= 8;
							uspq |= b;
						}
						double qpm = 60 * 1E6 / uspq;
						ret.bpm = qpm * ret.timeSig.denominator / 4;
					}
				}
				/* For note on/off messages */
				else if((msg.getStatus() & 0xe0) == 0x80) {
					ShortMessage smsg = (ShortMessage)msg;
					boolean on = smsg.getCommand() == ShortMessage.NOTE_ON;
					int note = smsg.getData1();
					int channel = programs[smsg.getChannel()];
					long denom = ret.timeSig.numerator * ret.ppq * 2;
					NoteMessage nmsg = new NoteMessage(channel, note, on, (double)time / denom, 0);
					ret.msgs.add(nmsg);
				}
				/* Change program */
				else if((msg.getStatus() & 0xf0) == 0b11000000) {
					ShortMessage smsg = (ShortMessage)msg;
					programs[smsg.getChannel()] = smsg.getData1();
				}
			}
		}
		ret.findLens();
		return ret;
	}

}
