package com.funguscow.musie.structure;

/**
 * A MIDI-like message to turn a note on or off at a certain time
 * @author alpac
 *
 */
public class NoteMessage implements Comparable<NoteMessage>{
	
	public int channel, note;
	public boolean on;
	public Fraction time;
	
	public NoteMessage(int channel, int note, boolean on, Fraction time) {
		this.channel = channel;
		this.note = note;
		this.on = on;
		this.time = time;
	}
	
	public int compareTo(NoteMessage other) {
		int dif = time.compareTo(other.time);
		if(dif != 0)
			return dif;
		dif = note - other.note;
		if(dif != 0)
			return dif;
		dif = channel - other.channel;
		if(dif != 0)
			return dif;
		if(on && !other.on)
			return 1;
		if(other.on && !on)
			return -1;
		return 0;
	}
	
	public boolean equals(Object other) {
		if(other == null)
			return false;
		if(!(other instanceof NoteMessage))
			return false;
		return compareTo((NoteMessage)other) == 0;
	}

}
