package leola.frontend;

import java.io.IOException;

import leola.ast.ASTNode;
import leola.frontend.listener.EventDispatcher;
import leola.frontend.tokens.LeolaErrorCode;
import leola.frontend.tokens.LeolaTokenType;

/**
 * <h1>Parser</h1>
 *
 * <p>
 * A language-independent framework class. This abstract parser class will be
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
public abstract class Parser {

    /**
     * Event dispatcher
     */
    private EventDispatcher eventDispatcher;

    /**
     * Exception handler
     */
    private ExceptionHandler exceptionHandler;

    protected Scanner scanner; // scanner used with this parser

    /**
     * Constructor.
     * 
     * @param scanner
     *            the scanner to be used with this parser.
     */
    protected Parser(Scanner scanner, ExceptionHandler exceptionHandler) {
        this.scanner = scanner;
        this.exceptionHandler = exceptionHandler;
        this.eventDispatcher = scanner.getEventDispatcher();
    }

    /**
     * @return the exceptionHandler
     */
    public ExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

    /**
     * Getter.
     * 
     * @return the scanner used by this parser.
     */
    public Scanner getScanner() {
        return scanner;
    }

    public Source getSource() {
        return scanner.getSource();
    }

    /**
     * @return the eventDispatcher
     */
    public EventDispatcher getEventDispatcher() {
        return eventDispatcher;
    }

    /**
     * Parse a source program and generate the intermediate code and the symbol
     * table. To be implemented by a language-specific parser subclass.
     * 
     * @throws Exception
     *             if an error occurred.
     */
    public abstract ASTNode parse() throws Exception;

    /**
     * Return the number of syntax errors found by the parser. To be implemented
     * by a language-specific parser subclass.
     * 
     * @return the error count.
     */
    public abstract int getErrorCount();

    /**
     * @return retrieves the previous token, may return null if none
     */
    public Token previousToken() {
        return scanner.previousToken();
    }

    /**
     * Call the scanner's currentToken() method.
     * 
     * @return the current token.
     */
    public Token currentToken() {
        return scanner.currentToken();
    }

    /**
     * Call the scanner's nextToken() method.
     * 
     * @return the next token.
     * @throws Exception
     *             if an error occurred.
     */
    public Token nextToken() throws IOException {
        return scanner.nextToken();
    }
    
    
    /**
     * Issues a parsing exception.
     * 
     * @param token
     * @param errorCode
     */
    public void throwParseError(Token token, LeolaErrorCode errorCode) {
        getExceptionHandler().errorToken(token, this, errorCode);
    }
    
    
    /**
     * Expect that the current token is of the supplied {@link LeolaTokenType}. If the expected token is
     * not a match, a parser error is thrown (parser{@link #throwParseError(Token, LeolaErrorCode)}.
     * 
     * @param currentToken the current {@link Token}, that will be validated 
     * @param expectedType the expected {@link LeolaTokenType}, which should match the currentToken
     * @param errorCode the {@link LeolaErrorCode} that will be raised if the current token does not match the expected type
     */
    public void expectToken(Token currentToken, LeolaTokenType expectedType, LeolaErrorCode errorCode) {
        if ( ! currentToken.getType().equals(expectedType) ) {
            throwParseError(currentToken, errorCode);
        }
    }
    
    /**
     * Expect that the current token is of the supplied {@link LeolaTokenType}. If the expected token is
     * not a match, a parser error is thrown (parser{@link #throwParseError(Token, LeolaErrorCode)}.  If there is a 
     * match, {@link Parser#nextToken()} is called.
     * 
     * @param currentToken the current {@link Token}, that will be validated 
     * @param expectedType the expected {@link LeolaTokenType}, which should match the currentToken
     * @param errorCode the {@link LeolaErrorCode} that will be raised if the current token does not match the expected type
     * @return calls {@link Parser#nextToken()} and returns the {@link Token}
     * @throws IOException
     */
    public Token expectTokenNext(Token currentToken, LeolaTokenType expectedType, LeolaErrorCode errorCode) throws IOException {
        expectToken(currentToken, expectedType, errorCode);
        return nextToken();
    }

}
