package com.funguscow.musie.structure;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.sound.sampled.AudioFormat;

import com.funguscow.musie.wave.Wave;

/**
 * A bar or group of bars
 * 
 * @author alpac
 *
 */
public class Segment extends Module {

	private List<Module> children;
	private Map<Integer, Integer> counts;
	private int order[];

	/**
	 * 
	 * @param random
	 * @return A new song structure!
	 */
	public static Segment song(Random random) {
		Fraction timeSig = new Fraction(4, 4, false);
		if (random.nextBoolean()) {
			if (random.nextBoolean())
				timeSig.numerator += 1;
			timeSig.denominator <<= random.nextInt(2);
			timeSig.numerator = 2 + 2 * random.nextInt(1 + timeSig.denominator / 2);
		}
		int length = 4 + random.nextInt(4);
		int max = length + 2;
		int depth = 2;
		Motif motif = new Motif(length, max, timeSig, random);
		int channels = 2 + random.nextInt(3);
		Attractor gens[] = new Attractor[channels];
		for (int i = 0; i < gens.length; i++) {
			gens[i] = new Attractor(random);
		}
		Segment song = new Segment(depth, motif, gens);
		song.mutate();
		return song;
	}

	public Segment(int depth, Motif motif, Attractor[] gen) {
		super(depth, motif, gen);
		children = new ArrayList<Module>();
		order = new int[motif.getLength()];
		counts = new TreeMap<Integer, Integer>();
		for (int i = 0; i < motif.getLength(); i++) {
			order[i] = motif.getValue(i);
			counts.put(order[i], 1 + counts.getOrDefault(order[i], 0));
		}
		for (int i = 0; i < motif.getMaximum(); i++) {
			if (depth == 0)
				children.add(new Bar(motif, gen, motif.getChord(i)));
			else
				children.add(new Segment(depth - 1, motif, gen));
		}
	}

	public Segment(Segment base) {
		super(base.depth, base.motif, base.gen);
		children = new ArrayList<Module>(base.children);
		counts = new TreeMap<Integer, Integer>(base.counts);
		order = new int[motif.getLength()];
		System.arraycopy(base.order, 0, order, 0, order.length);
	}

	@Override
	public void mutate() {
		super.mutate();
		Fraction ptr = new Fraction(0, 1);
		while (gen[0].nextInt(4) != 0) {
			if (ptr.denominator >= order.length)
				continue;
			int index = order.length - 1 - order.length * ptr.numerator / ptr.denominator;
			int modId = order[index];
			if (counts.getOrDefault(modId, 0) > 1) {
				counts.put(modId, counts.get(modId) - 1);
				children.add(children.get(modId).clone());
				modId = children.size() - 1;
			}
			children.get(modId).mutate();
			ptr.incrementNadic(2);
		}
	}

	public int getMaxNotes() {
		int notes = 0;
		for (Module child : children) {
			int candidate = child.getMaxNotes();
			if (candidate > notes)
				notes = candidate;
		}
		return notes;
	}

	@Override
	public Module clone() {
		return new Segment(this);
	}

	public void render(List<NoteMessage> msgs, int offset) {
		offset *= motif.getLength();
		for (int i = 0; i < motif.getLength(); i++) {
			int id = order[i];
			children.get(id).render(msgs, offset + i);
		}
	}

	private static final int MAX_NOTES = 12 * 3;
	private static final int MIN_NOTE = 30;

	/**
	 * Generate a random song an save it to the specified output stream
	 * Saves in 16-bit signed LE, mono, 44.1 kHz
	 * @param stream Stream to save to as a RIFF WAVE
	 * @throws IOException
	 */
	public static void makeWave(OutputStream stream) throws IOException{
		Segment song = song(new Random());
		List<NoteMessage> msgs = new ArrayList<NoteMessage>();
		song.render(msgs, 0);
		int minNote = msgs.get(0).note;
		for (NoteMessage msg : msgs) {
			if (msg.note < minNote)
				minNote = msg.note;
		}
		for (NoteMessage msg : msgs) {
			msg.note -= minNote;
			msg.note %= MAX_NOTES;
			msg.note += MIN_NOTE;
		}
		msgs = msgs.stream().filter(n -> n.note >= MIN_NOTE && n.note <= MIN_NOTE + MAX_NOTES)
				.collect(Collectors.toList());
		msgs.sort(NoteMessage::compareTo);
		Sequential seq = new Sequential(msgs, song.motif.getTimeSig());
		double[] samps = seq.render(44100, .5, .5);
		Wave.write(stream, samps, new AudioFormat(44100, 16, 1, true, false));
	}
	
	public static void main(String[] args) {
		File out = new File("test.wav");
		try {
			OutputStream stream = new FileOutputStream(out);
			makeWave(stream);
			stream.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

}
