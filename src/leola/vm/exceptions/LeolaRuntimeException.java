/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.vm.exceptions;

import leola.vm.types.LeoError;
import leola.vm.types.LeoObject;


/**
 * An evaluation exception, means that a bit of code was not able to be executed as intended.
 *
 * @author Tony
 *
 */
public class LeolaRuntimeException extends RuntimeException {

    /**
     * SUID
     */
    private static final long serialVersionUID = 159371812953160598L;

    private LeoError leoError;
    
    /**
     *
     */
    public LeolaRuntimeException() {
        this(new LeoError());
    }
    
    /**
     * @param error
     */
    public LeolaRuntimeException(LeoObject error) {
        if(error==null) {
            error = new LeoError();
        }
        else if (!error.isError()) {
            error = new LeoError(error);
        }
        
        this.leoError = error.as();
    }

    /**
     * @param message
     */
    public LeolaRuntimeException(String message) {
        super(message);
        this.leoError = new LeoError(message);
    }

    /**
     * @param cause
     */
    public LeolaRuntimeException(Throwable cause) {
        super(cause);
        this.leoError = new LeoError(cause.getMessage());
    }

    /**
     * @param message
     * @param cause
     */
    public LeolaRuntimeException(String message, Throwable cause) {
        super(message, cause);
        this.leoError = new LeoError(message);
    }

    /* (non-Javadoc)
     * @see java.lang.Throwable#getMessage()
     */
    @Override
    public String getMessage() {    
        return this.leoError.toString();
    }
    
    /**
     * @return the leoError
     */
    public LeoError getLeoError() {
        return leoError;
    }
}

