package com.funguscow.musie.structure;

/**
 * A MIDI-like message to turn a note on or off at a certain time
 * @author alpac
 *
 */
public class NoteMessage implements Comparable<NoteMessage>{
	
	public int channel, note;
	public boolean on;
	public double time, duration;
	
	public NoteMessage(int channel, int note, boolean on, double time, double dur) {
		this.channel = channel;
		this.note = note;
		this.on = on;
		this.time = time;
		duration = dur;
	}
	
	public int compareTo(NoteMessage other) {
		double ddif = time - other.time;
		if(Math.abs(ddif) > .001)
			return ddif < 0 ? -1 : ddif > 0 ? 1 : 0;
		int dif = note - other.note;
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
