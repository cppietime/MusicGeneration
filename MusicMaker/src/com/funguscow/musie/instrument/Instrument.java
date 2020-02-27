package com.funguscow.musie.instrument;

import com.funguscow.musie.filter.CascadeFilter;
import com.funguscow.musie.filter.Filter;

/**
 * Instrument to play notes
 * @author alpac
 *
 */
public class Instrument implements Effectable<Instrument> {

	private Envelope envelope;
	private Wavegen generator;
	private CascadeFilter effect;

	public Instrument(Wavegen generator) {
		this.generator = generator;
		envelope = null;
		effect = new CascadeFilter();
	}
	
	public Instrument setEnvelope(Envelope envelope) {
		this.envelope = envelope;
		return this;
	}
	
	public Instrument addEffect(Filter filter) {
		effect.addEffect(filter);
		return this;
	}

	/**
	 * Write samples to track, given specifications
	 * @param track
	 * @param frequency
	 * @param amplitude
	 * @param duration Length of note in seconds
	 * @param start Offset of note start in seconds
	 * @param sampleRate
	 */
	public void playNote(double track[], double frequency, double amplitude, double duration, double start,
			int sampleRate) {
		double relTime = envelope == null ? 0 : envelope.getRelease();
		double totalTime = duration + relTime;
		int samps = (int) (totalTime * sampleRate);
		int offset = (int) (start * sampleRate);
		double time = 0;
		effect.reset();
		generator.zeroPhase();
		for(int frame = offset; frame < offset + samps; frame ++) {
			if (frame >= track.length)
				break;
			double sample = generator.generate(frequency, sampleRate);
			sample = effect.filter(sample) * amplitude;
//			sample *= amplitude;
			if (envelope != null)
				sample *= envelope.gainFactor(time, duration);
			track[frame] += sample;
			time += 1.0 / sampleRate;
		}
	}

}
