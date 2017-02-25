/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.frontend;

import leola.ast.ASTNode;

/**
 * An evaluation exception, means that a bit of code was not able to be executed as intended.
 *
 * @author Tony
 *
 */
public class EvalException extends RuntimeException {

    /**
     * SUID
     */
    private static final long serialVersionUID = 159371812953160598L;

    /**
     *
     */
    public EvalException() {
    }

    /**
     * @param message
     */
    public EvalException(ASTNode node, String message) {
        super(message + " at line: " + node.getLineNumber());
    }

    /**
     * @param message
     */
    public EvalException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public EvalException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public EvalException(String message, Throwable cause) {
        super(message, cause);
    }

}

