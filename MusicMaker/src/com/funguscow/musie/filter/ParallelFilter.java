package com.funguscow.musie.filter;

import java.util.ArrayList;
import java.util.List;

/**
 * Filter running individual filters in parallel
 * @author alpac
 *
 */
public class ParallelFilter implements Filter {
	
	private List<Object[]> filters;
	
	public ParallelFilter() {
		filters = new ArrayList<Object[]>();
	}
	
	/**
	 * Adds a filter f with corresponding weight w
	 * @param f
	 * @param w
	 * @return this
	 */
	public ParallelFilter addFilter(Filter f, double w) {
		filters.add(new Object[] {f, w});
		return this;
	}
	
	/**
	 * 
	 * @param i
	 * @return The i-th filter
	 */
	public Object[] getFilter(int i) {
		return filters.get(i);
	}
	
	public double filter(double input) {
		double output = 0;
		for(Object[] fpair : filters) {
			output += (double)(fpair[1]) * ((Filter)(fpair[0])).filter(input);
		}
		return output;
	}

}
