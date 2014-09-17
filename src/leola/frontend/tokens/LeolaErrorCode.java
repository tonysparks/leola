package leola.frontend.tokens;

/**
 * <h1>PascalErrorCode</h1>
 *
 * <p>Pascal translation error codes.</p>
 *
 * <p>Copyright (c) 2009 by Ronald Mak</p>
 * <p>For instructional purposes only.  No warranties.</p>
 */
public enum LeolaErrorCode
{
    ALREADY_FORWARDED("Already specified in FORWARD"),
    CASE_CONSTANT_REUSED("CASE constant reused"),
    IDENTIFIER_REDEFINED("Redefined identifier"),
    IDENTIFIER_UNDEFINED("Undefined identifier"),
    INCOMPATIBLE_ASSIGNMENT("Incompatible assignment"),
    INCOMPATIBLE_TYPES("Incompatible types"),
    INVALID_ASSIGNMENT("Invalid assignment statement"),
    INVALID_CHARACTER("Invalid character"),
    INVALID_CONSTANT("Invalid constant"),
    INVALID_EXPONENT("Invalid exponent"),
    INVALID_EXPRESSION("Invalid expression"),
    INVALID_FIELD("Invalid field"),
    INVALID_FRACTION("Invalid fraction"),
    INVALID_IDENTIFIER_USAGE("Invalid identifier usage"),
    INVALID_INDEX_TYPE("Invalid index type"),
    INVALID_NUMBER("Invalid number"),
    INVALID_STATEMENT("Invalid statement"),
    INVALID_SUBRANGE_TYPE("Invalid subrange type"),
    INVALID_TARGET("Invalid assignment target"),
    INVALID_TYPE("Invalid type"),
    INVALID_VAR_PARM("Invalid VAR parameter"),
    INVALID_VAR_ARGS_START("Invalid variable argument; must have variable name"),
    INVALID_VAR_ARGS("Invalid variable argument; must be last argument"),
    MIN_GT_MAX("Min limit greater than max limit"),
    MISSING_ARROW("Missing ->"),
    MISSING_BEGIN("Missing BEGIN"),
    MISSING_COLON("Missing :"),
    MISSING_COLON_EQUALS("Missing :="),
    MISSING_COMMA("Missing ,"),
    MISSING_CONSTANT("Missing constant"),
    MISSING_DO("Missing DO"),
    MISSING_DOT_DOT("Missing .."),
    MISSING_RIGHT_BRACE("Missing Right Brace"),
    MISSING_EQUALS("Missing ="),
    MISSING_FOR_CONTROL("Invalid FOR control variable"),
    MISSING_IDENTIFIER("Missing identifier"),
    MISSING_LEFT_BRACKET("Missing ["),
    MISSING_OF("Missing OF"),
    MISSING_PERIOD("Missing ."),
    MISSING_PROGRAM("Missing PROGRAM"),
    MISSING_RIGHT_BRACKET("Missing ]"),
    MISSING_RIGHT_PAREN("Missing )"),
    MISSING_LEFT_PAREN("Missing ("),
    MISSING_LEFT_BRACE("Missing {"),
    MISSING_SEMICOLON("Missing ;"),
    MISSING_FILE_NAME("Missing \"<filename>\""),
    MISSING_THEN("Missing THEN"),
    MISSING_TO_DOWNTO("Missing TO or DOWNTO"),
    MISSING_UNTIL("Missing UNTIL"),
    MISSING_VARIABLE("Missing variable"),
    MISSING_CATCH_OR_FINALLY("Missing a 'catch' or 'finally' block"),
    NOT_CONSTANT_IDENTIFIER("Not a constant identifier"),
    NOT_RECORD_VARIABLE("Not a record variable"),
    NOT_TYPE_IDENTIFIER("Not a type identifier"),
    RANGE_INTEGER("Integer literal out of range"),
    RANGE_LONG("Long literal out of range"),
    RANGE_REAL("Real literal out of range"),
    STACK_OVERFLOW("Stack overflow"),
    TOO_MANY_LEVELS("Nesting level too deep"),
    TOO_MANY_SUBSCRIPTS("Too many subscripts"),
    UNEXPECTED_EOF("Unexpected end of file"),
    UNEXPECTED_TOKEN("Unexpected token"),
    UNIMPLEMENTED("Unimplemented feature"),
    UNRECOGNIZABLE("Unrecognizable input"),
    WRONG_NUMBER_OF_PARMS("Wrong number of actual parameters"),

    UNKNOWN_ERROR("An unknown error occured"),

    // Fatal errors.
    IO_ERROR(-101, "Object I/O error"),
    TOO_MANY_ERRORS(-102, "Too many syntax errors");

    private int status;      // exit status
    private String message;  // error message

    /**
     * Constructor.
     * @param message the error message.
     */
    LeolaErrorCode(String message)
    {
        this.status = 0;
        this.message = message;
    }

    /**
     * Constructor.
     * @param status the exit status.
     * @param message the error message.
     */
    LeolaErrorCode(int status, String message)
    {
        this.status = status;
        this.message = message;
    }

    /**
     * Getter.
     * @return the exit status.
     */
    public int getStatus()
    {
        return status;
    }

    /**
     * @return the message.
     */
    @Override
	public String toString()
    {
        return message;
    }
}
