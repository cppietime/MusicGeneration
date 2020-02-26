package com.funguscow.musie.structure;

import java.util.List;

/**
 * Abstract module, a bar or segment
 * @author alpac
 *
 */
public abstract class Module implements Cloneable {
	
	protected int length, depth;
	protected Motif motif;
	protected Attractor[] gen;
	
	public Module(int depth, Motif motif, Attractor[] gen) {
		this.depth = depth;
		this.motif = motif;
		this.gen = gen;
	}

	public int getLength() {
		return length;
	}
	
	public int getDepth() {
		return depth;
	}
	
	public void render(List<NoteMessage> msgs, int offset) {
	}
	
	public void mutate() {
	}
	
	/**
	 * 
	 * @return The maximum number of notes playing at one time
	 */
	public abstract int getMaxNotes();
	
	public abstract Module clone();
	
}
