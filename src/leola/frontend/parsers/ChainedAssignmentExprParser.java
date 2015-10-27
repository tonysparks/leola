/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.frontend.parsers;

import leola.ast.ASTNode;
import leola.ast.ChainedAssignmentExpr;
import leola.ast.Expr;
import leola.frontend.LeolaParser;
import leola.frontend.Token;
import leola.frontend.tokens.LeolaErrorCode;
import leola.frontend.tokens.LeolaTokenType;

/**
 * @author Tony
 *
 */
public class ChainedAssignmentExprParser extends ExprParser {

	/**
	 * @param parser
	 */
	public ChainedAssignmentExprParser(LeolaParser parser) {
		super(parser);
	}

    /**
     * Parse an assignment statement.
     * @param token the initial token.
     * @return the root node of the generated parse tree.
     * @throws Exception if an error occurred.
     */
    public ASTNode parse(Token token)
        throws Exception
    {
        Token startingToken = token;

    	// the left hand side (array or map) expr
    	Expr lhsExpr = null;

        // Synchronize on the = token.
        token = currentToken();
        if (token.getType() == LeolaTokenType.EQUALS) {
            token = nextToken();  // consume the =
        }
        else if ( token.getType() == LeolaTokenType.LEFT_BRACKET) {
        	lhsExpr = parseExpr(token);
        	token = currentToken();
        }
        else {
            throwParseError(token, LeolaErrorCode.MISSING_EQUALS);
        }

        // Parse the expression.  The ASSIGN node adopts the expression's
        // node as its second child.
        Expr exprNode = parseExpr(token);

        // Create the ASSIGN node.
        ChainedAssignmentExpr assignNode = new ChainedAssignmentExpr(lhsExpr, exprNode);
        setLineNumber(assignNode, startingToken);

        return assignNode;
    }
}

