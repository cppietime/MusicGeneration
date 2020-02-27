package com.funguscow.musie.filter;

import java.util.Arrays;

/**
 * A filter that is just a delay line
 * @author alpac
 *
 */
public class DelayLine implements Filter{
	
	private double gain, stride, buf[];
	private int ptr;
	private boolean bwd;
	
	/**
	 * 
	 * @param gain Coefficient multiplied by delayed sample
	 * @param bwd true if feed-backward
	 * @param stride amount back to pull samples from
	 */
	public DelayLine(double gain, boolean bwd, double stride) {
		this.gain = gain;
		this.stride = stride;
		this.bwd = bwd;
		buf = new double[(int)(stride + 1)];
		ptr = 0;
	}
	
	public double filter(double input) {
		double output = input * (1 - gain) + gain * PolesZeroPair.interpolate(buf, ptr - stride);
		if(bwd) {
			buf[ptr++] = output;
		}else {
			buf[ptr++] = input;
		}
		ptr %= buf.length;
		return output;
	}
	
	public void reset() {
		ptr = 0;
		Arrays.fill(buf, 0);
	}
	
	public double getGain() {
		return gain;
	}
	
	public DelayLine setGain(double gain) {
		this.gain = gain;
		return this;
	}
	
	public double getStride() {
		return stride;
	}
	
	public DelayLine setStride(double stride) {
		this.stride = Math.max(1, stride);
		if(buf.length != (int)(this.stride + 1)) {
			double nbuf[] = new double[(int)(this.stride + 1)];
			System.arraycopy(buf, 0, nbuf, 0, Math.min(buf.length, nbuf.length));
			buf = nbuf;
			ptr %= buf.length;
		}
		return this;
	}
	
	public DelayLine setBwd(boolean bwd) {
		this.bwd = bwd;
		return this;
	}

}
