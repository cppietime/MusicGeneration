package com.funguscow.musie.structure;

import java.util.Random;

/**
 * A strange attractor to produce psuedo-random numbers
 * @author alpac
 *
 */
public class Attractor extends Random {

	private static final long serialVersionUID = 3654258890310237069L;
	
	private double P, R, B, x, y, z;
	private double tempGaus, previous;
	private boolean hasGaus;
	
	/**
	 * Automatically produce a randomly configured attractor
	 * @param random
	 */
	public Attractor(Random random) {
		tune(random);
	}
	
	/**
	 * 
	 * @param P Fluid viscosity parameter
	 * @param R Temperature differential parameter
	 * @param B Space aspect ratio parameter
	 * @param x Starting x position
	 * @param y Starting y position
	 * @param z Starting z position
	 */
	public Attractor(double P, double R, double B, double x, double y, double z) {
		this.P = P;
		this.B = B;
		this.R = R;
		this.x = x;
		this.y = y;
		this.z = z;
		hasGaus = false;
	}
	
	public void tune(Random random) {
		this.P = 10 + random.nextDouble() * 10;
		this.R = 18 + random.nextDouble() * 20;
		this.B = 1 + random.nextDouble() * 10;
		this.x = random.nextGaussian();
		this.y = random.nextGaussian();
		this.z = random.nextGaussian();
	}
	
	/**
	 * Iterate step steps with t step-size
	 * @param t
	 * @param step
	 */
	public void orbit(double t, int step) {
		for(int i = 0; i < step; i++) {
			double dx = P*(y - x);
			double dy = R*x - y - x*z;
			double dz = x*y - B*z;
			x += dx * t;
			y += dy * t;
			z += dz * t;
		}
	}
	
	/**
	 * 
	 * @return Current double value
	 */
	public double rawDouble() {
		orbit(.01, 100);
		double now = Math.abs(x - x * y - x * z - y * z + x * y * z);
		if(Math.abs(now - previous) <= .00001){
			tune(new Random());
		}
		previous = now;
		return now;
	}
	
	/**
	 * 
	 * @param n
	 * @param lambda Chance to continue at each iteration
	 * @return A geometrically distributed random n-adic fraction
	 */
	public Fraction nadicFraction(int n, double lambda) {
		double raw = rawDouble();
		int depth = 0;
		while(raw % 1 <= lambda) {
//			nadic.incrementNadic(n);
			raw = rawDouble();
			depth ++;
		}
		int denom = (int)Math.pow(n, depth);
		System.out.println("Denom = " + denom);
		Fraction nadic = new Fraction(1, denom);
		int incrs = nextInt(denom);
		for(int i = 0; i < incrs; i ++)
			nadic.incrementNadic(n);
		return nadic;
	}
	
	@Override
	public double nextDouble() {
		return rawDouble() % 1;
	}
	
	@Override
	public double nextGaussian() {
		if(hasGaus) {
			hasGaus = false;
			return tempGaus;
		}
		double one = rawDouble() % 1, two = rawDouble() % 1;
		double radius = Math.sqrt(-2 * Math.log(one));
		double theta = 2 * Math.PI * two;
		tempGaus = radius * Math.cos(theta);
		hasGaus = true;
		return radius * Math.sin(theta);
	}
	
	@Override
	public int nextInt(int bound) {
		return (int)rawDouble() % bound;
	}
	
	@Override
	public String toString() {
		return "" + ((int)rawDouble());
	}
	
}
