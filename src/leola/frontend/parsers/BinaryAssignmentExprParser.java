/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.frontend.parsers;

import java.util.EnumSet;

import leola.ast.ASTNode;
import leola.ast.ArrayAccessSetExpr;
import leola.ast.AssignmentExpr;
import leola.ast.BinaryAssignmentExpr;
import leola.ast.Expr;
import leola.ast.BinaryExpr.BinaryOp;
import leola.frontend.LeolaParser;
import leola.frontend.Token;
import leola.frontend.tokens.LeolaErrorCode;
import leola.frontend.tokens.LeolaTokenType;

/**
 * @author Tony
 *
 */
public class BinaryAssignmentExprParser extends ExprParser {

	/**
	 * @param parser
	 */
	public BinaryAssignmentExprParser(LeolaParser parser) {
		super(parser);
	}

	 // Synchronization set for the := token.
    private static final EnumSet<LeolaTokenType> BINARY_ASSIGNMENT_OPS =
        ExprParser.EXPR_START_SET.clone();
    static {
    	BINARY_ASSIGNMENT_OPS.addAll(LeolaTokenType.BINARY_ASSIGNMENT.values());
        BINARY_ASSIGNMENT_OPS.add(LeolaTokenType.EQUALS);		// assignment
        BINARY_ASSIGNMENT_OPS.add(LeolaTokenType.LEFT_BRACKET); 	// array or map
        BINARY_ASSIGNMENT_OPS.addAll(StmtParser.STMT_FOLLOW_SET);
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
        
        // Parse the target variable.
    	String varName = token.getText();

    	// the left hand side (array or map) expr
    	Expr lhsExpr = null;

    	// the binary operator
    	BinaryOp binaryOp = null;

        // Synchronize on the binary assignemnt token.
        token = synchronize(BINARY_ASSIGNMENT_OPS);
        if (LeolaTokenType.BINARY_ASSIGNMENT.containsValue(token.getType()) ) {
        	LeolaTokenType type = token.getType();
        	binaryOp = type.toBinaryOp();

            token = nextToken();
        }
        else if ( token.getType() == LeolaTokenType.LEFT_BRACKET) {
        	lhsExpr = (Expr) new ExprParser(this).parse(token);
        	if ( lhsExpr instanceof AssignmentExpr ) {
        		((ArrayAccessSetExpr)lhsExpr).setVariableName(varName);
        	}

        	token = currentToken();
        }
        else {
            throwParseError(token, LeolaErrorCode.MISSING_EQUALS);
        }

        // Parse the expression.  The ASSIGN node adopts the expression's
        // node as its second child.
        ExprParser expressionParser = new ExprParser(this);
        Expr exprNode = (Expr)expressionParser.parse(token);

        // Create the ASSIGN node.
        BinaryAssignmentExpr assignNode = new BinaryAssignmentExpr(varName, lhsExpr, exprNode, binaryOp);
        setLineNumber(assignNode, startingToken);

        return assignNode;
    }
}

