/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.vm.debug;

import leola.vm.Scope;
import leola.vm.compiler.Bytecode;
import leola.vm.compiler.Outer;
import leola.vm.types.LeoObject;

/**
 * @author Tony
 *
 */
public class DebugEvent {

	
	private int base;
	private int topStack;
	private int stackPointer;
	private int programCounter;
	private int lineNumber;
	
	private Scope scope;	
	private LeoObject[] stack;
	private Outer[] calleeouters;	
	

	private Bytecode bytecode;
	
	
	
	/**
	 * @param stack
	 * @param locals
	 * @param lineNumber
	 */
	public DebugEvent(LeoObject[] stack, int base, int topStack, int sp, int pc, int lineNumber, Scope scope, Outer[] calleeouters,
			Bytecode bytecode) {
		super();
		this.stack = stack;		
		this.base = base;
		this.topStack = topStack;
		this.stackPointer = sp;
		this.programCounter = pc;
		
		this.lineNumber = lineNumber;
		
		this.scope = scope;
		this.calleeouters = calleeouters;
		
		this.bytecode = bytecode;		
	}

	/**
	 * @return the stack
	 */
	public LeoObject[] getStack() {
		return stack;
	}

	/**
	 * @return the base
	 */
	public int getBase() {
		return base;
	}
	
	/**
	 * @return the topStack
	 */
	public int getTopStack() {
		return topStack;
	}
	
	/**
	 * @return the stackPointer
	 */
	public int getStackPointer() {
		return stackPointer;
	}
	
	/**
	 * @return the programCounter
	 */
	public int getProgramCounter() {
		return programCounter;
	}
	
	/**
	 * @return the lineNumber
	 */
	public int getLineNumber() {
		return lineNumber;
	}		
	
	/**
	 * @return the calleeouters
	 */
	public Outer[] getCalleeouters() {
		return calleeouters;
	}
	
	/**
	 * @return the scope
	 */
	public Scope getScope() {
		return scope;
	}
	
	/**
	 * @return the bytecode
	 */
	public Bytecode getBytecode() {
		return bytecode;
	}

	@Override
	public String toString() {
	    StringBuilder sb = new StringBuilder();
	    final String format = "%4s %4s %26s %26s\n";
	    sb.append(String.format(format, "Line#","PC","Stack", "Locals"));
	    sb.append("========================================================================\n");
	    sb.append(String.format(format, Integer.toString(this.lineNumber), Integer.toString(this.programCounter), this.stack[base], this.stack[base]));
	    for(int i=base; i < this.stackPointer; i++) {
	        String local = i<this.topStack ? this.stack[i].toString() : "";
	        sb.append(String.format(format, "", "", this.stack[i], local));
	    }
	    return sb.toString();
	}
}
