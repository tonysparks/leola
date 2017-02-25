/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.frontend;

import java.io.IOException;

import leola.ast.ASTNode;
import leola.frontend.parsers.ProgramParser;
import leola.frontend.tokens.LeolaErrorCode;
import leola.frontend.tokens.LeolaTokenType;

/**
 * A {@link Parser} for the Leola programming language.
 * 
 * @author Tony
 *
 */
public class LeolaParser extends Parser {

    /**
     * @param scanner
     */
    public LeolaParser(Scanner scanner                     
                     , ExceptionHandler exceptionHandler) {
        super(scanner, exceptionHandler);
    }
    
    /**
     * Copy constructor
     * @param parser
     */
    public LeolaParser(LeolaParser parser) {
        this(parser.getScanner(), parser.getExceptionHandler());
    }

    /* (non-Javadoc)
     * @see leola.frontend.Parser#getErrorCount()
     */
    @Override
    public int getErrorCount() {
        return getExceptionHandler().getErrorCount();
    }

    /**
     * Set the current line number as a statement node attribute.
     * @param node ICodeNode
     * @param token Token
     */
    protected void setLineNumber(ASTNode node, Token token) {
        if (node != null) {
            node.setSourceLine(token.getText());
            node.setLineNumber(token.getLineNumber());
        }
    }
    
    /* (non-Javadoc)
     * @see leola.frontend.Parser#parse()
     */
    @Override
    public ASTNode parse() throws Exception {
        ASTNode program = null;
        try {
            Token token = nextToken();

            // Parse a program.
            ProgramParser programParser = new ProgramParser(this);            
            program = programParser.parse(token);
            
        }
        catch (java.io.IOException ex) {
            this.getExceptionHandler().onException(ex);
        }
        
        return program;
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

