package leola.frontend;

import java.io.IOException;

import leola.frontend.listener.EventDispatcher;

/**
 * <h1>Scanner</h1>
 *
 * <p>
 * A language-independent framework class. This abstract scanner class will be
 * implemented by language-specific subclasses.
 * </p>
 *
 * <p>
 * Copyright (c) 2009 by Ronald Mak
 * </p>
 * <p>
 * For instructional purposes only. No warranties.
 * </p>
 */
public abstract class Scanner {
    
    protected Source source; // source
    private Token currentToken; // current token
    private Token prevToken;

    private EventDispatcher eventDispatcher;

    /**
     * Constructor
     * 
     * @param source
     *            the source to be used with this scanner.
     */
    public Scanner(Source source) {
        this.source = source;
        this.eventDispatcher = source.getEventDispatcher();
    }

    /**
     * @return the source
     */
    public Source getSource() {
        return source;
    }

    /**
     * @return the eventDispatcher
     */
    public EventDispatcher getEventDispatcher() {
        return eventDispatcher;
    }

    /**
     * @return the prevToken
     */
    public Token previousToken() {
        return prevToken;
    }

    /**
     * @return the current token.
     */
    public Token currentToken() {
        return currentToken;
    }

    /**
     * Return next token from the source.
     * 
     * @return the next token.
     * @throws Exception
     *             if an error occurred.
     */
    public Token nextToken() throws IOException {
        prevToken = currentToken;
        currentToken = extractToken();
        return currentToken;
    }

    /**
     * Do the actual work of extracting and returning the next token from the
     * source. Implemented by scanner subclasses.
     * 
     * @return the next token.
     * @throws Exception
     *             if an error occurred.
     */
    protected abstract Token extractToken() throws IOException;

    /**
     * Call the source's currentChar() method.
     * 
     * @return the current character from the source.
     * @throws Exception
     *             if an error occurred.
     */
    public char currentChar() throws IOException {
        return source.currentChar();
    }

    /**
     * Call the source's nextChar() method.
     * 
     * @return the next character from the source.
     * @throws Exception
     *             if an error occurred.
     */
    public char nextChar() throws IOException {
        return source.nextChar();
    }

    /**
     * Peeks at the next char
     * 
     * @return the peeked character
     * @throws Exception
     */
    public char peekChar() throws IOException {
        return this.source.peekChar();
    }

    /**
     * Call the source's atEol() method.
     * 
     * @return true if at the end of the source line, else return false.
     * @throws Exception
     *             if an error occurred.
     */
    public boolean atEol() throws IOException {
        return source.atEol();
    }

    /**
     * Call the source's atEof() method.
     * 
     * @return true if at the end of the source file, else return false.
     * @throws Exception
     *             if an error occurred.
     */
    public boolean atEof() throws IOException {
        return source.atEof();
    }

    /**
     * Call the source's skipToNextLine() method.
     * 
     * @throws Exception
     *             if an error occurred.
     */
    public void skipToNextLine() throws IOException {
        source.skipToNextLine();
    }
}
