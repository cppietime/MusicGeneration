package com.funguscow.musie.instrument;

/**
 * Oscillators and strings
 * @author alpac
 *
 */
public interface Wavegen {
	
	/**
	 * Reset note
	 */
	public void zeroPhase();
	
	/**
	 * Generate  sample
	 * @param frequency
	 * @param sampleRate
	 * @return
	 */
	public double generate(double frequency, int sampleRate);

}
