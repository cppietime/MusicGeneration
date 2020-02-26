package com.funguscow.musie.wave;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.sound.sampled.AudioFormat;

import com.funguscow.musie.instrument.Instrument;
import com.funguscow.musie.random.Randomizer;

public class Wave {
	
	private static void writeLE(OutputStream dest, long value, int len) throws IOException {
		for(int i = 0; i < len; i ++) {
			byte b = (byte)(value & 0xff);
			dest.write(b);
			value >>= 8;
		}
	}
	
	public static boolean write(File dest, double track[], AudioFormat format) {
		OutputStream os;
		try {
			long bytesSize = track.length * format.getFrameSize();
			os = new FileOutputStream(dest);
			os.write("RIFF".getBytes());
			writeLE(os, 36 + bytesSize, 4);
			os.write("WAVE".getBytes());
			os.write("fmt ".getBytes());
			writeLE(os, 16, 4);
			writeLE(os, 1, 2);
			writeLE(os, format.getChannels(), 2);
			writeLE(os, (long) format.getFrameRate(), 4);
			writeLE(os, (long) format.getFrameRate() * format.getFrameSize(), 4);
			writeLE(os, format.getFrameSize(), 2);
			writeLE(os, format.getSampleSizeInBits(), 2);
			os.write("data".getBytes());
			writeLE(os, bytesSize, 4);
			for(int i = 0; i < track.length; i++) {
				double sample = track[i];
				int isamp;
				if(format.getSampleSizeInBits() <= 8)
					isamp = (int)((sample + 1) / 2 * 255);
				else
					isamp = (int)(sample * 32767);
				for(int j = 0; j < format.getChannels(); j++) {
					writeLE(os, isamp, format.getSampleSizeInBits() / 8);
				}
			}
			os.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static void main(String[] args) {
		AudioFormat format = new AudioFormat(44100, 8, 1, false, false);
		double samples[] = new double[44100 * 2];
		Instrument guitar = Randomizer.randomInstrument();
		guitar.playNote(samples, 440, 0.5, 1.5, 0, 44100);
		double max = 0;
		for(double d : samples) {
			if(Math.abs(d) > max)
				max = Math.abs(d);
		}
		System.out.println(max);
		File ofile = new File("test.wav");
		System.out.println("Writng to " + ofile.getAbsolutePath());
		write(ofile, samples, format);
	}

}
