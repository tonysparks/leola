package leola.frontend.tokens;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import leola.ast.BinaryExpr.BinaryOp;
import leola.frontend.ParseException;
import leola.frontend.TokenType;

/**
 * Leola token types.
 * 
 * @author Tony
 *
 */
public enum LeolaTokenType implements TokenType
{
    // Reserved words.    
    SWITCH, 
    CASE, WHEN, RETURN, ELSE, NAMESPACE, DEF, GEN, 
    YIELD, IF, NULL, VAR, WHILE, IS, CATCH,
    NEW, TRUE, FALSE, BREAK, CONTINUE, THROW, TRY, FINALLY,
    CLASS,
    // end Reserved words

    // Special symbols.
    PLUS("+"), MINUS("-"), STAR("*"), MOD("%"), SLASH("/"), D_EQUALS("=="), REF_EQUALS("==="),
    DOT("."), VAR_ARGS("..."), AT("@"), COMMA(","), SEMICOLON(";"), COLON(":"),
    EQUALS("="), NOT_EQUALS("!="), LESS_THAN("<"), LESS_EQUALS("<="),
    GREATER_EQUALS(">="), GREATER_THAN(">"), LEFT_PAREN("("), RIGHT_PAREN(")"),
    LEFT_BRACKET("["), RIGHT_BRACKET("]"), LEFT_BRACE("{"), RIGHT_BRACE("}"),
    LOGICAL_OR("||"), LOGICAL_AND("&&"), NOT("!"), DQUOTE("\""), ARROW("->"),
    BIT_SHIFT_LEFT("<<"), BIT_SHIFT_RIGHT(">>"),

    // Binary Assignment operators
    PLUS_EQ("+="), MINUS_EQ("-="), STAR_EQ("*="), SLASH_EQ("/="), MOD_EQ("%="),
    BSL_EQ("<<="), BSR_EQ(">>="), BOR_EQ("|="), BAND_EQ("&="), BXOR_EQ("^="),
    // end Binary Assignment operators

    BITWISE_NOT("~"), BITWISE_OR("|"), BITWISE_AND("&"), BITWISE_XOR("^"),
    // end Special symbols

    IDENTIFIER, INTEGER, LONG, REAL, STRING,
    ERROR, END_OF_FILE;

    private static final int FIRST_RESERVED_INDEX = SWITCH.ordinal();
    private static final int LAST_RESERVED_INDEX  = CLASS.ordinal();

    private static final int FIRST_SPECIAL_INDEX = PLUS.ordinal();
    private static final int LAST_SPECIAL_INDEX  = BITWISE_XOR.ordinal();

    private static final int FIRST_BIN_ASSGN_INDEX = PLUS_EQ.ordinal();
    private static final int LAST_BIN_ASSGN_INDEX = BXOR_EQ.ordinal();

    private String text;  // token text

    /**
     */
    LeolaTokenType() {
        this.text = this.toString().toLowerCase();
    }

    /**
     * @param text the token text.
     */
    LeolaTokenType(String text) {
        this.text = text;
    }

    /**
     * To a {@link BinaryOp}
     * @return the {@link BinaryOp} representation
     */
    public BinaryOp toBinaryOp() {
    	BinaryOp result = null;
    	switch(this) {
    	case BITWISE_AND:
    	case BAND_EQ:
    		result = BinaryOp.BIT_AND;
    		break;
    	case BITWISE_OR:
    	case BOR_EQ:
    		result = BinaryOp.BIT_OR;
    		break;
    	case BITWISE_XOR:
    	case BXOR_EQ:
    		result = BinaryOp.BIT_XOR;
    		break;
    	case LOGICAL_AND:
    		result = BinaryOp.AND;
    		break;
    	case LOGICAL_OR:
    		result = BinaryOp.OR;
    		break;
    	case MINUS:
    	case MINUS_EQ:
    		result = BinaryOp.SUB;
    		break;
    	case PLUS:
    	case PLUS_EQ:
    		result = BinaryOp.ADD;
    		break;
    	case STAR:
    	case STAR_EQ:
    		result = BinaryOp.MUL;
    		break;
    	case SLASH:
    	case SLASH_EQ:
    		result = BinaryOp.DIV;
    		break;
    	case MOD:
    	case MOD_EQ:
    		result = BinaryOp.MOD;
    		break;
    	case REF_EQUALS: {
    		result = BinaryOp.REQ;
    		break;
    	}
    	case D_EQUALS:
    		result = BinaryOp.EQ;
    		break;
    	case GREATER_EQUALS:
    		result = BinaryOp.GTE;
    		break;
    	case GREATER_THAN:
    		result = BinaryOp.GT;
    		break;
    	case LESS_EQUALS:
    		result = BinaryOp.LTE;
    		break;
    	case LESS_THAN:
    		result = BinaryOp.LT;
    		break;
    	case NOT_EQUALS:
    		result = BinaryOp.NEQ;
    		break;
    	case BIT_SHIFT_LEFT:
    	case BSL_EQ:
    		result = BinaryOp.BIT_SHIFT_LEFT;
    		break;
    	case BIT_SHIFT_RIGHT:
    	case BSR_EQ:
    		result = BinaryOp.BIT_SHIFT_RIGHT;
    		break;
    	default:
    		throw new IllegalArgumentException(this + " is not a legal BinaryOp");
    	}
    	return result;
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
    			(LeolaErrorCode.INVALID_NUMBER, "Unable to parse: " + this.text + " as a number.", e);
    	}
    }

    // Set of lower-cased Leola reserved word text strings.
    public static Set<String> RESERVED_WORDS = new HashSet<String>();
    static {
        LeolaTokenType values[] = LeolaTokenType.values();
        for (int i = FIRST_RESERVED_INDEX; i <= LAST_RESERVED_INDEX; ++i) {
            RESERVED_WORDS.add(values[i].getText().toLowerCase());
        }
    }

    // Hash table of Leola special symbols.  Each special symbol's text
    // is the key to its Leola token type.
    public static Map<String, LeolaTokenType> SPECIAL_SYMBOLS = new HashMap<String, LeolaTokenType>();
    static {
        LeolaTokenType values[] = LeolaTokenType.values();
        for (int i = FIRST_SPECIAL_INDEX; i <= LAST_SPECIAL_INDEX; ++i) {
            SPECIAL_SYMBOLS.put(values[i].getText(), values[i]);
        }
    }

    /**
     * Binary Assignment operators
     */
    public static Map<String, LeolaTokenType> BINARY_ASSIGNMENT = new HashMap<String, LeolaTokenType>();
    static {
    	LeolaTokenType values[] = LeolaTokenType.values();
        for (int i = FIRST_BIN_ASSGN_INDEX; i <= LAST_BIN_ASSGN_INDEX; ++i) {
            BINARY_ASSIGNMENT.put(values[i].getText(), values[i]);
        }
    }
}
