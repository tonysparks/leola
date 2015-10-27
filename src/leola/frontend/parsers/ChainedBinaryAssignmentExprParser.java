/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.frontend.parsers;

import leola.ast.ASTNode;
import leola.ast.BinaryExpr.BinaryOp;
import leola.ast.ChainedBinaryAssignmentExpr;
import leola.ast.Expr;
import leola.frontend.LeolaParser;
import leola.frontend.Token;
import leola.frontend.tokens.LeolaErrorCode;
import leola.frontend.tokens.LeolaTokenType;

/**
 * @author Tony
 *
 */
public class ChainedBinaryAssignmentExprParser extends ExprParser {

	/**
	 * @param parser
	 */
	public ChainedBinaryAssignmentExprParser(LeolaParser parser) {
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

    	BinaryOp binaryOp = null;

        token = currentToken();
        if (LeolaTokenType.BINARY_ASSIGNMENT.containsValue(token.getType()) ) {
        	LeolaTokenType type = token.getType();
        	binaryOp = type.toBinaryOp();

            token = nextToken();
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
        ChainedBinaryAssignmentExpr assignNode = new ChainedBinaryAssignmentExpr(lhsExpr, exprNode, binaryOp);
        setLineNumber(assignNode, startingToken);

        return assignNode;
    }
}

