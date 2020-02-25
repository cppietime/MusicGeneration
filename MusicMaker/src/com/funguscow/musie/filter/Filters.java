package com.funguscow.musie.filter;

public class Filters {
	
	/**
	 * A single conjugate-pair all-pole filter
	 * @param mag Mangitude of pole
	 * @param angle Argument of pole
	 * @param stride Delay
	 * @return
	 */
	public static PolesZeroPair Pole(double mag, double angle, int stride) {
		return new PolesZeroPair(mag, angle, true, stride);
	}
	
	/**
	 * Conjugate-pair all-zero filter
	 * @param mag Magnitude of zero
	 * @param angle Argument of zero
	 * @param stride Delay
	 * @return
	 */
	public static PolesZeroPair Zero(double mag, double angle, int stride) {
		return new PolesZeroPair(mag, angle, false, stride);
	}
	
	/**
	 * All-pass filter at mag*exp(angle*i), w/ delay stride
	 * @param mag
	 * @param angle
	 * @param stride
	 * @return
	 */
	public static CascadeFilter Allpass(double mag, double angle, int stride) {
		if (mag < 1)
			mag = 1.0 / mag;
		Filter zeros = Zero(mag, angle, stride);
		Filter poles = Pole(1.0 / mag, angle, stride);
		return new CascadeFilter().addFilter(zeros).addFilter(poles);
	}

}
