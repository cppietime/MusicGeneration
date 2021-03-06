package com.funguscow.musie.instrument;

import java.util.function.DoubleUnaryOperator;

public class Waveforms {

	public static DoubleUnaryOperator Sine = Math::sin,
			Sawtooth = (phase) -> (phase % (Math.PI * 2)) / Math.PI - 1,
			Square = (phase) -> (phase % (2 * Math.PI)) < Math.PI ? -1 : 1,
			Triangle = (phase) -> Math.abs(Sawtooth.applyAsDouble(phase)) * -2 + 1,
			Halfsine = (phase) -> (phase % (2 * Math.PI)) < Math.PI ? Math.sin(phase) : -1,
			Quartersine = (phase) -> (phase % (2 * Math.PI)) < Math.PI / 2 ? Math.sin(phase) : -1,
			Rectsine = (phase) -> Math.abs(Math.sin(phase)) * 2 - 1,
			Noise = (phase) -> Math.random() * 2 - 1;
	
}
