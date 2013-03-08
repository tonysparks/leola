package leola.frontend.tokens;

import leola.frontend.Source;

import static leola.frontend.tokens.LeolaTokenType.*;
/**
 * <h1>PascalWordToken</h1>
 *
 * <p> Pascal word tokens (identifiers and reserved words).</p>
 *
 * <p>Copyright (c) 2009 by Ronald Mak</p>
 * <p>For instructional purposes only.  No warranties.</p>
 */
public class LeolaWordToken extends LeolaToken
{

	/**
	 * Determines if the supplied character is valid inside the identifier
	 * @param c
	 * @return
	 */
	public static final boolean isValidIdentifierCharacter(char c) {
		boolean isValid = Character.isLetterOrDigit(c);
		if ( !isValid ) {
			switch(c) {
			case '$':
			case '@':
			case '_':
				isValid = true;
				break;
			}
		}

		return isValid;
	}

	/**
	 * Determines if the supplied character is a valid start character for an identifier
	 *
	 * @param c
	 * @return true if valid
	 */
	public static final boolean isValidStartIdentifierCharacter(char c) {
		boolean isValid = Character.isLetter(c);
		if ( !isValid ) {
			switch(c) {
			case '$':
			case '@':
			case '_':
				isValid = true;
				break;
			}
		}

		return isValid;
	}

    /**
     * Constructor.
     * @param source the source from where to fetch the token's characters.
     * @throws Exception if an error occurred.
     */
    public LeolaWordToken(Source source)
        throws Exception
    {
        super(source);
    }

    /**
     * Extract a Pascal word token from the source.
     * @throws Exception if an error occurred.
     */
    @Override
	protected void extract()
        throws Exception
    {
        StringBuilder textBuffer = new StringBuilder();
        char currentChar = currentChar();

        // Get the word characters (letter or digit).  The scanner has
        // already determined that the first character is a letter.
        while (isValidIdentifierCharacter(currentChar)) {
            textBuffer.append(currentChar);
            currentChar = nextChar();  // consume character
        }

        text = textBuffer.toString();

        // Is it a reserved word or an identifier?
        type = (RESERVED_WORDS.contains(text))
               ? LeolaTokenType.valueOf(text.toUpperCase())  // reserved word
               : IDENTIFIER;                                  // identifier
    }
}
