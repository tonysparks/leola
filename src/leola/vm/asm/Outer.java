/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.vm.asm;

import leola.vm.types.LeoObject;

/**
 * An {@link Outer} references a value off of the stack.  These are created via Closures.
 * 
 * @author Tony
 *
 */
public class Outer {

	private LeoObject[] stack;
	private int index;
	
	/**
	 * @param stack
	 * @param index
	 */
	public Outer(LeoObject[] stack, int index) {
		this.stack = stack;
		this.index = index;
	}
	

	
	/**
	 * @return the index
	 */
	public int getIndex() {
		return index;
	}
	
	/**
	 * Sets the value
	 * @param value
	 */
	public void setValue(LeoObject value) {		
		stack[index] = value;
	}
	
	/**
	 * @return the value
	 */
	public LeoObject getValue() {
		return stack[index];
	}
	
	/**
	 * The value is no longer on the stack,
	 * so therefore we take the current value and store
	 * it.  This value is now "closed" upon for a closure.
	 */
	public void close() {
		this.stack = new LeoObject[] {
			this.stack[this.index]
		};
		this.index = 0;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getValue().toString();
	}
}

