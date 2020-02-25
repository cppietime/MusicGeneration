package com.funguscow.musie.instrument;

import java.util.function.DoubleUnaryOperator;

public class Waveforms {

	public DoubleUnaryOperator Sine = Math::sin,
			Sawtooth = (phase) -> (phase % (Math.PI * 2)) / Math.PI - 1,
			Square = (phase) -> phase < Math.PI ? -1 : 1,
			Triangle = (phase) -> Math.abs(Sawtooth.applyAsDouble(phase)) * -2 + 1,
			Halfsine = (phase) -> phase < Math.PI ? Math.sin(phase) * 2 - 1 : -1,
			Quartersine = (phase) -> phase < Math.PI / 2 ? Math.sin(phase) * 2 - 1 : -1,
			Rectsine = (phase) -> Math.abs(Math.sin(phase)) * 2 - 1;
	
}
