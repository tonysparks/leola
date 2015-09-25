/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.vm.lib;

import leola.vm.Leola;
import leola.vm.exceptions.LeolaRuntimeException;
import leola.vm.types.LeoNamespace;

/**
 * A library module to be loaded for the interpreter.
 * 
 * @author Tony
 */
public interface LeolaLibrary {

	/**
	 * Initializes the library.
	 * 
	 * @param leola
	 * @throws LeolaRuntimeException
	 */
	@LeolaIgnore
	public void init(Leola leola, LeoNamespace namespace) throws LeolaRuntimeException;
}

