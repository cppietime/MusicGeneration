package com.funguscow.musie.structure;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * A motif representing a series of values in a certain range to be further expanded into modules
 * @author alpac
 *
 */
public class Motif {
	
	private int length, maximum;
	private int values[], chords[];
	private Fraction timeSig;
	
	public Motif(int length, int maximum, Fraction sig, Random random) {
		this.length = length;
		this.maximum = maximum;
		this.timeSig = sig;
		values = new int[this.length];
		chords = new int[this.length];
		Map<Integer, Integer> correspond = new HashMap<Integer, Integer>();
		for(int i = 0; i < this.length; i ++) {
			int sel = random.nextInt(this.maximum);
			if(correspond.containsKey(sel))
				values[i] = correspond.get(sel);
			else {
				int num = correspond.size();
				values[i] = num;
				correspond.put(sel, num);
			}
			chords[i] = random.nextInt(6);
		}
		this.maximum = correspond.size();
	}

	public int getLength() {
		return length;
	}

	public int getMaximum() {
		return maximum;
	}
	
	public int getValue(int i) {
		return values[i];
	}
	
	public int getChord(int i) {
		return chords[i];
	}
	
	public Fraction getTimeSig() {
		return timeSig;
	}

}
