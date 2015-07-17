/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.vm.types;

import leola.vm.compiler.Outer;

/**
 * An object that can carry closure values along with it.
 * 
 * @author Tony
 *
 */
public abstract class LeoOuterObject extends LeoObject {

	/**
	 * Dummy var
	 */
	public static final Outer[] NOOUTERS = {};
	
	/**
	 * Any closure values
	 */
	protected Outer[] outers;
	
	/**
	 * @param type
	 */
	public LeoOuterObject(LeoType type, int numberOfOuters) {
		super(type);	
		
		this.outers = numberOfOuters>0 ? new Outer[numberOfOuters] : NOOUTERS;
	}

	/**
	 * @return true
	 */
	public boolean isOuter() {
		return true;
	}
	
	/**
	 * @return the outers
	 */
	public Outer[] getOuters() {
		return outers;
	}
	
}

