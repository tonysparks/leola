package leola.frontend;

/**
 * <h1>Token</h1>
 *
 * <p>
 * The framework class that represents a token returned by the scanner.
 * </p>
 *
 * <p>
 * Copyright (c) 2009 by Ronald Mak
 * </p>
 * <p>
 * For instructional purposes only. No warranties.
 * </p>
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
     * @throws Exception
     *             if an error occurred.
     */
    public Token(Source source) throws Exception {
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
    @SuppressWarnings("unchecked")
    public <T extends TokenType> T getType() {
        return (T) type;
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
     * @throws Exception
     *             if an error occurred.
     */
    protected void extract() throws Exception {
        text = Character.toString(currentChar());
        value = null;

        nextChar(); // consume current character
    }

    /**
     * Call the source's currentChar() method.
     * 
     * @return the current character from the source.
     * @throws Exception
     *             if an error occurred.
     */
    protected char currentChar() throws Exception {
        return source.currentChar();
    }

    /**
     * Call the source's nextChar() method.
     * 
     * @return the next character from the source after moving forward.
     * @throws Exception
     *             if an error occurred.
     */
    protected char nextChar() throws Exception {
        return source.nextChar();
    }

    /**
     * Call the source's peekChar() method.
     * 
     * @return the next character from the source without moving forward.
     * @throws Exception
     *             if an error occurred.
     */
    protected char peekChar() throws Exception {
        return source.peekChar();
    }

    /**
     * Peek ahead <code>pos</code> many spaces.
     * 
     * @param pos
     * @return the char at the current position + pos, or EOL/EOF if it reached the end of the line or file.
     * @throws Exception
     */
    protected char peekAhead(int pos) throws Exception {
        return source.peekAhead(pos);
    }
}
