/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.vm.asm;

/**
 * Describes an {@link Outer} i.e., where it can be found
 * @author Tony
 *
 */
public class OuterDesc {	
	
	private int	index;
	private int up;
	/**
	 * @param index
	 * @param up
	 */
	public OuterDesc(int index, int up) {		
		this.index = index;
		this.up = up;
	}
	
	/**
	 * @return the index
	 */
	public int getIndex() {
		return index;
	}
	
	/**
	 * @return the up
	 */
	public int getUp() {
		return up;
	}
	
}

