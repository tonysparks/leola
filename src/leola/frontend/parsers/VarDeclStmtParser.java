/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.frontend.parsers;

import leola.ast.ASTNode;
import leola.ast.Expr;
import leola.ast.VarDeclStmt;
import leola.frontend.LeolaParser;
import leola.frontend.Token;
import leola.frontend.tokens.LeolaErrorCode;
import leola.frontend.tokens.LeolaTokenType;

/**
 * @author Tony
 *
 */
public class VarDeclStmtParser extends ExprParser {

	/**
	 * @param parser
	 */
	public VarDeclStmtParser(LeolaParser parser) {
		super(parser);
	}

	/* (non-Javadoc)
	 * @see leola.frontend.parsers.StmtParser#parse(leola.frontend.Token)
	 */
	@Override
	public ASTNode parse(Token token) throws Exception {
	    Token startingToken = token;
		token = nextToken(); // consume VAR token
		
		
		/* get the identifier */				
		expectToken(token, LeolaTokenType.IDENTIFIER, LeolaErrorCode.MISSING_IDENTIFIER);
		
		String varName = token.getText();
		
		token = nextToken();		
		expectToken(token, LeolaTokenType.EQUALS, LeolaErrorCode.INVALID_ASSIGNMENT);
		
		/* get the value expression */
		Expr value = parseExpr(nextToken());
		
		ASTNode varDecl = new VarDeclStmt(varName, value);		
		
		setLineNumber(varDecl, startingToken);
		return varDecl;
	}
}

