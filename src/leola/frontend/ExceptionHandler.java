/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.frontend;

import leola.frontend.tokens.LeolaErrorCode;

/**
 * Callback interface for handling error either during execution or parsing stages.
 * 
 * @author Tony
 *
 */
public interface ExceptionHandler {

    /**
     * The number of errors that have been encountered.
     * 
     * @return the number of errors that have occurred.
     */
    public int getErrorCount();
    
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

