package com.funguscow.musie.structure;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * A leaf-level bar of notes
 * 
 * @author alpac
 *
 */
public class Bar extends Module {

	private List<Note> measures[];
	private int chord;

	/**
	 * Unfortunately this must be suppressed because apparently generics don't like
	 * arrays :(
	 * 
	 * @param motif
	 * @param gen
	 */
	@SuppressWarnings("unchecked")
	public Bar(Motif motif, Attractor[] gen, int chord) {
		super(-1, motif, gen);
		this.chord = chord;
		measures = new List[gen.length];
		for (int i = 0; i < measures.length; i++) {
			measures[i] = new ArrayList<Note>();
			do {
				Note next = new Note(gen[i].nadicFraction(motif.getTimeSig().numerator, 0.5), this.chord, gen[i]);
				measures[i].add(next);
			} while (gen[i].nextInt(5) != 0);
			measures[i].sort(Note::compareTo);
		}
	}

	@SuppressWarnings("unchecked")
	public Bar(Bar base) {
		super(-1, base.motif, base.gen);
		this.chord = base.chord;
		this.measures = new List[gen.length];
		for (int i = 0; i < measures.length; i++) {
			measures[i] = new ArrayList<Note>(base.measures[i]);
		}
	}

	@Override
	public Module clone() {
		return new Bar(this);
	}

	@Override
	public void mutate() {
		List<Note> temp = new ArrayList<Note>();
		for (int i = 0; i < measures.length; i++) {
			List<Note> measure = measures[i];
			temp.clear();
			for (Note note : measure) {
				if (gen[i].nextInt(2) == 0)
					temp.add(note);
			}
			while (gen[i].nextInt(3) != 0) {
				Note next = new Note(gen[i].nadicFraction(motif.getTimeSig().numerator, 0.5), this.chord, gen[i]);
				temp.add(next);
			}
			measure.clear();
			measure.addAll(temp);
			measure.sort(Note::compareTo);
		}
	}

	public int getMaxNotes() {
		Comparator<Note> ncomp = (a, b) -> a.getNote() - b.getNote();
		Set<Note> activeNotes = new TreeSet<Note>(ncomp);
		Set<Note> deadNotes = new TreeSet<Note>(ncomp);
		List<Note> allNotes = new ArrayList<Note>();
		for (List<Note> measure : measures)
			allNotes.addAll(measure);
		allNotes.sort(Note::compareTo);
		int active = 0;
		for (Note note : allNotes) {
			double time = note.getStart();
			for (Note playing : activeNotes) {
				if (playing.getStart() + playing.getLength() <= time)
					deadNotes.add(playing);
			}
			for (Note dead : deadNotes) {
				activeNotes.remove(dead);
			}
			if (!activeNotes.contains(note)) {
				activeNotes.add(note);
				int nowActive = activeNotes.size();
				if (nowActive > active)
					active = nowActive;
			}
			deadNotes.clear();
		}
		return active;
	}

	public void render(List<NoteMessage> msgs, int offset) {
		Fraction start = new Fraction(offset, 1);
		Comparator<Note> ncomp = (a, b) -> a.getNote() - b.getNote();
		Set<Note> activeNotes = new TreeSet<Note>(ncomp);
		Set<Note> deadNotes = new TreeSet<Note>(ncomp);
		for (int i = 0; i < measures.length; i++) {
			List<Note> measure = measures[i];
			for (Note note : measure) {
				double time = note.getStart();
				for (Note playing : activeNotes) {
					if (playing.getStart() + playing.getLength() <= time) {
						deadNotes.add(playing);
						NoteMessage off = new NoteMessage(i, playing.getNote(), false,
								playing.getStart() + playing.getLength() + start.asReal(), 0);
						msgs.add(off);
					}
				}
				for (Note dead : deadNotes) {
					activeNotes.remove(dead);
				}
				if (!activeNotes.contains(note)) {
					activeNotes.add(note);
					NoteMessage on = new NoteMessage(i, note.getNote(), true, note.getStart() + start.asReal(),
							note.getLength());
					msgs.add(on);
				}
				deadNotes.clear();
			}
			for (Note playing : activeNotes) {
				NoteMessage off = new NoteMessage(i, playing.getNote(), false,
						playing.getStart() + playing.getLength()+ start.asReal(), 0);
				msgs.add(off);
			}
			activeNotes.clear();
		}
	}

}
