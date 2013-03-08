/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.frontend.parsers;

import java.util.ArrayList;
import java.util.List;

import leola.ast.ASTNode;
import leola.ast.Expr;
import leola.ast.OnExpr;
import leola.ast.OnExpr.OnClause;
import leola.ast.Stmt;
import leola.frontend.LeolaParser;
import leola.frontend.Token;
import leola.frontend.tokens.LeolaErrorCode;
import leola.frontend.tokens.LeolaTokenType;



/**
 * ON Expression Parser
 *
 * @author Tony
 *
 */
public class OnExprParser extends ExprParser {

	private Expr expr;

	/**
	 * @param parser
	 */
	public OnExprParser(Expr lhsExpr, LeolaParser parser) {
		super(parser);
		this.expr = lhsExpr;
	}


    /**
     * Parse an assignment statement.
     * @param token the initial token.
     * @return the root node of the generated parse tree.
     * @throws Exception if an error occurred.
     */
	@Override
    public ASTNode parse(Token token)
        throws Exception
    {		
		List<OnClause> clauses = new ArrayList<OnExpr.OnClause>(2);					
		do {
			token = nextToken(); // eat the ON token
			if(! token.getType().equals(LeolaTokenType.IDENTIFIER)) {
				getExceptionHandler().errorToken(token, this, LeolaErrorCode.MISSING_IDENTIFIER);
			}
	    	String className = token.getText();
	    	
	    	token = nextToken(); // eat the classname token
			if(! token.getType().equals(LeolaTokenType.IDENTIFIER)) {
				getExceptionHandler().errorToken(token, this, LeolaErrorCode.MISSING_IDENTIFIER);
			}
			
	    	String identifier = token.getText();
	    	Stmt stmt = (Stmt)(new StmtParser(this)).parse(nextToken());
	    	
	    	clauses.add(new OnClause(className, identifier, stmt));
	    	token = currentToken();
		} while (token.getType().equals(LeolaTokenType.ON));
		
        // Create the IS node.
    	OnExpr isExpr = new OnExpr(expr, clauses );
        setLineNumber(isExpr, currentToken());

        return isExpr;
    }

}

