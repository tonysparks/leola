/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.frontend.parsers;

import leola.ast.ASTNode;
import leola.ast.FuncDefExpr;
import leola.ast.Stmt;
import leola.frontend.LeolaParser;
import leola.frontend.Token;

/**
 * @author Tony
 *
 */
public class FuncDefExprParser extends ExprParser {
	
	/**
	 * @param parser
	 */
	public FuncDefExprParser(LeolaParser parser) {
		super(parser);
	}

	/* (non-Javadoc)
	 * @see leola.frontend.parsers.ExprParser#parse(leola.frontend.Token)
	 */
	@Override
	public ASTNode parse(Token token) throws Exception {
		Token next = this.nextToken(); // eat the DEF token					
		
		/* parse the parameter listings */		
		ParameterList parameters = ParserUtils.parseParameterListings(this, next);
		
		/* now parse the body */
		StmtParser parser = new StmtParser(this);
		Stmt body = (Stmt)parser.parse(currentToken());
		
		FuncDefExpr defExpr = new FuncDefExpr(body, parameters);
		setLineNumber(defExpr, currentToken());
		return defExpr;		
	}
}

