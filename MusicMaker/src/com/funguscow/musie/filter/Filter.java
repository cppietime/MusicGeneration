package com.funguscow.musie.filter;

/**
 * DSP filter to process a signal one sample at a time
 * @author alpac
 *
 */
@FunctionalInterface
public interface Filter {
	
	/**
	 * Applies a single sample and gets the filtered output
	 * @param input Input sample
	 * @return Output sample
	 */
	public double filter(double input);

}
