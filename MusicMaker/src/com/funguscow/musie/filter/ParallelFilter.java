package com.funguscow.musie.filter;

import java.util.ArrayList;
import java.util.List;

/**
 * Filter running individual filters in parallel
 * @author alpac
 *
 */
public class ParallelFilter implements Filter {
	
	/**
	 * A helper class for filter-weight pairs
	 * @author alpac
	 *
	 */
	public static class FilterPair {
		public Filter filter;
		public double weight;
		public FilterPair(Filter filter, double weight) {
			this.filter = filter;
			this.weight = weight;
		}
	}
	
	private List<FilterPair> filters;
	
	public ParallelFilter() {
		filters = new ArrayList<FilterPair>();
	}
	
	/**
	 * Adds a filter f with corresponding weight w
	 * @param f
	 * @param w
	 * @return this
	 */
	public ParallelFilter addFilter(Filter f, double w) {
		filters.add(new FilterPair (f, w));
		return this;
	}
	
	/**
	 * 
	 * @param i
	 * @return The i-th filter
	 */
	public Filter getFilter(int i) {
		return filters.get(i).filter;
	}
	
	public double filter(double input) {
		double output = 0;
		for(FilterPair fpair : filters) {
			output += fpair.weight * fpair.filter.filter(input);
		}
		return output;
	}
	
	public void reset() {
		for(FilterPair fpair : filters) {
			fpair.filter.reset();
		}
	}

}
