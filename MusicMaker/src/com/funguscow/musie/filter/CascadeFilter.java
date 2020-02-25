package com.funguscow.musie.filter;

import java.util.ArrayList;
import java.util.List;

/**
 * Cascading of individual filters
 * @author alpac
 *
 */
public class CascadeFilter implements Filter {
	
	private List<Filter> filters;
	
	public CascadeFilter() {
		filters = new ArrayList<Filter>();
	}
	
	/**
	 * Append filter f to end of filter cascade
	 * @param f
	 * @return this
	 */
	public CascadeFilter addFilter(Filter f) {
		filters.add(f);
		return this;
	}
	
	/**
	 * 
	 * @param i
	 * @return The i-th filter
	 */
	public Filter getFilter(int i) {
		return filters.get(i);
	}
	
	public double filter(double input) {
		double sample = input;
		for(Filter filter : filters) {
			sample = filter.filter(sample);
		}
		return sample;
	}

}
