package com.funguscow.musie.filter;

/**
 * A flanger effect
 * @author alpac
 *
 */
public class Flanger implements Filter {

	private DelayLine delay;
	private double weight, avg, amp, omega, phase;

	/**
	 * 
	 * @param gain Decay coefficient
	 * @param bwd true if feed-backward
	 * @param stride Average delay in samples
	 * @param weight Dry/wet mixing
	 * @param amp Variation in delay
	 * @param omega Angular frequency of delay
	 */
	public Flanger(double gain, boolean bwd, double stride, double weight, double amp, double omega) {
		this.weight = weight;
		this.avg = stride;
		this.amp = amp;
		this.omega = omega;
		phase = 0;
		delay = new DelayLine(gain, bwd, stride);
	}
	
	public double filter(double input) {
		double flanged = delay.filter(input);
		double output = input + (flanged - input) * weight;
		phase = (phase + omega) % (2 * Math.PI);
		delay.setStride(avg + amp * Math.cos(phase));
		return output;
	}
	
	public void reset() {
		delay.reset();
		phase = 0;
	}

}
