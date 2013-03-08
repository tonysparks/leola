/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.frontend;

import leola.frontend.tokens.LeolaErrorCode;

/**
 * @author Tony
 *
 */
public interface ExceptionHandler {

	/**
	 * Handles an exception.
	 * 
	 * @param e
	 */
	public void onException(Exception e);
	
	/**
	 * Handles a erroneous token.
	 * 
	 * @param token
	 * @param parser
	 * @param errorCode
	 */
	public void errorToken(Token token, Parser parser, LeolaErrorCode errorCode);
}

