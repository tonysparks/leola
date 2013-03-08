/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.frontend.events;

import leola.frontend.listener.Event;

/**
 * A line of source code has been read.
 * 
 * @author Tony
 *
 */
public class SourceLineEvent extends Event {
	private String line;
	private int lineNumber;
	/**
	 * @param source
	 * @param line
	 * @param lineNumber
	 */
	public SourceLineEvent(Object source, String line, int lineNumber) {
		super(source);
		this.line = line;
		this.lineNumber = lineNumber;
	}

	/**
	 * @return the line
	 */
	public String getLine() {
		return line;
	}
	
	/**
	 * @return the lineNumber
	 */
	public int getLineNumber() {
		return lineNumber;
	}
	
}

