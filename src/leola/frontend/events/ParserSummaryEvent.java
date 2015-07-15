/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.frontend.events;

import leola.frontend.listener.Event;

/**
 * Summary of parsing the code event.
 * 
 * @author Tony
 *
 */
public class ParserSummaryEvent extends Event {

	private int totalLines;
	private int errorCount;
	private float elapsedTime;
	
	/**
	 * @return the totalLines
	 */
	public int getTotalLines() {
		return totalLines;
	}

	/**
	 * @return the errorCount
	 */
	public int getErrorCount() {
		return errorCount;
	}

	/**
	 * @return the elapsedTime
	 */
	public float getElapsedTime() {
		return elapsedTime;
	}

	/**
	 * @param source
	 * @param totalLines
	 * @param errorCount
	 * @param elapsedTime
	 */
	public ParserSummaryEvent(Object source, int totalLines, int errorCount,
			float elapsedTime) {
		super(source);
		this.totalLines = totalLines;
		this.errorCount = errorCount;
		this.elapsedTime = elapsedTime;
	}

	
	
}

