/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.vm.compiler;

/**
 * Describes an {@link Outer} i.e., where it can be found.
 * 
 * @author Tony
 *
 */
public class OuterDesc {	
	
    /**
     * The index on the stack
     * of where this variable lives
     */
	private int	index;
	
	/**
	 * Number of scopes we have to navigate
	 * up to, in order to reach the local
	 * stack in which this variable lives
	 */
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
	 * Sets the index on the stack of where the 
	 * variable can be found.
	 * 
	 * @param index
	 */
	public void setIndex(int index) {
	    this.index = index;
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

