/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.frontend;

import leola.ast.ASTNode;
import leola.frontend.events.ParserSummaryEvent;
import leola.frontend.parsers.ProgramParser;

/**
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
		// TODO Auto-generated method stub
		return 0;
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
        long startTime = System.currentTimeMillis();
        ASTNode program = null;
        try {
            Token token = nextToken();

            // Parse a program.
            ProgramParser programParser = new ProgramParser(this);            
            program = programParser.parse(token);
            
            // Send the parser summary message.
            float elapsedTime = (System.currentTimeMillis() - startTime) / 1000.0f;
            getEventDispatcher().sendNow( new ParserSummaryEvent(this, token.getLineNumber(),getErrorCount(), elapsedTime) );
            
        }
        catch (java.io.IOException ex) {
            this.getExceptionHandler().onException(ex);
        }
        
        return program;
	}

}

