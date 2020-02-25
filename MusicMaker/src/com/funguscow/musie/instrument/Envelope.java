package com.funguscow.musie.instrument;

public class Envelope {
	
	private double attack, hold, decay, sustain, release;
	
	public Envelope(double A, double H, double D, double S, double R) {
		attack = A;
		hold = H;
		decay = D;
		sustain = S;
		release = R;
	}
	
	public double gainFactor(double time, double duration) {
		double pre = Math.min(time, duration);
		double gain = sustain;
		if(pre < attack)
			gain = 1.0 * pre / attack;
		else if(pre - attack <= hold)
			gain = 1.0;
		else if(pre - attack - hold < decay)
			gain = 1.0 + (sustain - 1.0) * (pre - attack - hold) / decay;
		if(time >= duration + release)
			gain = 0;
		else if(time > duration)
			gain -= gain * (time - duration) / release;
		return gain;
	}
	
	public double getRelease() {
		return release;
	}

}
