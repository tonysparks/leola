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
import leola.ast.Expr;
import leola.frontend.LeolaParser;
import leola.frontend.Token;
import leola.frontend.tokens.LeolaErrorCode;
import leola.frontend.tokens.LeolaTokenType;

/**
 * @author Tony
 *
 */
public class AssignmentExprParser extends ExprParser {
    
	/**
	 * @param parser
	 */
	public AssignmentExprParser(LeolaParser parser) {
		super(parser);
	}

	 // Synchronization set for the := token.
    private static final EnumSet<LeolaTokenType> COLON_EQUALS_SET =
        ExprParser.EXPR_START_SET.clone();
    static {
        COLON_EQUALS_SET.add(LeolaTokenType.EQUALS);		// assignment
        COLON_EQUALS_SET.add(LeolaTokenType.LEFT_BRACKET); 	// array or map
        COLON_EQUALS_SET.addAll(StmtParser.STMT_FOLLOW_SET);        
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
        
        // Parse the target variable.
    	String varName = token.getText();

    	// the left hand side (array or map) expr
    	Expr lhsExpr = null;
    	
        // Synchronize on the = token.
        token = synchronize(COLON_EQUALS_SET);
        if (token.getType() == LeolaTokenType.EQUALS) {
            token = nextToken();  // consume the =
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
        AssignmentExpr assignNode = new AssignmentExpr(varName, lhsExpr, exprNode);
        setLineNumber(assignNode, startingToken);
        
        return assignNode;
    }
}

