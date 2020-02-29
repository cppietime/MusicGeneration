package com.funguscow.musie.structure;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.sound.midi.MidiSystem;
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
		System.out.println("Time signature: " + timeSig);
		int length = 4 + random.nextInt(4);
		int max = length + 2;
		int depth = 2;
		System.out.println("Length = " + length + " Depth = " + depth + " Max = " + max);
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
		System.out.println("Geneating at depth " + depth);
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

	public static void main(String[] args) {
		Segment song = song(new Random());
		System.out.println("Maxnotes: " + song.getMaxNotes());
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
		System.out.println(msgs.size() + " note messages");
		Sequential seq = new Sequential(msgs, song.motif.getTimeSig());
		try {
			MidiSystem.write(seq.toMidi(), 0, new File("test.mid"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Starting sample rendering");
		double[] samps = seq.render(44100, .5, .5);
		System.out.println("Starting file writing");
		Wave.write(new File("test.wav"), samps, new AudioFormat(44100, 16, 1, true, false));
		System.out.println("Done");
	}

}
