/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.vm.compiler;

import leola.vm.Scope;
import leola.vm.util.ArrayUtil;

/**
 * Represents local variables within a {@link Scope}.  Used for compilation to determine the 
 * the amount of stack space needed for local variables.
 * 
 * <p>
 * Local variables within a {@link Scope} follow proper lexical scoping by overriding out
 * of scope values.  That is, the {@link Locals#getNumberOfLocals()} pool will only be the
 * max number of visible variables within a {@link Scope}.
 * 
 * @author Tony
 *
 */
public class Locals {

    /**
     * The pool of reference symbols
     */
	private String[] pool;	
	
	/**
	 * Current index to store in the pool
	 */
	private int index;
	
	/**
	 * Total number of locals defined
	 * within the scope
	 */
	private int numberOfLocals;
		
	public Locals() {		
		this.index = 0;
	}
	
	/**
	 * Retrieve the index at which the supplied reference lives in the
	 * pool.
	 * 
	 * @param reference
	 * @return the index at which the reference is stored.  If not found, -1.
	 */
	private int getFromPool(String reference) {
		if(pool==null) {
			pool = ArrayUtil.newStringArray();
			return -1;
		}
		
		int index = -1;
		for(int i = 0; i < this.index; i++) {
			if ( this.pool[i].equals(reference) ) {
				index = i;
				break;
			}
		}
		
		return index;
	}
	
	/**
	 * Stores the reference at the supplied index in the pool
	 * 
	 * @param reference
	 * @param index
	 */
	private void putInPool(String reference, int index) {
		if ( index >= this.pool.length ) {
			this.pool = ArrayUtil.resize(pool, pool.length  << 1 );
		}
		
		this.pool[index] = reference;
	}
	
	
	/**
	 * Allocates the number of local variables stored 
	 * 
	 * @param size
	 */
	public void allocate(int size) {
		if ( this.pool == null ) {	
			this.pool = new String[size];
		}				
		this.index = 0;
		this.numberOfLocals = size;
	}
		
	
	/**
	 * Stores in the pool
	 * 
	 * @param reference
	 * @return the index within the pool in which the reference is stored.
	 */
	public int store(String reference) {
		
		int index = getFromPool(reference);
		
		/* if this is a scoped object, put the reference in
		 * the values array
		 */
		
		if ( index < 0 ) {			
			putInPool(reference, this.index);					
		}									
		else {
			return index;
		}
		
		int result = this.index++;
		this.numberOfLocals = Math.max(this.index, this.numberOfLocals);
		
		return result;
	}
	
	/**
	 * Retrieves the reference by index
	 * @param index
	 * @return the reference stored and the supplied index
	 */
	public String getReference(int index) {
		return this.pool[index];
	}
	
	/**
	 * Sets the reference at the specified index
	 * @param index
	 * @param reference
	 */
	public void setReference(int index, String reference) {
		this.pool[index] = reference;
	}
	
	/**
	 * Retrieves the index at which the supplied reference is stored.
	 * 
	 * @param reference
	 * @return -1 if not found, otherwise the index
	 */
	public int get(String reference) {	
		int index = getFromPool(reference);
		return index;		
	}
			
	/**
	 * @return the number of local variables.
	 */
	public int getNumberOfLocals() {
		return this.numberOfLocals;
	}
	
	/**
	 * Used for reseting the index for lexical scoping.
	 * 
	 * @param index
	 */
	public void setIndex(int prevIndex) {
		for(int i = prevIndex; i < this.index; i++) {
			if(this.pool!=null ) this.pool[i] = null;
		}
		this.index = prevIndex;
	}
	
	/**
	 * @return the current index value
	 */
	public int getIndex() {
		return this.index;
	}
		
	@Override
	public Locals clone() {
		Locals clone = new Locals();
		clone.index = this.index;

		if ( this.pool != null ) {
			clone.pool = new String[this.pool.length];
			System.arraycopy(this.pool, 0, clone.pool, 0, this.pool.length);
		}
		return clone;
	}
}

