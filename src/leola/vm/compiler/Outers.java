/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.vm.compiler;

import leola.vm.Scope;
import leola.vm.util.ArrayUtil;

/**
 * Represents the {@link Outer}s that are stored in a particular {@link Scope}.  
 * 
 * @author Tony
 *
 */
public class Outers {
	
	private OuterDesc[] outers;
	private int size;
		
	/**
	 * @param symbols
	 */
	public Outers() {
		this.size = 0;
	}
	
	/**
	 * This will allocate a new {@link OuterDesc} array if
	 * one hasn't alread been allocated.
	 * 
	 * @return the {@link OuterDesc} array
	 */
	private OuterDesc[] lazyouters() {
		if ( this.outers == null ) {
			this.outers = ArrayUtil.newOuterDescArray();	
		}
		
		return this.outers;
	}
		
	
	/**
	 * Allocate storage for a pre-determined amount of {@link OuterDesc}s
	 * 
	 * @param size the number of entries to allocate for
	 */
	public void allocate(int size) {
		if ( this.outers == null ) {
			this.outers = new OuterDesc[size];
		}
		
		if ( size > this.outers.length ) {
			this.outers = ArrayUtil.resize(outers, size );
		}
	}
	
	
	/**
	 * Get an {@link OuterDesc} by its stored index
	 * 
	 * @param index
	 * @return the {@link OuterDesc} stored in the index
	 */
	public OuterDesc get(int index) {
		return this.outers[index];
	}
	
	
	/**
	 * @return the number of outers
	 */
	public int getNumberOfOuters() {
		return this.size;
	}
		
	/**
	 * Store the {@link OuterDesc}
	 * @param value
	 * @return the index in which this is stored
	 */
	public int store(OuterDesc value) {
		/* first check and see if this outer exists already */
		for(int i = 0; i < this.size; i++) {
			OuterDesc v = this.outers[i];
			if(v!=null) {
				if(v.getIndex()==value.getIndex() &&
				   v.getUp() == value.getUp()) {
					return i;
				}
			}
		}
		
		if ( this.size >= lazyouters().length) {
			outers = ArrayUtil.resize(outers, outers.length << 1);
		}				
		
		this.outers[this.size++] = value;				
		return this.size-1;
	}
}

