/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.frontend;

import leola.frontend.tokens.LeolaErrorCode;

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
    private LeolaErrorCode errorCode;
    
    public ParseException() {
        this(LeolaErrorCode.UNKNOWN_ERROR);
    }
    
    /**
     * @param errorCode
     */
    public ParseException(LeolaErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * @param message
     */
    public ParseException(LeolaErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * @param cause
     */
    public ParseException(LeolaErrorCode errorCode, Throwable cause) {
        super(cause);
        this.errorCode = errorCode;
    }

    /**
     * @param message
     * @param cause
     */
    public ParseException(LeolaErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    /**
     * @param message
     */
    public ParseException(String message) {
        this(LeolaErrorCode.UNKNOWN_ERROR, message);        
    }

    /**
     * @param cause
     */
    public ParseException(Throwable cause) {
        this(LeolaErrorCode.UNKNOWN_ERROR,cause);
    }

    /**
     * @param message
     * @param cause
     */
    public ParseException(String message, Throwable cause) {
        this(LeolaErrorCode.UNKNOWN_ERROR,message, cause);        
    }

    /**
     * @return the errorCode
     */
    public LeolaErrorCode getErrorCode() {
        return errorCode;
    }
}

