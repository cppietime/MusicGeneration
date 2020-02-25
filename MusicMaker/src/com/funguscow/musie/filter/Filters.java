package com.funguscow.musie.filter;

public class Filters {

	/**
	 * A single conjugate-pair all-pole filter
	 * 
	 * @param mag    Mangitude of pole
	 * @param angle  Argument of pole
	 * @param stride Delay
	 * @return
	 */
	public static PolesZeroPair Pole(double mag, double angle, int stride) {
		return new PolesZeroPair(mag, angle, true, stride);
	}

	/**
	 * Conjugate-pair all-zero filter
	 * 
	 * @param mag    Magnitude of zero
	 * @param angle  Argument of zero
	 * @param stride Delay
	 * @return
	 */
	public static PolesZeroPair Zero(double mag, double angle, int stride) {
		return new PolesZeroPair(mag, angle, false, stride);
	}

	/**
	 * All-pass filter at mag*exp(angle*i), w/ delay stride
	 * 
	 * @param mag
	 * @param angle
	 * @param stride
	 * @return
	 */
	public static CascadeFilter Allpass(double mag, double angle, int stride) {
		System.out.println("Magnitude = " + mag);
		if (mag < 1)
			mag = 1.0 / mag;
		Filter zeros = Zero(mag, angle, stride);
		Filter poles = Pole(1.0 / mag, angle, stride);
		return new CascadeFilter().addEffect(zeros).addEffect(poles);
	}

	/**
	 * 
	 * @return Standardish reverb
	 */
	public static CascadeFilter reverb(double delay, double detune) {
		CascadeFilter reverb = new CascadeFilter();
		ParallelFilter combs = new ParallelFilter();
		combs.addFilter(new DelayLine(0, false, 1), .25).addFilter(new DelayLine(.75, true, delay), .25)
				.addFilter(new DelayLine(.75, true, delay + detune), .25)
				.addFilter(new DelayLine(.75, true, delay - detune), .25);
		reverb.addEffect(combs).addEffect(Allpass(.9, Math.PI / 3, 10)).addEffect(Allpass(.9, Math.PI * 2 / 3, 7));
		return reverb;
	}
	
	/**
	 * Produce a butterworth filter w/ even poles
	 * @param n Half the # poles
	 * @param cutoff Cutoff angular frequency
	 * @param highpass true for highpass filter
	 * @return
	 */
	public static CascadeFilter buttersworth(int n, double cutoff, boolean highpass) {
		CascadeFilter butter = new CascadeFilter();
		n *= 2;
		double incr = Math.PI / (n + 1);
		for(int i = 0; i < n; i++) {
			double angle = Math.PI / 2 + incr * (i + 1);
			double sreal = cutoff * Math.cos(angle),
					simag = cutoff * Math.sin(angle);
			double zmag = Math.exp(sreal);
			PolesZeroPair pole = new PolesZeroPair(zmag, simag, highpass, 1);
			butter.addEffect(pole);
		}
		return butter;
	}

}
