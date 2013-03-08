/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.frontend.parsers;

import java.util.EnumSet;

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
public class IfStmtParser extends StmtParser {

	// Synchronization set for THEN.
    private static final EnumSet<LeolaTokenType> THEN_SET =
        StmtParser.STMT_START_SET.clone();
    static {        
        THEN_SET.addAll(StmtParser.STMT_FOLLOW_SET);
    }
	
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
		token = nextToken();  // consume the IF
		
		IfStmt ifStmt = null;
		
        // Parse the expression.
        // The IF node adopts the expression subtree as its first child.
        ExprParser expressionParser = new ExprParser(this);
        Expr exprNode = (Expr)expressionParser.parse(token);
        
        // Synchronize at the THEN.
        token = synchronize(THEN_SET);
        /*if (token.getType() == LeolaTokenType.LEFT_BRACE) {
            token = nextToken();  // consume the {
        }
        else {            
            getExceptionHandler().errorToken(token, this, LeolaErrorCode.MISSING_LEFT_BRACE);
        }*/

        // Parse the THEN statement.
        // The IF node adopts the statement subtree as its second child.
        StmtParser statementParser = new StmtParser(this);
        Stmt thenStmt = (Stmt)statementParser.parse(token);
        token = currentToken();

        // Look for an ELSE.
        if (token.getType() == LeolaTokenType.ELSE) {
            token = nextToken();  // consume the ELSE

            // Parse the ELSE statement.
            // The IF node adopts the statement subtree as its third child.
            Stmt elseStmt = (Stmt)statementParser.parse(token);
            ifStmt = new IfStmt(exprNode, thenStmt, elseStmt);
        }
        else {
        	ifStmt = new IfStmt(exprNode, thenStmt);
        }

        setLineNumber(ifStmt, currentToken());
        return ifStmt;
	}

}

