/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.frontend;

import leola.frontend.tokens.Token;

/**
 * An exception during parsing
 * 
 * @author Tony
 *
 */
public class ParseException extends RuntimeException {

    /**
     * SUID
     */
    private static final long serialVersionUID = 4773494052623080002L;

    /**
     * Error code
     */
    private ErrorCode errorCode;
    
    /**
     * The token in which we errored on
     */
    private Token token;
    
    public ParseException(Token token) {
        this(ErrorCode.UNKNOWN_ERROR, token);
    }
    
    /**
     * @param errorCode
     */
    public ParseException(ErrorCode errorCode, Token token) {
        this.errorCode = errorCode;
        this.token = token;
    }

    /**
     * @param message
     */
    public ParseException(ErrorCode errorCode, Token token, String message) {
        super(message);
        this.errorCode = errorCode;
        this.token = token;
    }

    /**
     * @param cause
     */
    public ParseException(ErrorCode errorCode, Token token, Throwable cause) {
        super(cause);
        this.errorCode = errorCode;
        this.token = token;
    }

    /**
     * @param message
     * @param cause
     */
    public ParseException(ErrorCode errorCode, Token token, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.token = token;
    }
    
    /**
     * @param message
     */
    public ParseException(Token token, String message) {
        this(ErrorCode.UNKNOWN_ERROR, token, message);        
    }

    /**
     * @param cause
     */
    public ParseException(Token token, Throwable cause) {
        this(ErrorCode.UNKNOWN_ERROR, token, cause);
    }

    /**
     * @param message
     * @param cause
     */
    public ParseException(Token token, String message, Throwable cause) {
        this(ErrorCode.UNKNOWN_ERROR, token, message, cause);        
    }

    /**
     * @return the errorCode
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }
    
    /**
     * @return the current token which the {@link ParseException} occurred
     */
    public Token getToken() {
        return token;
    }
}

