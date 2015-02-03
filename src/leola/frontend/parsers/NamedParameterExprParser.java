/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.frontend.parsers;

import leola.ast.ASTNode;
import leola.ast.Expr;
import leola.ast.NamedParameterExpr;
import leola.ast.OnStmt;
import leola.frontend.LeolaParser;
import leola.frontend.Token;



/**
 * Named Parameter Statement Parser
 *
 * @author Tony
 *
 */
public class NamedParameterExprParser extends StmtParser {

	/**
	 * @param parser
	 */
	public NamedParameterExprParser(LeolaParser parser) {
		super(parser);
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
		
	    String parameterName = token.getText();
		
	    Expr valueExpr = (Expr)new ExprParser(this).parse(nextToken());
	    
        NamedParameterExpr stmt = new NamedParameterExpr(parameterName, valueExpr);
        setLineNumber(stmt, currentToken());
        return stmt;
    }

}

