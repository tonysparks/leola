/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.frontend.parsers;

import leola.ast.ASTNode;
import leola.ast.Expr;
import leola.ast.Stmt;
import leola.ast.WhileStmt;
import leola.frontend.LeolaParser;
import leola.frontend.Token;

/**
 * @author Tony
 *
 */
public class WhileStmtParser extends ExprParser {

	/**
	 * @param parser
	 */
	public WhileStmtParser(LeolaParser parser) {
		super(parser);
	}

    /**
     * Parse a WHILE statement.
     * @param token the initial token.
     * @return the root node of the generated parse tree.
     * @throws Exception if an error occurred.
     */
    @Override
	public ASTNode parse(Token token)
        throws Exception
    {
        Token startingToken = token;
        token = nextToken();  // consume the WHILE

        // Parse the expression; while EXPR
        Expr exprNode = parseExpr(token);

        // Parse the statement; while EXPR STMT 
        Stmt stmt = parseStmt(currentToken());

        WhileStmt whileStmt = new WhileStmt(exprNode, stmt);
        setLineNumber(whileStmt, startingToken);
        return whileStmt;
    }
}

