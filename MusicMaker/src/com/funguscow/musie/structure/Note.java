package com.funguscow.musie.structure;

/**
 * An abstract note object
 * @author alpac
 *
 */
public class Note implements Comparable<Note>{

	private int note, octave;
	private Fraction length;
	private double begin, duration;

	/**
	 * 
	 * @param start Fraction into the measure where this note starts
	 * @param chord The chord ID number
	 * @param gen PRG, this will take care of the rest of the parameters
	 */
	public Note(Fraction start, int chord, Attractor gen) {
		length = gen.nadicFraction(start.denominator, .2);
		begin = start.asReal();
		duration = length.asReal();
		octave = (int) (gen.nextGaussian() * 1.5);
		note = chord + 2 * gen.nextInt(3);
		while (gen.nextInt(2) == 0)
			note += 3;
		note %= 7;
	}
	
	private static int scale[] = {0, 2, 4, 5, 7, 9, 11};
	/**
	 * 
	 * @return This note a single note number
	 */
	public int getNote() {
		if(note < 0) {
			throw new RuntimeException("Negative notes?");
		}
		int interval = scale[note];
		return interval + octave * 12;
	}
	
	public double getStart() {
		return begin;
	}
	
	public double getLength() {
		return duration;
	}
	
	public int compareTo(Note other) {
		double ddif = begin - other.begin;
		if(Math.abs(ddif) >= .001)
			return ddif > 0 ? 1 : ddif < 0 ? -1 : 0;
		ddif = duration - other.duration;
		if(Math.abs(ddif) >= .001)
			return ddif > 0 ? 1 : ddif < 0 ? -1 : 0;
		int sdif;
		sdif = octave - other.octave;
		if(sdif != 0)
			return sdif;
		sdif = note - other.note;
		if(sdif != 0)
			return sdif;
		return 0;
	}
	
	public boolean equals(Object other) {
		if(other == null)
			return false;
		if(!(other instanceof Note))
			return false;
		return compareTo((Note)other) == 0;
	}

}
