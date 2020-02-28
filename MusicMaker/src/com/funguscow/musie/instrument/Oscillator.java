package com.funguscow.musie.instrument;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

import com.funguscow.musie.filter.CascadeFilter;
import com.funguscow.musie.filter.Filter;

/**
 * Single oscillator for use in instruments
 * @author alpac
 *
 */
public class Oscillator implements Wavegen, Effectable<Oscillator>{
	
	/**
	 * A helper class for modulating oscillators
	 * @author alpac
	 *
	 */
	public static class Modulator {
		public Oscillator osc;
		public double gain;
		public double freq_ratio;
		public Modulator(Oscillator osc, double gain, double freq) {
			this.osc = osc;
			this.gain = gain;
			freq_ratio = freq;
		}
		public double getFreq(double freq) {
			if (freq_ratio < 0)
				return -freq_ratio;
			return freq * freq_ratio;
		}
	}
	
	private DoubleUnaryOperator generator;
	private List<Modulator> phase_mod, freq_mod, amp_mod, synthesis;
	private CascadeFilter effect;
	private double phase;
	
	/**
	 * Create a new blank oscillator
	 * @param generator Waveform used
	 */
	public Oscillator(DoubleUnaryOperator generator) {
		this.generator = generator;
		phase_mod = new ArrayList<Modulator>();
		freq_mod = new ArrayList<Modulator>();
		amp_mod = new ArrayList<Modulator>();
		synthesis = new ArrayList<Modulator>();
		effect = new CascadeFilter();
		phase = 0;
	}
	
	/**
	 * Add filter f to this effect chain
	 * @param f
	 * @return this
	 */
	public Oscillator addEffect(Filter f) {
		effect.addEffect(f);
		return this;
	}
	
	/**
	 * Add an oscillator to modulate phase
	 * @param osc Modulating oscillator
	 * @param gain Amplitude factor
	 * @param ratio Ratio of frequencies
	 * @return this
	 */
	public Oscillator addPhaseMod(Oscillator osc, double gain, double ratio) {
		phase_mod.add(new Modulator(osc, gain, ratio));
		return this;
	}
	
	/**
	 * Add an oscillator to modulate frequency
	 * @param osc Modulating oscillator
	 * @param gain Amplitude factor
	 * @param ratio Ratio of frequencies
	 * @return this
	 */
	public Oscillator addFreqMod(Oscillator osc, double gain, double ratio) {
		freq_mod.add(new Modulator(osc, gain, ratio));
		return this;
	}
	
	/**
	 * Add an oscillator to modulate amplitude
	 * @param osc Modulating oscillator
	 * @param gain Amplitude factor
	 * @param ratio Ratio of frequencies
	 * @return this
	 */
	public Oscillator addAmpMod(Oscillator osc, double gain, double ratio) {
		amp_mod.add(new Modulator(osc, gain, ratio));
		return this;
	}
	
	/**
	 * Add an oscillator to add to output
	 * @param osc Modulating oscillator
	 * @param gain Amplitude factor
	 * @param ratio Ratio of frequencies
	 * @return this
	 */
	public Oscillator addSynth(Oscillator osc, double gain, double ratio) {
		synthesis.add(new Modulator(osc, gain, ratio));
		return this;
	}
	
	/**
	 * Get the sample next
	 * @param frequency
	 * @param sampleRate
	 * @return
	 */
	public double generate(double frequency, int sampleRate) {
		double freqUse = frequency;
		for(Modulator mod : freq_mod) {
//			freqUse *= 1.0 + mod.gain * mod.osc.generate(mod.getFreq(frequency), sampleRate);
			freqUse *= Math.pow(2, mod.gain * mod.osc.generate(mod.getFreq(frequency), sampleRate) / 12);
		}
		double phaseUse = phase;
		for(Modulator mod : phase_mod) {
			phaseUse += mod.gain * mod.osc.generate(mod.getFreq(freqUse), sampleRate);
		}
		double ampUse = generator.applyAsDouble(phaseUse);
		for(Modulator mod : amp_mod) {
			ampUse *= 1.0 + mod.gain * mod.osc.generate(mod.getFreq(freqUse), sampleRate);
		}
		for(Modulator mod : synthesis) {
			ampUse += mod.gain * mod.osc.generate(mod.getFreq(freqUse), sampleRate);
		}
		phase += 2 * Math.PI * frequency / sampleRate;
		ampUse = effect.filter(ampUse);
		return ampUse;
	}
	
	/**
	 * Reset the phase of the oscillator and all inputs
	 */
	public void zeroPhase() {
		phase = 0;
		for(Modulator mod : freq_mod)
			mod.osc.zeroPhase();
		for(Modulator mod : phase_mod)
			mod.osc.zeroPhase();
		for(Modulator mod : amp_mod)
			mod.osc.zeroPhase();
		for(Modulator mod : synthesis)
			mod.osc.zeroPhase();
		effect.reset();
	}

}
