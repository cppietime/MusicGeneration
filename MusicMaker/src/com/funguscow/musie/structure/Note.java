package com.funguscow.musie.structure;

/**
 * An abstract note object
 * @author alpac
 *
 */
public class Note implements Comparable<Note>{

	private int note, octave;
	private Fraction start, length;

	/**
	 * 
	 * @param start Fraction into the measure where this note starts
	 * @param chord The chord ID number
	 * @param gen PRG, this will take care of the rest of the parameters
	 */
	public Note(Fraction start, int chord, Attractor gen) {
		this.start = start;
		length = gen.nadicFraction(start.denominator, .2);
		octave = (int) (gen.nextGaussian() * 1.5);
		note = chord + 2 * gen.nextInt(3);
		while (gen.nextInt(2) == 0)
			note += 3;
		note %= 7;
	}
	
	/**
	 * 
	 * @return This note a single note number
	 */
	public int getNote() {
		int interval = note * 2 - note / 3 + note / 6;
		return interval + octave * 12;
	}
	
	public Fraction getStart() {
		return start;
	}
	
	public Fraction getLength() {
		return length;
	}
	
	public int compareTo(Note other) {
		int sdif = start.compareTo(other.start);
		if(sdif != 0)
			return sdif;
		sdif = length.compareTo(other.length);
		if(sdif != 0)
			return sdif;
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
