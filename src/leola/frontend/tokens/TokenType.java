package leola.frontend.tokens;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import leola.frontend.ErrorCode;
import leola.frontend.ParseException;

/**
 * Leola token types.
 * 
 * @author Tony
 *
 */
public enum TokenType {
    // Reserved words.    
    SWITCH, 
        CASE, WHEN, RETURN, ELSE, NAMESPACE, DEF, GEN, 
        YIELD, IF, NULL, VAR, WHILE, IS, CATCH,
        NEW, TRUE, FALSE, BREAK, CONTINUE, THROW, TRY, FINALLY,
    CLASS,
    // end Reserved words

    // Special symbols.
    PLUS("+"), 
        MINUS("-"), STAR("*"), MOD("%"), SLASH("/"), D_EQUALS("=="), 
        REF_EQUALS("==="), REF_NOT_EQUALS("!=="),
        DOT("."), VAR_ARGS("..."), AT("@"), QUESTION_MARK("?"), COMMA(","), SEMICOLON(";"), COLON(":"),
        EQUALS("="), NOT_EQUALS("!="), LESS_THAN("<"), LESS_EQUALS("<="),
        GREATER_EQUALS(">="), GREATER_THAN(">"), LEFT_PAREN("("), RIGHT_PAREN(")"),
        LEFT_BRACKET("["), RIGHT_BRACKET("]"), LEFT_BRACE("{"), RIGHT_BRACE("}"),
        LOGICAL_OR("||"), LOGICAL_AND("&&"), NOT("!"), DQUOTE("\""), ARROW("->"), FAT_ARROW("=>"),
        BIT_SHIFT_LEFT("<<"), BIT_SHIFT_RIGHT(">>"),

        // Binary Assignment operators
        PLUS_EQ("+="), 
            MINUS_EQ("-="), STAR_EQ("*="), SLASH_EQ("/="), MOD_EQ("%="),
            BSL_EQ("<<="), BSR_EQ(">>="), BOR_EQ("|="), BAND_EQ("&="), 
        BXOR_EQ("^="),
        // end Binary Assignment operators

        BITWISE_NOT("~"), BITWISE_OR("|"), BITWISE_AND("&"), 
    BITWISE_XOR("^"),
    // end Special symbols

    IDENTIFIER, 
    INTEGER, 
    LONG, 
    REAL, 
    STRING,
    ERROR, 
    END_OF_FILE;

    private static final int FIRST_RESERVED_INDEX = SWITCH.ordinal();
    private static final int LAST_RESERVED_INDEX  = CLASS.ordinal();

    private static final int FIRST_SPECIAL_INDEX = PLUS.ordinal();
    private static final int LAST_SPECIAL_INDEX  = BITWISE_XOR.ordinal();

    private static final int FIRST_BIN_ASSGN_INDEX = PLUS_EQ.ordinal();
    private static final int LAST_BIN_ASSGN_INDEX = BXOR_EQ.ordinal();

    private String text;  // token text

    /**
     */
    TokenType() {
        this.text = this.toString().toLowerCase();
    }

    /**
     * @param text the token text.
     */
    TokenType(String text) {
        this.text = text;
    }

   

    /**
     * @return the token text.
     */
    public String getText() {
        return text;
    }

    /**
     * Returns the text as a number
     * @return the text as a number
     * @throws Exception
     */
    public double getTextAsNumber() throws Exception {
        try {
            return Double.parseDouble(this.text);
        }
        catch(Exception e) {
            throw new ParseException 
                (ErrorCode.INVALID_NUMBER, null, "Unable to parse: " + this.text + " as a number.", e);
        }
    }

    // Set of lower-cased Leola reserved word text strings.
    public static Set<String> RESERVED_WORDS = new HashSet<String>();
    static {
        TokenType values[] = TokenType.values();
        for (int i = FIRST_RESERVED_INDEX; i <= LAST_RESERVED_INDEX; ++i) {
            RESERVED_WORDS.add(values[i].getText().toLowerCase());
        }
    }

    // Hash table of Leola special symbols.  Each special symbol's text
    // is the key to its Leola token type.
    public static Map<String, TokenType> SPECIAL_SYMBOLS = new HashMap<String, TokenType>();
    static {
        TokenType values[] = TokenType.values();
        for (int i = FIRST_SPECIAL_INDEX; i <= LAST_SPECIAL_INDEX; ++i) {
            SPECIAL_SYMBOLS.put(values[i].getText(), values[i]);
        }
    }

    /**
     * Binary Assignment operators
     */
    public static Map<String, TokenType> BINARY_ASSIGNMENT = new HashMap<String, TokenType>();
    static {
        TokenType values[] = TokenType.values();
        for (int i = FIRST_BIN_ASSGN_INDEX; i <= LAST_BIN_ASSGN_INDEX; ++i) {
            BINARY_ASSIGNMENT.put(values[i].getText(), values[i]);
        }
    }
}
