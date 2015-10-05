/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.frontend.parsers;

import leola.ast.ASTNode;
import leola.ast.Expr;
import leola.ast.ReturnStmt;
import leola.frontend.LeolaParser;
import leola.frontend.Token;

/**
 * @author Tony
 *
 */
public class ReturnStmtParser extends StmtParser {

	/**
	 * @param parser
	 */
	public ReturnStmtParser(LeolaParser parser) {
		super(parser);
	}
	
	/* (non-Javadoc)
	 * @see leola.frontend.parsers.StmtParser#parse(leola.frontend.Token)
	 */
	@Override
	public ASTNode parse(Token token) throws Exception {
	    Token startingToken = token;
		Token next = nextToken(); // eat the RETURN keyword
		ExprParser retExpr = new ExprParser(this);
		Expr expr = (Expr)retExpr.parse(next);
		
		ReturnStmt retStmt = new ReturnStmt(expr);
		setLineNumber(retStmt, startingToken);
		return retStmt;
	}

}

