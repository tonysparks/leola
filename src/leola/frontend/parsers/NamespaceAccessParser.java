/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.frontend.parsers;

import leola.ast.ASTNode;
import leola.ast.NamespaceAccessExpr;
import leola.ast.OwnableExpr;
import leola.frontend.LeolaParser;
import leola.frontend.Token;

/**
 * @author Tony
 *
 */
public class NamespaceAccessParser extends ExprParser {

	/**
	 * @param parser
	 */
	public NamespaceAccessParser(LeolaParser parser) {
		super(parser);
	}

	/* (non-Javadoc)
	 * @see leola.frontend.parsers.ExprParser#parse(leola.frontend.Token)
	 */
	@Override
	public ASTNode parse(Token token) throws Exception {
	    Token startingToken = token;
		String parentName = token.getText();

		token = nextToken(); // eat the COLON

		ASTNode node = parseIdentifier(token);
		OwnableExpr expr = (OwnableExpr)node;
		NamespaceAccessExpr memExpr = new NamespaceAccessExpr(parentName, expr.getOwner(), expr);
		memExpr.setParent(true);
		
		if( !(expr instanceof NamespaceAccessExpr) ) {
		    expr.setOwner(parentName);
		    expr.setParent(false);
		}
		
		setLineNumber(memExpr, startingToken);
		return memExpr;
	}

}

