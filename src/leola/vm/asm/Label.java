/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.vm.asm;

import java.util.Stack;

/**
 * A Label to jmp instructions
 * 
 * @author Tony
 *
 */
public class Label {
	private int index;
	private Stack<Long> deltas;
	private Asm asm;
	
	public Label(Asm asm) {
		this.asm = asm;
		this.index = -1;
		this.deltas = new Stack<Long>();
	}
	
	/**
	 * Sets the location of the label
	 */
	public void set() {
		this.index = asm.getInstructionSize(); 
	}
	
	/**
	 * @return the index in the instruction list where
	 * this label is defined
	 */
	public int getLabelInstructionIndex() {
		return index;
	}
	
	/**
	 * @return the deltas
	 */
	public Stack<Long> getDeltas() {
		return deltas;
	}
	
	/**
	 * Marks an occurrence of the label (later calculates the
	 * jmp delta)
	 * 
	 * @param opcode
	 */
	public void mark(int opcode) {
		int instrIndex = this.asm.getInstructionSize() - 1;
		int instr = opcode;
		
		long delta = instrIndex;
		delta = delta << 32 | instr;
		
		this.deltas.push(delta);
	}
}

