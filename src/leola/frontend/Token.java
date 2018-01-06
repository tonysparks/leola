package leola.frontend;

import leola.frontend.tokens.TokenType;

/**
 * A Token represents a language token such as a symbol or identifier
 * 
 * @author Tony
 *
 */
public class Token {
    protected TokenType type; // language-specific token type
    protected String text; // token text
    protected Object value; // token value
    protected Source source; // source
    protected int lineNum; // line number of the token's source line
    protected int position; // position of the first token character

    /**
     * Constructor.
     * 
     * @param source
     *            the source from where to fetch the token's characters.
     */
    public Token(Source source) {
        this.source = source;
        this.lineNum = source.getLineNum();
        this.position = source.getPosition();

        extract();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.text;
    }

    /**
     * Getter
     * 
     * @return the token type
     */    
    public TokenType getType() {
        return this.type;
    }

    /**
     * Getter.
     * 
     * @return the token text.
     */
    public String getText() {
        return text;
    }

    /**
     * Getter.
     * 
     * @return the token value.
     */
    public Object getValue() {
        return value;
    }

    /**
     * Getter.
     * 
     * @return the source line number.
     */
    public int getLineNumber() {
        return lineNum;
    }

    /**
     * Getter.
     * 
     * @return the position.
     */
    public int getPosition() {
        return position;
    }

    /**
     * Default method to extract only one-character tokens from the source.
     * Subclasses can override this method to construct language-specific
     * tokens. After extracting the token, the current source line position will
     * be one beyond the last token character.
     * 
     */
    protected void extract() {
        text = Character.toString(currentChar());
        value = null;

        nextChar(); // consume current character
    }

    /**
     * Call the source's currentChar() method.
     * 
     * @return the current character from the source.
     */
    protected char currentChar() {
        return source.currentChar();
    }

    /**
     * Call the source's nextChar() method.
     * 
     * @return the next character from the source after moving forward.
     */
    protected char nextChar() {
        return source.nextChar();
    }

    /**
     * Call the source's peekChar() method.
     * 
     * @return the next character from the source without moving forward.
     */
    protected char peekChar() {
        return source.peekChar();
    }

    /**
     * Peek ahead <code>pos</code> many spaces.
     * 
     * @param pos
     * @return the char at the current position + pos, or EOL/EOF if it reached the end of the line or file.
     */
    protected char peekAhead(int pos) {
        return source.peekAhead(pos);
    }
}
