/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.frontend.events;

import leola.frontend.Source;
import leola.frontend.Token;
import leola.frontend.listener.Event;

/**
 * @author Tony
 *
 */
public class SyntaxErrorEvent extends Event {

    private int lineNumber;
    private int position;
    private String tokenText;
    private String errorMessage;
    private Source sourceCode;
	
    /**
     * @param source
     * @param sourceCode
     * @param token
     * @param errorMessage
     */
	public SyntaxErrorEvent(Object source, Source sourceCode, Token token, String errorMessage) {
		super(source);
		this.sourceCode = sourceCode;
		
		this.lineNumber = token.getLineNumber();
		this.position = token.getPosition();
		this.tokenText = token.getText();
		
		this.errorMessage = errorMessage;
	}
	
	/**
	 * @return the sourceCode
	 */
	public Source getSourceCode() {
		return sourceCode;
	}
	
	/**
	 * @return the lineNumber
	 */
	public int getLineNumber() {
		return lineNumber;
	}
	/**
	 * @param lineNumber the lineNumber to set
	 */
	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}
	/**
	 * @return the position
	 */
	public int getPosition() {
		return position;
	}
	/**
	 * @param position the position to set
	 */
	public void setPosition(int position) {
		this.position = position;
	}
	/**
	 * @return the tokenText
	 */
	public String getTokenText() {
		return tokenText;
	}
	/**
	 * @param tokenText the tokenText to set
	 */
	public void setTokenText(String tokenText) {
		this.tokenText = tokenText;
	}
	/**
	 * @return the errorMessage
	 */
	public String getErrorMessage() {
		return errorMessage;
	}
	/**
	 * @param errorMessage the errorMessage to set
	 */
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
    
    
}

