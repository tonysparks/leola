/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.vm.debug;

/**
 * Listens for debug lines
 * 
 * @author chq-tonys
 *
 */
public interface DebugListener {

	/**
	 * A line has been encountered
	 * 
	 * @param event
	 */
	public void onLineNumber(DebugEvent event);
}
