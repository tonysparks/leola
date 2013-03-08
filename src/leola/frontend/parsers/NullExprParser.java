/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.frontend.parsers;

import leola.ast.ASTNode;
import leola.ast.NullExpr;
import leola.frontend.LeolaParser;
import leola.frontend.Token;

/**
 * @author Tony
 *
 */
public class NullExprParser extends ExprParser {

	/**
	 * @param parser
	 */
	public NullExprParser(LeolaParser parser) {
		super(parser);
	}
	
	/* (non-Javadoc)
	 * @see leola.frontend.parsers.ExprParser#parse(leola.frontend.Token)
	 */
	@Override
	public ASTNode parse(Token token) throws Exception {
		nextToken(); // eat the NULL
		NullExpr expr = new NullExpr();
		setLineNumber(expr, currentToken());
		return expr;
	}

}

