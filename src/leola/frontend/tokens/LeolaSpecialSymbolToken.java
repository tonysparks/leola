package leola.frontend.tokens;

import leola.frontend.Source;
import static leola.frontend.tokens.LeolaTokenType.*;
import static leola.frontend.tokens.LeolaErrorCode.*;

/**
 * Special symbol tokens
 * 
 * @author Tony
 *
 */
public class LeolaSpecialSymbolToken extends LeolaToken {
    /**
     * Constructor.
     * @param source the source from where to fetch the token's characters.
     * @throws Exception if an error occurred.
     */
    public LeolaSpecialSymbolToken(Source source)
        throws Exception {
        super(source);
    }

    /**
     * Extract a Leola special symbol token from the source.
     * @throws Exception if an error occurred.
     */
    @Override
	protected void extract()
        throws Exception {
        char currentChar = currentChar();

        text = Character.toString(currentChar);
        type = null;

        switch (currentChar) {

            // Single-character special symbols.
            case ',':  case ';':  case '(':  case ')': 
            case '[':  case ']':  case '{':  case '}': case ':':
            case '~':
            // case '^':
            {
                nextChar();  // consume character
                break;
            }

            // . or ..
            case '.': {
            	currentChar = nextChar();
            	 if (currentChar == '.') {
                     text += currentChar;
                     currentChar = nextChar();  // consume '.'
                     
                     if(currentChar == '.') {
                     	text += currentChar;
                     	nextChar();
                     }
                     else {
                    	 type = ERROR;
                         value = INVALID_CHARACTER;
                     }
                 }
            	 break;
            }
            
            // + or +=
            case '+' : {
            	currentChar = nextChar();

            	if (currentChar == '=') {
                    text += currentChar;
                    nextChar();  // consume '='
                }

            	break;
            }

            // * or *=
            case '*' : {
            	currentChar = nextChar();

            	if (currentChar == '=') {
                    text += currentChar;
                    nextChar();  // consume '='
                }

            	break;
            }

            // / or /=
            case '/' : {
            	currentChar = nextChar();

            	if (currentChar == '=') {
                    text += currentChar;
                    nextChar();  // consume '='
                }

            	break;
            }

            // % or %=
            case '%' : {
            	currentChar = nextChar();

            	if (currentChar == '=') {
                    text += currentChar;
                    nextChar();  // consume '='
                }

            	break;
            }

            // ^ or ^=
            case '^' : {
            	currentChar = nextChar();

            	if (currentChar == '=') {
                    text += currentChar;
                    nextChar();  // consume '='
                }

            	break;
            }

            // = or ==
            case '=': {
                currentChar = nextChar();  // consume '=';

                if (currentChar == '=') {
                    text += currentChar;
                    currentChar = nextChar();  // consume '='
                    
                    if(currentChar == '=') {
                    	text += currentChar;
                    	nextChar();
                    }
                }

                break;
            }

            // < or <= or <>
            case '<': {
                currentChar = nextChar();  // consume '<';

                if (currentChar == '=') {
                    text += currentChar;
                    nextChar();  // consume '='
                }
                else if (currentChar == '<') {
                    text += currentChar;
                    nextChar();  // consume '<'

                    char peekChar = currentChar();
                    if ( peekChar == '=' ) {
                    	text += peekChar;
                    	nextChar(); // consume '='
                    }
                }

                break;
            }

            // > or >=
            case '>': {
                currentChar = nextChar();  // consume '>';

                if (currentChar == '=') {
                    text += currentChar;
                    nextChar();  // consume '='
                }
                else if (currentChar == '>') {
                    text += currentChar;
                    nextChar();  // consume '>'

                    char peekChar = currentChar();
                    if ( peekChar == '=' ) {
                    	text += peekChar;
                    	nextChar(); // consume '='
                    }
                }

                break;
            }

            // - or ->
            case '-': {
                currentChar = nextChar();  // consume '-';

                if (currentChar == '>') {
                    text += currentChar;
                    nextChar();  // consume '>'
                }
                else if (currentChar == '=') {
                    text += currentChar;
                    nextChar();  // consume '='
                }

                break;
            }
            case '!': {
                currentChar = nextChar();  // consume '!';

                if (currentChar == '=') {
                    text += currentChar;
                    nextChar();  // consume '='
                }

                break;
            }
            case '&': {
                currentChar = nextChar();  // consume '&';

                if (currentChar == '&') {
                    text += currentChar;
                    nextChar();  // consume '&'
                }
                else if (currentChar == '=') {
                    text += currentChar;
                    nextChar();  // consume '='
                }

                break;
            }
            case '|': {
                currentChar = nextChar();  // consume '|';

                if (currentChar == '|') {
                    text += currentChar;
                    nextChar();  // consume '|'
                }
                else if (currentChar == '=') {
                    text += currentChar;
                    nextChar();  // consume '='
                }

                break;
            }

            default: {
                nextChar();  // consume bad character
                type = ERROR;
                value = INVALID_CHARACTER;
            }
        }

        // Set the type if it wasn't an error.
        if (type == null) {
            type = SPECIAL_SYMBOLS.get(text);
        }
    }
}
