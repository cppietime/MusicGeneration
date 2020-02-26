package com.funguscow.musie.structure;

import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

/**
 * A class representing the note messages in this project's representation (i.e.
 * not quite MIDI)
 * 
 * @author alpac
 *
 */
public class Sequential {

	private static final int DEFAULT_PPQ = 128 * 3;
	private static final double DEFAULT_BPM = 120;

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
			double qtrs = msg.time.asReal() * timeSig.asReal() * 2;
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
					Fraction start = new Fraction(time, denom);
					NoteMessage nmsg = new NoteMessage(channel, note, on, start);
					ret.msgs.add(nmsg);
				}
				/* Change program */
				else if((msg.getStatus() & 0xf0) == 0b11000000) {
					ShortMessage smsg = (ShortMessage)msg;
					programs[smsg.getChannel()] = smsg.getData1();
				}
			}
		}
		return ret;
	}

}
