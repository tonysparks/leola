/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.vm.debug;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import leola.vm.types.LeoObject;

/**
 * @author Tony
 *
 */
public class SimpleStdOutDebugListener implements DebugListener {

	private boolean echo;
	private Set<Integer> breakpoints;
	private PrintStream out;
	private Scanner input;
	
	public SimpleStdOutDebugListener(PrintStream pstream, InputStream istream) {
		this.out = pstream;
		this.input = new Scanner(istream);
		
		
		this.breakpoints = new HashSet<Integer>();
		this.echo = true;
	}
	
	/**
	 * Pipes to System.out
	 */
	public SimpleStdOutDebugListener() {
		this(System.out, System.in);
	}
	
	/* (non-Javadoc)
	 * @see leola.vm.debug.DebugListener#onLineNumber(leola.vm.debug.DebugEvent)
	 */
	@Override
	public void onLineNumber(DebugEvent event) {
		int lineNumber = event.getLineNumber();
		
		if(isEcho()) {
			out.println("Line: " + lineNumber);
			debugPrint(event.getStack(), event.getBase(), event.getStackPointer(), event.getTopStack());
		}
		
		if(this.breakpoints.contains(lineNumber)) {
			this.input.hasNext();
			this.input.next();
		}
	}

	/**
	 * Print out each line to standard out
	 * 
	 * @param echo the echo to set
	 */
	public void setEcho(boolean echo) {
		this.echo = echo;
	}
	
	/**
	 * @return the echo
	 */
	public boolean isEcho() {
		return echo;
	}
	
	
	public void addBreakpoint(int lineNumber) {
		this.breakpoints.add(lineNumber);
	}
	
	public void removeBreakpoint(int lineNumber) {
		this.breakpoints.remove(new Integer(lineNumber));
	}
	
	public void removeAllBreakpoints() {
		this.breakpoints.clear();
	}
	
	
	/**
	 * Prints the contents of the stack
	 * 
	 * @param stack
	 * @param base
	 * @param top
	 */
	private void printStack(LeoObject[] stack, int base, int top) {
		for(int i = base; i < top; i++) {
			LeoObject obj = stack[i];
			if ( obj == null ) {
				out.println("\t\tType: <empty> Value: <empty>");
			}
			else {
				out.println("\t\tType: " + obj.getType() + " Value: " + obj.toString());
			}
		}
	}
	
	/**
	 * Prints the locals
	 * @param stack
	 * @param base
	 * @param startOfStack
	 */
	private void printLocals(LeoObject[] stack, int base, int startOfStack) {
		printStack(stack, base, startOfStack);
	}
	
	/**
	 * Prints the locals and stack values
	 * 
	 * @param stack
	 * @param base
	 * @param top
	 * @param topStack
	 */	
	private void debugPrint(LeoObject[] stack, int base, int top, int topStack) {		
		out.println("\tLocals {");
		printLocals(stack, base, topStack);
		out.println("\t}");
		out.println("\tStack {");
		printStack(stack, topStack, top);
		out.println("\t}");
		out.println("");
	}
}
