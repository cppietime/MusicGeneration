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

	private static final int AMPMOD_ODDS = 3, FREQMOD_ODDS = 3, PHASEMOD_ATTS = 4, PHASEODD_MIN = 2, PHASEODD_RANGE = 4,
			PHASEPOW_RANGE = 3;
	private static final double AMPAMP = .05, AMPFREQ_RANGE = 4.5, AMPFREQ_MIN = .05, FREQAMP = .05,
			FREQFREQ_RANGE = 4.5, FREQFREQ_MIN = .05;

	private static Random random = new Random();

	private static void applyMods(Oscillator base, int depth) {
		if (depth > 0) {
			if (random.nextInt() % AMPMOD_ODDS == 0) {
				Oscillator ampmod = randomSine(depth - 1);
				double amp = random.nextDouble() * AMPAMP;
				double freq = AMPFREQ_MIN + random.nextDouble() * AMPFREQ_RANGE;
				base.addAmpMod(ampmod, amp, -freq);
			}
			if (random.nextInt() % FREQMOD_ODDS == 0) {
				Oscillator freqmod = randomSine(depth - 1);
				double amp = random.nextDouble() * FREQAMP;
				double freq = FREQFREQ_MIN + random.nextDouble() * FREQFREQ_RANGE;
				base.addFreqMod(freqmod, amp, -freq);
			}
			for (int i = 0; i < random.nextInt(PHASEMOD_ATTS); i++) {
				Oscillator phasemod = randomSine(depth - 1);
				int odd = PHASEODD_MIN + (random.nextInt(PHASEODD_RANGE));
				int twopow = (random.nextInt(PHASEPOW_RANGE));
				int pow = 1 << twopow;
				double freq = odd * pow;
				double amp = .2 * random.nextDouble();
				if (random.nextBoolean())
					freq = 1 / freq;
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
	public static Oscillator randomSine(int depth) {
		Oscillator sine = new Oscillator(Waveforms.Sine);
		applyMods(sine, depth);
		return sine;
	}

	/**
	 * 
	 * @param random
	 * @param depth
	 * @return A random sine-like oscillator
	 */
	public static Oscillator randomSinelike(int depth) {
		Oscillator sinelike = null;
		int type = random.nextInt(3);
		switch (type) {
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
		applyMods(sinelike, depth - 1);
		return sinelike;
	}

	private static final double LOWBUT_DEV = Math.PI / 2;
	private static final int LOWBUT_MIN = 1, LOWBUTN_RANGE = 3;

	/**
	 * 
	 * @param random
	 * @return A random non-sine wave oscillator
	 */
	public static Oscillator randomNonsine() {
		Oscillator nonsine = null;
		int nstype = random.nextInt(3);
		switch (nstype) {
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
		nonsine.addEffect(Filters.buttersworth(LOWBUT_MIN + random.nextInt(LOWBUTN_RANGE),
				Math.PI / 4 + random.nextDouble() * LOWBUT_DEV, false));
		return nonsine;
	}

	private static final int BUT_ATTS = 3;

	/**
	 * 
	 * @param random
	 * @return A randomly filtered noise generator
	 */
	public static Oscillator randomNoise() {
		Oscillator noise = new Oscillator(Waveforms.Noise);
		for (int i = 0; i < 1 + random.nextInt(BUT_ATTS); i++) {
			noise.addEffect(Filters.buttersworth(LOWBUT_MIN + random.nextInt(LOWBUTN_RANGE),
					Math.PI + random.nextGaussian() * LOWBUT_DEV, random.nextBoolean()));
		}
		return noise;
	}

	private static final int DEPTH0 = 2;
	private static final double PICLOG_BASE = 3, PICLOG_MUL = 7;

	private static boolean plucky = false;

	/**
	 * 
	 * @param random
	 * @return A random wavegen
	 */
	public static Wavegen randomWave() {
		int type = random.nextInt(plucky ? 3 : 5);
		switch (type) {
		case 0:
		case 1:
			return randomSine(DEPTH0);
//		case -1:
//			return randomNonsine();
		case 2:
			return randomSinelike(DEPTH0);
		case 3:
		default:
			plucky = true;
			Wavegen src = randomWave();
			if (!(src instanceof Pluck))
				src = new Pluck(src, 1 - Math.exp(random.nextDouble() * -PICLOG_MUL - PICLOG_BASE));
			plucky = false;
			return src;
		}
	}

	private static final double ATT_MUL = .05, HOLD_MUL = .2, DEC_MUL = .5, SUS_BASE = .2, SUS_MUL = .78, REL_MUL = .1;

	/**
	 * 
	 * @param random
	 * @return Random AHDSR envelope
	 */
	public static Envelope randomADSR() {
		Envelope envelope = new Envelope(Math.exp(random.nextDouble() * ATT_MUL) - 1, random.nextDouble() * HOLD_MUL,
				Math.exp(random.nextDouble() * DEC_MUL) - 1, SUS_BASE + random.nextDouble() * SUS_MUL,
				random.nextDouble() * REL_MUL);
		return envelope;
	}

	private static final double STRMUL = 2, STRBASE = 5, DELBASE = 440, DELRAN = 5, FWBASE = .5, FWMUL = .2, FDMUL = 44,
			FDRAM = 8, FDOMRAN = 3, MAGMIN = .2, MAGRANGE = .8, PHMULS = 6, PHMUL = 2, FEEDMUL = .4, PWMUL = .2,
			PWBASE = .5, PWARGS = 6, PDOMRAN = 3, DELAY_MUL = 200, DELAY_BASE = 1000, DETUNE_MUL = 900, FORM_MIN = .3,
			FORM_MAX = .98;
	private static final double FORMANTS[] = { 240, 2400, 235, 2100, 390, 2300, 370, 1900, 585, 1710, 850, 1610, 820,
			1530, 750, 940, 700, 760, 600, 1170, 500, 700, 460, 1310, 360, 640, 300, 1390, 250, 595 };
	private static final int STAGEMIN = 3, STAGERANGE = 4, REVERB_CHANCE = 2, FORMANT_CHANCE = 3;

	/**
	 * Apply random filter effects to base
	 * 
	 * @param base
	 * @param random
	 */
	public static void randomEffects(Effectable<?> base) {
		switch (random.nextInt(3)) {
		case 0:
			base.addEffect(new Flanger(1 - Math.exp(random.nextDouble() * -STRMUL - STRBASE), true,
					DELBASE * (random.nextDouble() * DELRAN + 1), random.nextGaussian() * FWMUL + FWBASE,
					FDMUL * (1 + random.nextDouble() * FDRAM),
					2 * Math.PI / 44100 * (1 + random.nextDouble() * FDOMRAN)));
			break;
		case 1:
			base.addEffect(new Phaser(STAGEMIN + random.nextInt(STAGERANGE), MAGMIN + random.nextDouble() * MAGRANGE,
					random.nextGaussian() * Math.PI / PHMULS + Math.PI / PHMUL, random.nextDouble() * FEEDMUL,
					random.nextGaussian() * PWMUL + PWBASE, random.nextGaussian() * Math.PI / PWARGS,
					2 * Math.PI / 44100 * (1 + random.nextDouble() * PDOMRAN)));
		}
		if (random.nextInt(REVERB_CHANCE) == 0) {
			base.addEffect(
					Filters.reverb(random.nextGaussian() * DELAY_MUL + DELAY_BASE, random.nextDouble() * DETUNE_MUL));
		}
		for (int i = 0; i < random.nextInt(BUT_ATTS); i++) {
			base.addEffect(Filters.buttersworth(LOWBUT_MIN + random.nextInt(LOWBUTN_RANGE),
					Math.PI + random.nextGaussian() * LOWBUT_DEV, random.nextBoolean()));
		}
		if (random.nextInt(FORMANT_CHANCE) == 0) {
			int index = random.nextInt(FORMANTS.length / 2);
			base.addEffect(Filters.formant(random.nextDouble() * (FORM_MAX - FORM_MIN) + FORM_MIN, FORMANTS[index * 2],
					FORMANTS[index * 2 + 1]));
		}
	}

	/**
	 * 
	 * @param random
	 * @return A randomized instrument
	 */
	public static Instrument randomInstrument() {
		Wavegen wave = randomWave();
		Instrument instrument = new Instrument(wave);
		instrument.setEnvelope(randomADSR());
		randomEffects(instrument);
		return instrument;
	}

	private static final double DRUM_ATT = .02, DRUM_DEC = .175, DRUM_SUS = .005, DRUM_REL = .1, PLUCK_ODDS = 0;

	/**
	 * 
	 * @param random
	 * @return A randomized drum instrument
	 */
	public static Instrument randomDrums() {
		Wavegen wave = randomNoise();
		if (random.nextDouble() <= PLUCK_ODDS)
			wave = new Pluck(wave, .98);
		Envelope envelope = new Envelope(random.nextDouble() * DRUM_ATT, 0, random.nextDouble() * DRUM_DEC,
				random.nextDouble() * (wave instanceof Pluck ? .5 : DRUM_SUS), random.nextDouble() * DRUM_REL);
		Instrument drum = new Instrument(wave).setEnvelope(envelope);
		randomEffects(drum);
		drum.addEffect(Filters.buttersworth(3, Math.PI / 4, false));
		return drum;
	}

}
