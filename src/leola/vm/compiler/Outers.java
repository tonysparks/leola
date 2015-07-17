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
	private OuterDesc[] lazyouters() {
		if ( this.outers == null ) {
			this.outers = ArrayUtil.newOuterDescArray();	
		}
		
		return this.outers;
	}
		
	public void allocate(int size) {
		if ( this.outers == null ) {
			this.outers = new OuterDesc[size];
		}
		
		if ( size > this.outers.length ) {
			this.outers = ArrayUtil.resize(outers, size );
		}
	}
	
	public OuterDesc get(int index) {
		return this.outers[index];
	}
	
	
	public int getNumberOfOuters() {
		return this.size;
	}
		
	
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
		
		this.outers[this.size] = value;
//		if ( value != null ) {
//			value.setIndex(this.size);
//		}
		
		this.size++;	
		return this.size-1;
	}
	
	
	public OuterDesc[] getOuters() {
		return outers;
	}
}

