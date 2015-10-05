/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.frontend.parsers;

import leola.ast.ASTNode;
import leola.ast.Expr;
import leola.ast.NewExpr;
import leola.frontend.LeolaParser;
import leola.frontend.Token;
import leola.frontend.tokens.LeolaErrorCode;
import leola.frontend.tokens.LeolaTokenType;

/**
 * @author Tony
 *
 */
public class NewExprParser extends FuncInvocationParser {

	/**
	 * @param parser
	 */
	public NewExprParser(LeolaParser parser) {
		super(parser);
	}

	/* (non-Javadoc)
	 * @see leola.frontend.parsers.ExprParser#parse(leola.frontend.Token)
	 */
	@Override
	public ASTNode parse(Token token) throws Exception {
	    Token startingToken = token;
		token = nextToken(); // eat the NEW

		/* Native classes include '.', so we must continue until a '(' */
		String className = parseClassName(token);

		Expr[] params = ParserUtils.parseArgumentExpressions(this, token);
		NewExpr expr = new NewExpr(className, params);
		setLineNumber(expr, startingToken);
		return expr;
	}

	/**
	 * Parses the class name.
	 *
	 * @param token
	 * @return
	 * @throws Exception
	 */
	private String parseClassName(Token token) throws Exception {
		String className = "";

		LeolaTokenType type = token.getType();
		while(! type.equals(LeolaTokenType.LEFT_PAREN)) {
			if ( type.equals(LeolaTokenType.IDENTIFIER) ) {
				className += token.getText();
			}
			else if ( type.equals(LeolaTokenType.DOT)) {
				className += ".";
			}
			else if ( type.equals(LeolaTokenType.COLON)) {
				className += ":";
			}
			else {
				getExceptionHandler().errorToken(token, this, LeolaErrorCode.UNEXPECTED_TOKEN);
			}

			token = nextToken();
			type = token.getType();
		}

		return className;
	}
}

