package com.funguscow.musie.instrument;

import java.util.Arrays;

/**
 * String vibrator emulator
 * @author alpac
 *
 */
public class Pluck implements Wavegen {
	
	private Wavegen generator;
	private double decay;
	private int time;
	private double ring_buffer[];
	
	public Pluck(Wavegen generator, double decay) {
		this.generator = generator;
		this.decay = decay;
		ring_buffer = new double[4096];
	}
	
	/**
	 * Reset for new note
	 */
	public void zeroPhase() {
		time = 0;
		Arrays.fill(ring_buffer, 0);
		generator.zeroPhase();
	}
	
	/**
	 * Generate sample in note
	 * @param frequency
	 * @param sampleRate
	 * @return
	 */
	public double generate(double frequency, int sampleRate) {
		double sample;
		int delay = (int)(.5 + sampleRate / frequency);
		if (time < delay) {
			sample = generator.generate(frequency, sampleRate);
		} else {
			int lindex = (time + 1 - delay) % delay, rindex = (time + 2 - delay) % delay;
			sample = decay / 2 * (ring_buffer[lindex] + ring_buffer[rindex]);
		}
		ring_buffer[time % delay] = sample;
		time = (time + 1);
		return sample;
	}

}
