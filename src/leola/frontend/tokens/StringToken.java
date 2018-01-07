package leola.frontend.tokens;

import static leola.frontend.ErrorCode.UNEXPECTED_EOF;
import static leola.frontend.Source.EOF;
import static leola.frontend.tokens.TokenType.ERROR;
import static leola.frontend.tokens.TokenType.STRING;

import leola.frontend.Source;


/**
 * The String token; which includes normal strings and verbatim strings.
 * 
 * @author Tony
 *
 */
public class StringToken extends Token {
    public static final char STRING_CHAR = '"';
    public static final String MULTI_STRING = "\"\"\"";
    
    /**
     * Constructor.
     * @param source the source from where to fetch the token's characters.
     */
    public StringToken(Source source) {
        super(source);
    }

    /**
     * Extract a Leola string token from the source.
     */
    @Override
    protected void extract() {
        StringBuilder textBuffer = new StringBuilder();
        StringBuilder valueBuffer = new StringBuilder();

        char currentChar = nextChar();  // consume initial quote
        textBuffer.append(STRING_CHAR);

        boolean inBlockString = false;
        boolean isStart = true;
        
        // Get string characters.
        do {
            // Replace any whitespace character with a blank.
//            if (Character.isWhitespace(currentChar)) {
//                currentChar = ' ';
//            }

            if (isEscape(currentChar)) {
                char escape = applyEscape(currentChar);
                textBuffer.append(escape);
                valueBuffer.append(escape);

                currentChar = currentChar();
            }
            else if (( (inBlockString || currentChar != STRING_CHAR) ) && (currentChar != EOF)) {
                textBuffer.append(currentChar);
                valueBuffer.append(currentChar);
                currentChar = nextChar();  // consume character
            }

            // Look for multi comments
            if (currentChar == STRING_CHAR) {
                if ((currentChar == STRING_CHAR) && (peekChar() == STRING_CHAR)) { 
                    char thirdChar = peekAhead(isStart ? 1 : 2);  // look for the third quote
                    if ( thirdChar == STRING_CHAR ) {
                        textBuffer.append(STRING_CHAR + STRING_CHAR);
                        // valueBuffer.append(currentChar); // append single-quote
                        currentChar = nextChar();        // consume quotes
                        currentChar = nextChar();
                        inBlockString = !inBlockString;
                    }                    
                }
            }
            isStart = false;
        } while ((inBlockString || currentChar != STRING_CHAR) && (currentChar != EOF));

        if (currentChar == STRING_CHAR) {
            nextChar();  // consume final quote
            textBuffer.append(STRING_CHAR);

            type = STRING;
            value = valueBuffer.toString();
        }
        else {
            type = ERROR;
            value = UNEXPECTED_EOF;
        }

        text = textBuffer.toString();
    }

    /**
     * Determines if this is an escape character.
     *
     * @param currentChar
     * @return
     */
    private boolean isEscape(char currentChar) {
        boolean isEscape = false;
        if ( currentChar == '\\' ) {
            char nextChar = peekChar();
            switch(nextChar) {
            case 't':
            case 'b':
            case 'n':
            case 'r':
            case 'f':
            case '\'':
            case '\"':
            case '\\':
                isEscape = true;
                break;
            default:
                isEscape = false;
            }
        }

        return isEscape;
    }

    /**
     * Eat to the end of the escape
     * @param currentChar
     * @return
     */
    private char applyEscape(char currentChar) {
        char result = currentChar;
        char nextChar = nextChar();
        switch(nextChar) {
            case 't':
                result = "\t".charAt(0);
                break;
            case 'b':
                result = "\b".charAt(0);
                break;
            case 'n':
                result = "\n".charAt(0);
                break;
            case 'r':
                result = "\r".charAt(0);
                break;
            case 'f':
                result = "\f".charAt(0);
                break;
            case '\'':
                result = "\'".charAt(0);
                break;
            case '\"':
                result = "\"".charAt(0);
                break;
            case '\\':
                result = "\\".charAt(0);
                break;
            default:
                throw new IllegalArgumentException("Must invoke isEscape first!");
        }
        nextChar(); // eat this char

        return result;
    }
}
