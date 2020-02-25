package com.funguscow.musie.random;

import java.util.Random;

import com.funguscow.musie.filter.Filters;
import com.funguscow.musie.filter.Flanger;
import com.funguscow.musie.filter.Phaser;
import com.funguscow.musie.instrument.Effectable;
import com.funguscow.musie.instrument.Envelope;
import com.funguscow.musie.instrument.Instrument;
import com.funguscow.musie.instrument.Oscillator;
import com.funguscow.musie.instrument.Pluck;
import com.funguscow.musie.instrument.Waveforms;
import com.funguscow.musie.instrument.Wavegen;

/**
 * Produces randomized parameters
 * 
 * @author alpac
 *
 */
public class Randomizer {

	private static void applyMods(Oscillator base, Random random, int depth) {
		if (depth > 0) {
			if (random.nextInt() % 3 == 0) {
				Oscillator ampmod = randomSine(random, depth - 1);
				double amp = random.nextDouble() * .2;
				double freq = 0.5 + random.nextDouble() * 4.5;
				base.addAmpMod(ampmod, amp, -freq);
			}
			if (random.nextInt() % 3 == 0) {
				Oscillator freqmod = randomSine(random, depth - 1);
				double amp = random.nextDouble() * .5;
				double freq = 0.5 + random.nextDouble() * 4.5;
				base.addFreqMod(freqmod, amp, -freq);
			}
			for (int i = 0; i < random.nextInt(3); i++) {
				Oscillator phasemod = randomSine(random, depth - 1);
				int odd = 2 + (random.nextInt(4));
				int twopow = (random.nextInt(3));
				int pow = 1 << twopow;
				double freq = odd * pow;
				double amp = Math.PI * 2 / (twopow + 1);
				if (random.nextBoolean())
					freq = 1 / freq;
				System.out.println("Mod w/ freq = " + freq + " amp = " + amp);
				base.addPhaseMod(phasemod, amp, freq);
			}
		}
	}

	/**
	 * 
	 * @param random
	 * @param depth
	 * @return A random sinusoidal oscillator
	 */
	public static Oscillator randomSine(Random random, int depth) {
		Oscillator sine = new Oscillator(Waveforms.Sine);
		applyMods(sine, random, depth);
		return sine;
	}

	/**
	 * 
	 * @param random
	 * @param depth
	 * @return A random sine-like oscillator
	 */
	public static Oscillator randomSinelike(Random random, int depth) {
		Oscillator sinelike = null;
		switch (random.nextInt(3)) {
		case 0:
			sinelike = new Oscillator(Waveforms.Halfsine);
			break;
		case 1:
			sinelike = new Oscillator(Waveforms.Quartersine);
			break;
		case 2:
			sinelike = new Oscillator(Waveforms.Rectsine);
			break;
		}
		applyMods(sinelike, random, depth - 1);
		return sinelike;
	}

	/**
	 * 
	 * @param random
	 * @return A random non-sine wave oscillator
	 */
	public static Oscillator randomNonsine(Random random) {
		Oscillator nonsine = null;
		switch (random.nextInt(3)) {
		case 0:
			nonsine = new Oscillator(Waveforms.Sawtooth);
			break;
		case 1:
			nonsine = new Oscillator(Waveforms.Square);
			break;
		case 2:
			nonsine = new Oscillator(Waveforms.Triangle);
			break;
		}
		nonsine.addEffect(Filters.buttersworth(1 + random.nextInt(3), Math.PI + random.nextGaussian() * .25, false));
		return nonsine;
	}

	/**
	 * 
	 * @param random
	 * @return A randomly filtered noise generator
	 */
	public static Oscillator randomNoise(Random random) {
		Oscillator noise = new Oscillator(Waveforms.Noise);
		for (int i = 0; i < 1 + random.nextInt(3); i++) {
			noise.addEffect(Filters.buttersworth(1 + random.nextInt(3), Math.PI + random.nextGaussian() * .25,
					random.nextBoolean()));
			;
		}
		return noise;
	}

	/**
	 * 
	 * @param random
	 * @return A random wavegen
	 */
	public static Wavegen randomWave(Random random) {
		int type = random.nextInt(4);
		System.out.println("Type = " + type);
		switch (type) {
		case 0:
			return randomSine(random, 3);
		case 1:
			return randomSinelike(random, 3);
		case 2:
			return randomNonsine(random);
		default:
			Wavegen src = randomWave(random);
			if (!(src instanceof Pluck))
				src = new Pluck(src, 1 - Math.exp(random.nextDouble() * -7 - 3));
			return src;
		}
	}

	/**
	 * 
	 * @param random
	 * @return Random AHDSR envelope
	 */
	public static Envelope randomADSR(Random random) {
		Envelope envelope = new Envelope(Math.exp(random.nextDouble() * .2) - 1, random.nextDouble() * .25,
				Math.exp(random.nextDouble() * .5) - 1, .2 + random.nextDouble() * .78, random.nextDouble());
		return envelope;
	}

	/**
	 * Apply random filter effects to base
	 * 
	 * @param base
	 * @param random
	 */
	public static void randomEffects(Effectable<?> base, Random random) {
		switch (random.nextInt(3)) {
		case 0:
			base.addEffect(new Flanger(1 - Math.exp(random.nextDouble() * -2 - 5), true,
					440 * (random.nextDouble() * 5 + 1), random.nextGaussian() * .2 + .5,
					44 * (1 + random.nextDouble() * 8), 2 * Math.PI / 44100 * (1 + random.nextDouble() * 3)));
			break;
		case 1:
			base.addEffect(new Phaser(3 + random.nextInt(4), .2 + random.nextDouble() * .8,
					random.nextGaussian() * Math.PI / 6 + Math.PI / 2, random.nextDouble() * .4,
					random.nextGaussian() * .2 + .5, random.nextGaussian() * Math.PI / 6,
					2 * Math.PI / 44100 * (1 + random.nextDouble() * 3)));
		}
		if (random.nextInt(2) == 0) {
			base.addEffect(Filters.reverb(random.nextGaussian() * 500 + 1600, random.nextDouble() * 900));
		}
		if (random.nextInt(3) == 0) {
			base.addEffect(new Flanger(1 - Math.exp(random.nextDouble() * -2 - 5), false,
					990 * (random.nextDouble() * 4 + 1), random.nextGaussian() * .2 + .5,
					99 * (1 + random.nextDouble() * 8), 2 * Math.PI / 44100 * (1 + random.nextDouble() * 3)));
		}
	}

	/**
	 * 
	 * @param random
	 * @return A randomized instrument
	 */
	public static Instrument randomInstrument(Random random) {
		Wavegen wave = randomWave(random);
		Instrument instrument = new Instrument(wave);
		instrument.setEnvelope(randomADSR(random));
		randomEffects(instrument, random);
		return instrument;
	}

	/**
	 * 
	 * @param random
	 * @return A randomized drum instrument
	 */
	public static Instrument randomDrums(Random random) {
		Wavegen wave = randomNoise(random);
		Envelope envelope = new Envelope(random.nextDouble() * .08, 0, random.nextDouble() * .08,
				random.nextDouble() * .005, random.nextDouble() * .1);
		Instrument drum = new Instrument(wave).setEnvelope(envelope);
		randomEffects(drum, random);
		return drum;
	}

}
