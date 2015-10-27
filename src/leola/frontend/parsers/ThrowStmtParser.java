/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.frontend.parsers;

import leola.ast.ASTNode;
import leola.ast.Expr;
import leola.ast.ThrowStmt;
import leola.frontend.LeolaParser;
import leola.frontend.Token;

/**
 * @author Tony
 *
 */
public class ThrowStmtParser extends ExprParser {

	/**
	 * @param parser
	 */
	public ThrowStmtParser(LeolaParser parser) {
		super(parser);
	}

	/* (non-Javadoc)
	 * @see leola.frontend.parsers.StmtParser#parse(leola.frontend.Token)
	 */
	@Override
	public ASTNode parse(Token token) throws Exception {
	    Token startingToken = token;
		Token next = nextToken(); // eat the THROW keyword
		Expr expr = parseExpr(next);

		ThrowStmt retStmt = new ThrowStmt(expr);
		setLineNumber(retStmt, startingToken);
		return retStmt;
	}

}

