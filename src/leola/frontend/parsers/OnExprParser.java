/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.frontend.parsers;

import java.util.List;

import leola.ast.ASTNode;
import leola.ast.Expr;
import leola.ast.OnExpr;
import leola.ast.OnExpr.OnClause;
import leola.frontend.LeolaParser;
import leola.frontend.Token;



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
     * Parse the ON Expression.
     * 
     * @param token the initial token.
     * @return the root node of the generated parse tree.
     * @throws Exception if an error occurred.
     */
	@Override
    public ASTNode parse(Token token)
        throws Exception
    {		
		List<OnClause> clauses = OnStmtParser.parseOnClause(this, token);
		
        // Create the ON node.
    	OnExpr onExpr = new OnExpr(expr, clauses );
        setLineNumber(onExpr, currentToken());

        return onExpr;
    }

}

