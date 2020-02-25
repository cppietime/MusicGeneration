package com.funguscow.musie.filter;

import java.util.Arrays;

/**
 * A filter of a single pole/zero conjugate pair
 * @author alpac
 *
 */
public class PolesZeroPair implements Filter {

	private double magnitude, arg, buf[], coefs[], stride, unity;
	private int ptr;
	private boolean pole;

	/**
	 * 
	 * @param magnitude 0 - 1, magnitude about unit circle
	 * @param arg -pi - pi, angle about unit circle
	 * @param pole true if pole, false if zero
	 * @param stride delay time
	 */
	public PolesZeroPair(double magnitude, double arg, boolean pole, double stride) {
		this.magnitude = magnitude;
		this.arg = arg;
		this.pole = pole;
		this.stride = stride;
		coefs = new double[3];
		buf = new double[2 * (int)(stride + 1)];
		Arrays.fill(buf, 0);
		calculateCoefs();
		ptr = 0;
		unity = 1;
	}
	
	/**
	 * Calculate coefficients given magnitude and arg
	 */
	public void calculateCoefs() {
		double real = Math.cos(arg) * magnitude;
		double imag = Math.sin(arg) * magnitude;
		unity = 1.0 - 2.0 * real + real * real + imag * imag;
		if(pole)
			unity = 1/unity;
//		double realdif = real / magnitude - real;
//		double imagdif = imag / magnitude + imag;
//		double arguni = Math.abs(1 - magnitude) * Math.sqrt(realdif * realdif + imagdif * imagdif);
//		if(pole)
//			arguni = 1/arguni;
//		unity = Math.max(unity, arguni);
//		double nyuni = 1.0 + 2.0 * real + real * real + imag * imag;
//		if(pole)
//			nyuni = 1/nyuni;
//		unity = Math.max(unity, nyuni);
		unity = 1.0 / unity;
		coefs[0] = 1.0;
		coefs[1] = -2 * real;
		coefs[2] = (real * real + imag * imag);
	}

	public double filter(double input) {
		double output = 0;
		if (pole) {
			input *= unity;
			output = (input - coefs[1] * interpolate(buf, ptr - stride) - coefs[2] * interpolate(buf, ptr - 2 * stride))
					/ coefs[0];
			buf[ptr++] = output;
		} else {
			output = coefs[0] * input + coefs[1] * interpolate(buf, ptr - stride)
					+ coefs[2] * interpolate(buf, ptr - 2 * stride);
			output *= unity;
			buf[ptr++] = input;
		}
		ptr %= buf.length;
		return output;
	}
	
	public void reset() {
		ptr = 0;
		Arrays.fill(buf, 0);
	}

	/**
	 * Linearly interpolate an array element
	 * @param arr Array to take from
	 * @param offset Offset with integer and fractional part
	 * @return Interpolated value
	 */
	public static double interpolate(double arr[], double offset) {
		offset = (offset + arr.length) % arr.length;
		int index = (int) offset;
		double frac = offset - index;
		double left = arr[index];
		double right = arr[(index + 1) % arr.length];
		return left + (right - left) * frac;
	}
	
	public double getMagnitude() {
		return magnitude;
	}

	public PolesZeroPair setMagnitude(double magnitude) {
		this.magnitude = magnitude;
		return this;
	}

	public double getArg() {
		return arg;
	}

	public PolesZeroPair setArg(double arg) {
		this.arg = arg;
		return this;
	}

	public double getStride() {
		return stride;
	}

	public void setStride(double stride) {
		this.stride = stride;
		if (stride * 2 != buf.length) {
			double nbuf[] = new double[2 * (int)(stride + 1)];
			System.arraycopy(buf, 0, nbuf, 0, Math.min(buf.length, nbuf.length));
			buf = nbuf;
			ptr %= buf.length;
		}
	}

	public PolesZeroPair setPole(boolean pole) {
		this.pole = pole;
		return this;
	}

}
