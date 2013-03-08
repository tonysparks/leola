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
public class ThrowStmtParser extends StmtParser {

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
		Token next = nextToken(); // eat the THROW keyword
		ExprParser retExpr = new ExprParser(this);
		Expr expr = (Expr)retExpr.parse(next);

		ThrowStmt retStmt = new ThrowStmt(expr);
		setLineNumber(retStmt, currentToken());
		return retStmt;
	}

}

