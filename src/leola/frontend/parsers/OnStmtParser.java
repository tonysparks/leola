/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.frontend.parsers;

import java.util.ArrayList;
import java.util.List;

import leola.ast.ASTNode;
import leola.ast.OnExpr;
import leola.ast.OnExpr.OnClause;
import leola.ast.OnStmt;
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
public class OnStmtParser extends StmtParser {

	/**
	 * @param parser
	 */
	public OnStmtParser(LeolaParser parser) {
		super(parser);
	}

	
	/**
	 * Parses the on clause statements
	 * 
	 * @param parser
	 * @param token
	 * @return the list of {@link OnClause}'s
	 * @throws Exception
	 */
	public static List<OnClause> parseOnClause(LeolaParser parser, Token token) throws Exception {
		List<OnClause> clauses = new ArrayList<OnExpr.OnClause>(2);					
		do {
			token = parser.nextToken(); // eat the ON token
			if(! token.getType().equals(LeolaTokenType.IDENTIFIER)) {
				parser.getExceptionHandler().errorToken(token, parser, LeolaErrorCode.MISSING_IDENTIFIER);
			}
	    	String className = token.getText();
	    	
	    	token = parser.nextToken(); // eat the classname token
			if(! token.getType().equals(LeolaTokenType.IDENTIFIER)) {
				parser.getExceptionHandler().errorToken(token, parser, LeolaErrorCode.MISSING_IDENTIFIER);
			}
			
	    	String identifier = token.getText();
	    	Stmt stmt = (Stmt)(new StmtParser(parser)).parse(parser.nextToken());
	    	
	    	clauses.add(new OnClause(className, identifier, stmt));
	    	token = parser.currentToken();
		} while (token.getType().equals(LeolaTokenType.ON));
		
		return clauses;
	}

    /**
     * Parse the {@link OnStmt}
     * 
     * @param token the initial token.
     * @return the root node of the generated parse tree.
     * @throws Exception if an error occurred.
     */
	@Override
    public ASTNode parse(Token token)
        throws Exception {		
		List<OnClause> clauses = parseOnClause(this, token);
		
        // Create the ON node.
    	OnStmt onStmt = new OnStmt(clauses);
        setLineNumber(onStmt, currentToken());
        return onStmt;
    }

}

