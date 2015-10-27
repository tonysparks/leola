/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.frontend.parsers;

import leola.ast.ASTNode;
import leola.ast.Expr;
import leola.ast.IfStmt;
import leola.ast.Stmt;
import leola.frontend.LeolaParser;
import leola.frontend.Token;
import leola.frontend.tokens.LeolaTokenType;

/**
 * @author Tony
 *
 */
public class IfStmtParser extends ExprParser {

	/**
	 * @param parser
	 */
	public IfStmtParser(LeolaParser parser) {
		super(parser);
	}
	
	/* (non-Javadoc)
	 * @see leola.frontend.parsers.StmtParser#parse(leola.frontend.Token)
	 */
	@Override
	public ASTNode parse(Token token) throws Exception {
	    Token startingToken = token;
		token = nextToken();  // consume the IF
		
		IfStmt ifStmt = null;
		
		// the expression if EXPR
        Expr exprNode = parseExpr(token);
        
        token = currentToken();
                
        // the statement if EXPR STMT
        Stmt thenStmt = parseStmt(token);
        token = currentToken();

        // Look for an ELSE.
        if (token.getType() == LeolaTokenType.ELSE) {
            token = nextToken();  // consume the ELSE

            // Parse the ELSE statement.
            // The IF node adopts the statement subtree as its third child.
            Stmt elseStmt = parseStmt(token);
            ifStmt = new IfStmt(exprNode, thenStmt, elseStmt);
        }
        else {
        	ifStmt = new IfStmt(exprNode, thenStmt);
        }

        setLineNumber(ifStmt, startingToken);
        return ifStmt;
	}

}

