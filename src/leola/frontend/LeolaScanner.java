/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.frontend;

import static leola.frontend.Source.EOF;
import static leola.frontend.Source.EOL;
import leola.frontend.tokens.LeolaErrorCode;
import leola.frontend.tokens.LeolaErrorToken;
import leola.frontend.tokens.LeolaNumberToken;
import leola.frontend.tokens.LeolaSpecialSymbolToken;
import leola.frontend.tokens.LeolaStringToken;
import leola.frontend.tokens.LeolaTokenType;
import leola.frontend.tokens.LeolaWordToken;

/**
 * @author Tony
 *
 */
public class LeolaScanner extends Scanner {

	public static final String START_COMMENT = "/*";
	public static final String END_COMMENT = "*/";
	public static final String SINGLE_COMMENT = "//";

	/**
	 * @param source
	 */
	public LeolaScanner(Source source) {
		super(source);
	}

	/* (non-Javadoc)
	 * @see leola.frontend.Scanner#extractToken()
	 */
	@Override
	protected Token extractToken() throws Exception {
		skipWhiteSpace();
        Token token;
        char currentChar = currentChar();

        // Construct the next token.  The current character determines the
        // token type.
        if (currentChar == EOF) {
            token = new EofToken(source);
        }
        else if ( LeolaWordToken.isValidStartIdentifierCharacter(currentChar) ) {
            token = new LeolaWordToken(source);
        }
        else if (Character.isDigit(currentChar)) {
            token = new LeolaNumberToken(source);
        }
        else if (currentChar == LeolaStringToken.STRING_CHAR) {
            token = new LeolaStringToken(source);
        }
        else if (LeolaTokenType.SPECIAL_SYMBOLS
                 .containsKey(Character.toString(currentChar))) {
            token = new LeolaSpecialSymbolToken(source);
        }
        else {
            token = new LeolaErrorToken(source, LeolaErrorCode.INVALID_CHARACTER,
                                         Character.toString(currentChar));
            nextChar();  // consume character
        }

        return token;
	}

	/**
     * Skip whitespace characters by consuming them.  A comment is whitespace.
     * @throws Exception if an error occurred.
     */
    private void skipWhiteSpace() throws Exception {
        char currentChar = currentChar();

        while (Character.isWhitespace(currentChar)
        		|| checkSequence(START_COMMENT)
        		|| checkSequence(SINGLE_COMMENT) ) {

            // Start of a comment?
            if ( checkSequence(START_COMMENT) ) {
                do {
                    currentChar = nextChar();  // consume comment characters
                }
                while ((!checkSequence(END_COMMENT)) && (currentChar != EOF));

                // Found closing '}'?
                if ( checkSequence(END_COMMENT) ) {
                	for(int i = 0; i < END_COMMENT.length(); i++) {
                		currentChar = nextChar();  // consume the comment
                	}
                }
            }
            else if ( checkSequence(SINGLE_COMMENT) ) {
            	do {
                    currentChar = nextChar();  // consume comment characters
                }
                while (currentChar != EOL);
            }
            // Not a comment.
            else {
                currentChar = nextChar();  // consume whitespace character
            }
        }
    }

    /**
     * Check the sequence matches the input (2 chars).
     *
     * @param seq
     * @return
     * @throws Exception
     */
    private boolean checkSequence(String seq) throws Exception {
    	boolean result = true;
    	char currentChar = currentChar();

    	for(int i = 0; i < 2; i++) {
    		if (currentChar == EOF ) {
    			result = false;
    			break;
    		}

    		if (currentChar != seq.charAt(i)) {
    			result = false;
    			break;
    		}

    		currentChar = peekChar();
    	}
    	/*if ( result ) {
    		nextChar(); // eat the char
    	}*/

    	return result;
    }
}

