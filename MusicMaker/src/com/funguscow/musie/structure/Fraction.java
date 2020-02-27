package com.funguscow.musie.structure;

/**
 * A rational number represented with numerator and denominator
 * @author alpac
 *
 */
public class Fraction implements Comparable<Fraction>, Cloneable{
	
	public int numerator, denominator;
	
	public Fraction(long numerator,  long denominator, boolean reduce) {
		long gcd = reduce ? GCD(numerator, denominator) : 1;
		this.numerator = (int)(numerator / gcd);
		this.denominator = (int)(denominator / gcd);
		if(numerator < 0 || denominator < 0) {
			throw new RuntimeException("Negative fraction? " + this);
		}
	}
	
	public Fraction(long numerator, long denominator) {
		this(numerator, denominator, true);
	}
	
	/**
	 * Modifices this fraction in place
	 * @return The next enumerated positive rational
	 */
	public Fraction increment() {
		int nextnum = numerator, nextden = denominator;
		do {
			nextnum ++;
			if(nextnum >= nextden) {
				nextnum = 1;
				nextden ++;
			}
		}while(GCD(nextnum, nextden) != 1);
		numerator = nextnum;
		denominator = nextden;
		return this;
	}
	
	/**
	 * Increment but only with denominators of powers of n
	 * @param n
	 * @return The next rational
	 */
	public Fraction incrementNadic(int n) {
		int nextnum = numerator, nextden = denominator;
		if(numerator > 1) {
			int gcd = (int)GCD(nextnum, nextden);
			nextnum /= gcd;
			nextden /= gcd;
		}
		nextnum ++;
		if(nextnum % n == 0)
			nextnum ++;
		if(nextnum >= nextden) {
			nextnum = 1;
			nextden *= n;
		}
		numerator = nextnum;
		denominator = nextden;
		return this;
	}
	
	/**
	 * Add other to this and return the result
	 * @param other
	 * @return
	 */
	public Fraction add(Fraction other) {
		if(other.asReal() < 0) {
			throw new RuntimeException("Other  negative " + other);
		}
		if(asReal() < 0) {
			throw new RuntimeException("I'm negative " + this);
		}
		long gcd = GCD(denominator, other.denominator);
		long num = numerator * other.denominator / gcd + other.numerator * denominator / gcd;
		long den = denominator * other.denominator / gcd;
		return new Fraction(num, den);
	}
	
	/**
	 * Subtract other from this
	 * @param other
	 * @return The new fraction with the result
	 */
	public Fraction sub(Fraction other) {
		int num = numerator * other.denominator - other.numerator * denominator;
		int den = denominator * other.denominator;
		return new Fraction(num, den);
	}
	
	/**
	 * 
	 * @return This as a double
	 */
	public double asReal() {
		return (double)numerator / denominator;
	}
	
	public Fraction clone() {
		return new Fraction(numerator, denominator);
	}
	
	public int compareTo(Fraction other) {
		int numme = numerator * other.denominator;
		int numot = other.numerator * denominator;
		return numme - numot;
	}
	
	public boolean equals(Object other) {
		if(other == null)
			return false;
		if(other instanceof Integer || other instanceof Long || other instanceof Short || other instanceof Byte)
			return compareTo(new Fraction((int)other, 1)) == 0;
		if(other instanceof Float || other instanceof Double)
			return (float) numerator / denominator == (float)other;
		return compareTo((Fraction)other) == 0;
	}
	
	public String toString() {
		return numerator + "/" + denominator;
	}
	
	/**
	 * 
	 * @param a
	 * @param b
	 * @return The Greatest Common Divisor of a and b
	 */
	public static long GCD(long a, long b) {
		long r0 = a, r1 = b;
		while(r1 != 0) {
			long tmp = r0 % r1;
			r0 = r1;
			r1 = tmp;
		}
		return r0;
	}

}
