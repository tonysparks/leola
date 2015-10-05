/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.frontend.parsers;

import leola.ast.ASTNode;
import leola.ast.GenDefExpr;
import leola.ast.Stmt;
import leola.frontend.LeolaParser;
import leola.frontend.Token;

/**
 * @author Tony
 *
 */
public class GenDefExprParser extends ExprParser {
	
	/**
	 * @param parser
	 */
	public GenDefExprParser(LeolaParser parser) {
		super(parser);
	}

	/* (non-Javadoc)
	 * @see leola.frontend.parsers.ExprParser#parse(leola.frontend.Token)
	 */
	@Override
	public ASTNode parse(Token token) throws Exception {
	    Token startingToken = token;
		Token next = this.nextToken(); // eat the GEN token					
		
		/* parse the parameter listings */		
		ParameterList parameters = ParserUtils.parseParameterListings(this, next);
		
		/* now parse the body */
		StmtParser parser = new StmtParser(this);
		Stmt body = (Stmt)parser.parse(currentToken());
		
		GenDefExpr defExpr = new GenDefExpr(body, parameters);
		setLineNumber(defExpr, startingToken);
		return defExpr;		
	}
}

