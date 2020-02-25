package com.funguscow.musie.instrument;

import com.funguscow.musie.filter.*;

public interface Effectable<T> {

	public T addEffect(Filter filter);
	
}
