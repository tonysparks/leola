/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.frontend;

import leola.frontend.tokens.LeolaErrorCode;
import leola.vm.exceptions.LeolaRuntimeException;
import leola.vm.types.LeoError;
import leola.vm.types.LeoObject;

/**
 * Callback interface for handling errors either during compilation or parsing stages.  This will not capture/listen
 * for exceptions thrown during runtime calls.
 * 
 * <p>As an example:
 * 
 * <pre>    
 *   Leola runtime = ...
 *   ExceptionHandler myCustomHandler = ... // your custom handler for handling syntax errors
 *   runtime.setExceptionHandler(myCustomHandler);
 *   
 *   
 *   LeoObject function = runtime.eval(new FileReader(scriptFile));
 *   try {
 *      LeoObject result = function.xcall(arg1); // you must catch this exception
 *   }
 *   catch(LeolaRuntimeException e) {
 *    // do something
 *   }  
 * </pre>
 * 
 * You may alternatively deal with runtime exceptions by calling the {@link LeoObject#call()} variants.  These differ
 * from their {@link LeoObject#xcall()} equivalent by not throwing a {@link LeolaRuntimeException}, but instead returning 
 * the {@link LeoError} if there is one.
 * 
 * <p>As an example:
 * 
 * <pre>    
 *   LeoObject function = ...
 *   
 *   LeoObject result = function.xcall(arg1); 
 *   if(result.isError() {
 *      // do something
 *   }
 *     
 * </pre> 
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

