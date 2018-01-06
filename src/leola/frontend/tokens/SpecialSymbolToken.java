package leola.frontend.tokens;

import static leola.frontend.ErrorCode.INVALID_CHARACTER;
import static leola.frontend.tokens.TokenType.ERROR;
import static leola.frontend.tokens.TokenType.SPECIAL_SYMBOLS;

import leola.frontend.Source;

/**
 * Special symbol tokens
 * 
 * @author Tony
 *
 */
public class SpecialSymbolToken extends Token {
    
    /**
     * @param source the source from where to fetch the token's characters.
     */
    public SpecialSymbolToken(Source source) {
        super(source);
    }

    /**
     * Extract a Leola special symbol token from the source.
     */
    @Override
    protected void extract() {
        char currentChar = currentChar();

        text = Character.toString(currentChar);
        type = null;

        switch (currentChar) {

            // Single-character special symbols.
            case ',':  case ';':  case '(':  case ')': 
            case '[':  case ']':  case '{':  case '}': case ':':
            case '@':  case '~':  
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
                
                char peekChar = currentChar();
                if ( peekChar == '=' ) {
                    text += peekChar;
                    nextChar(); // consume '='
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
