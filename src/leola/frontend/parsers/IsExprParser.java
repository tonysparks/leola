/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.frontend.parsers;

import leola.ast.ASTNode;
import leola.ast.ArrayAccessSetExpr;
import leola.ast.AssignmentExpr;
import leola.ast.Expr;
import leola.ast.IsExpr;
import leola.frontend.LeolaParser;
import leola.frontend.Token;



/**
 * IS Expression Parser
 *
 * @author Tony
 *
 */
public class IsExprParser extends ExprParser {

	private Expr lhsExpr;

	/**
	 * @param parser
	 */
	public IsExprParser(Expr lhsExpr, LeolaParser parser) {
		super(parser);
		this.lhsExpr = lhsExpr;
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
	    Token startingToken = token;
		String varName = token.getText();
		
		/**
		 * HACK -
		 * 	If the LHS was NULL, the lhsExpr won't be NULL.
		 */
		if ( this.lhsExpr == null ) {

	    	// the left hand side (array or map) expr
	    	lhsExpr = (Expr) new ExprParser(this).parse(token);
	    	if ( lhsExpr instanceof AssignmentExpr ) {
	    		((ArrayAccessSetExpr)lhsExpr).setVariableName(varName);
	    	}

	    	token = currentToken();
		}
		else {
			token = nextToken(); // eat the IS token
		}

    	String className = ParserUtils.parseClassName(this, token);

        // Create the IS node.
    	IsExpr isExpr = new IsExpr(varName, lhsExpr, className);
        setLineNumber(isExpr, startingToken);

        return isExpr;
    }

}

