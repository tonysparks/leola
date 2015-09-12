/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.frontend.parsers;

import leola.ast.ASTNode;
import leola.ast.CatchStmt;
import leola.ast.Stmt;
import leola.frontend.LeolaParser;
import leola.frontend.Token;
import leola.frontend.tokens.LeolaErrorCode;
import leola.frontend.tokens.LeolaTokenType;



/**
 * ON Statement Parser
 *
 * @author Tony
 *
 */
public class CatchStmtParser extends StmtParser {

	/**
	 * @param parser
	 */
	public CatchStmtParser(LeolaParser parser) {
		super(parser);
	}

    /**
     * Parse the {@link CatchStmt}
     * 
     * @param token the initial token.
     * @return the root node of the generated parse tree.
     * @throws Exception if an error occurred.
     */
	@Override
    public ASTNode parse(Token token)
        throws Exception {		
	       
        token = nextToken(); // eat the CATCH token
        if(! token.getType().equals(LeolaTokenType.IDENTIFIER)) {
            getExceptionHandler().errorToken(token, this, LeolaErrorCode.MISSING_IDENTIFIER);
        }                        
        String identifier = token.getText();
        
        Stmt stmt = (Stmt) (new StmtParser(this)).parse(nextToken());
	    
		
        // Create the Catch node.
    	CatchStmt onStmt = new CatchStmt(identifier, stmt);
        setLineNumber(onStmt, currentToken());
        return onStmt;
    }

}

