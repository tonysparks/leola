package leola.frontend.tokens;

/**
 * Leola error codes
 * 
 * @author Tony
 *
 */
public enum LeolaErrorCode {
    INVALID_ASSIGNMENT("Invalid assignment statement"),
    INVALID_CHARACTER("Invalid character"),
    INVALID_NUMBER("Invalid number"),
    INVALID_VAR_ARGS_START("Invalid variable argument; must have variable name"),
    INVALID_VAR_ARGS("Invalid variable argument; must be last argument"),
    INVALID_MULTI_VAR_ARGS("Invalid variable argument; only one variable argument is allowed"),
    INVALID_ARGS_EXPANSION("Invalid variable argument expansion; can only be used in function arguments"),
    INVALID_MULTI_ARGS_EXPANSION("Invalid variable argument expansion; can only expand one argument"),
    
    
    MISSING_ARROW("Missing ->"),
    MISSING_COMMA("Missing ,"),
    MISSING_RIGHT_BRACE("Missing Right Brace"),
    MISSING_EQUALS("Missing ="),
    MISSING_IDENTIFIER("Missing identifier"),
    MISSING_RIGHT_BRACKET("Missing ]"),
    MISSING_RIGHT_PAREN("Missing )"),
    MISSING_LEFT_PAREN("Missing ("),
    MISSING_LEFT_BRACE("Missing {"),
    MISSING_CATCH_OR_FINALLY("Missing an 'catch' or 'finally' block"),
    MISSING_INTERFACE("Missing interface definition on class."),
    
    RANGE_INTEGER("Integer literal out of range"),
    RANGE_LONG("Long literal out of range"),
    RANGE_REAL("Real literal out of range"),

    UNEXPECTED_EOF("Unexpected end of file"),
    UNEXPECTED_TOKEN("Unexpected token"),
    UNIMPLEMENTED("Unimplemented feature"),

    UNKNOWN_ERROR("An unknown error occured"),

    // Fatal errors.
    //IO_ERROR(-101, "Object I/O error"),
    TOO_MANY_ERRORS(-102, "Too many syntax errors");

    private int status;      // exit status
    private String message;  // error message

    /**
     * Constructor.
     * @param message the error message.
     */
    LeolaErrorCode(String message) {
        this.status = 0;
        this.message = message;
    }

    /**
     * Constructor.
     * @param status the exit status.
     * @param message the error message.
     */
    LeolaErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }

    /**
     * Getter.
     * @return the exit status.
     */
    public int getStatus() {
        return status;
    }

    /**
     * @return the message.
     */
    @Override
	public String toString() {
        return message;
    }
}
