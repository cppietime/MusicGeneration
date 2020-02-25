package com.funguscow.musie.filter;

/**
 * Hopefully, a phaser effect
 * @author alpac
 *
 */
public class Phaser implements Filter {
	
	private CascadeFilter filters[];
	private double feedback, weight, magnitude, arg, amp, omega, phase, prev;
	
	/**
	 * 
	 * @param stages Number of all-pass filters
	 * @param magnitude Magnitude of zeroes in filters
	 * @param arg Angle of zeroes/poles
	 * @param feedback Feedback coefficient
	 * @param weight Dry/wet mixing
	 * @param amp Variation in arg
	 * @param omega Angular frequency of oscillation
	 */
	public Phaser(int stages, double magnitude, double arg, double feedback, double weight, double amp, double omega) {
		this.feedback = feedback;
		this.weight = weight;
		this.amp = amp;
		this.omega = omega;
		this.arg = arg;
		this.magnitude = magnitude;
		phase = 0;
		prev = 0;
		filters = new CascadeFilter[stages];
		for(int i = 0; i < stages; i ++) {
			filters[i] = Filters.Allpass(this.magnitude, arg, 1);
		}
	}
	
	public double filter(double input) {
		prev = input + (prev - input) * feedback;
		phase = (phase + omega) % (2 * Math.PI);
		double carg = arg + amp * Math.cos(phase);
		for(CascadeFilter filter : filters) {
			prev = filter.filter(prev);
			((PolesZeroPair)filter.getFilter(0)).setArg(carg).calculateCoefs();
			((PolesZeroPair)filter.getFilter(1)).setArg(carg).calculateCoefs();
		}
		prev = input + (prev - input) * weight;
		return prev;
	}
	
	public void reset() {
		phase = prev = 0;
		for(CascadeFilter filter : filters)
			filter.reset();
	}

}
