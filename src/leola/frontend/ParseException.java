/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.frontend;

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
    
    public ParseException() {
        this(ErrorCode.UNKNOWN_ERROR);
    }
    
    /**
     * @param errorCode
     */
    public ParseException(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * @param message
     */
    public ParseException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * @param cause
     */
    public ParseException(ErrorCode errorCode, Throwable cause) {
        super(cause);
        this.errorCode = errorCode;
    }

    /**
     * @param message
     * @param cause
     */
    public ParseException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    /**
     * @param message
     */
    public ParseException(String message) {
        this(ErrorCode.UNKNOWN_ERROR, message);        
    }

    /**
     * @param cause
     */
    public ParseException(Throwable cause) {
        this(ErrorCode.UNKNOWN_ERROR,cause);
    }

    /**
     * @param message
     * @param cause
     */
    public ParseException(String message, Throwable cause) {
        this(ErrorCode.UNKNOWN_ERROR,message, cause);        
    }

    /**
     * @return the errorCode
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }
}

