/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.frontend.parsers;

import leola.ast.ASTNode;
import leola.ast.ChainedArrayAccessExpr;
import leola.ast.ChainedArrayAccessSetExpr;
import leola.ast.ChainedAssignmentExpr;
import leola.ast.ChainedBinaryAssignmentExpr;
import leola.ast.Expr;
import leola.frontend.LeolaParser;
import leola.frontend.Token;
import leola.frontend.tokens.LeolaErrorCode;
import leola.frontend.tokens.LeolaTokenType;

/**
 * Parses an error declaration
 *
 * @author Tony
 *
 */
public class ChainedArrayAccessExprParser extends ExprParser {

	/**
	 * @param parser
	 */
	public ChainedArrayAccessExprParser(LeolaParser parser) {
		super(parser);
	}

	/* (non-Javadoc)
	 * @see leola.frontend.parsers.ExprParser#parse(leola.frontend.Token)
	 */
	@Override
	public ASTNode parse(Token firstToken) throws Exception {
		Token token = nextToken(); // eat the [

		Expr index = parseExpr(token);

        // Look for the matching ] token.        
        token = expectTokenNext(currentToken(), LeolaTokenType.RIGHT_BRACKET, LeolaErrorCode.MISSING_RIGHT_BRACKET);

        Expr expr = null;

        /* This is a set operation */
        if ( token.getType() == LeolaTokenType.EQUALS ) {
        	ChainedAssignmentExprParser parser = new ChainedAssignmentExprParser(this);
        	ChainedAssignmentExpr assignExpr = (ChainedAssignmentExpr)parser.parse(firstToken);
        	assignExpr.setLhsExpr(new ChainedArrayAccessSetExpr(index));
        	expr = assignExpr;
        }
        else if ( LeolaTokenType.BINARY_ASSIGNMENT.containsValue(token.getType()) ) {
        	ChainedBinaryAssignmentExprParser parser = new ChainedBinaryAssignmentExprParser(this);
        	ChainedBinaryAssignmentExpr assignExpr = (ChainedBinaryAssignmentExpr)parser.parse(firstToken);
        	assignExpr.setLhsExpr(new ChainedArrayAccessSetExpr(index));

        	expr = assignExpr;
        }
        else {
        	expr = new ChainedArrayAccessExpr(index);
        }

		setLineNumber(expr, firstToken);

		return expr;
	}
}

