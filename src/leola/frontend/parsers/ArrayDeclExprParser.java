/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.frontend.parsers;

import static leola.frontend.tokens.LeolaTokenType.COMMA;

import java.util.EnumSet;

import leola.ast.ASTNode;
import leola.ast.ArrayDeclExpr;
import leola.ast.Expr;
import leola.frontend.LeolaParser;
import leola.frontend.Token;
import leola.frontend.tokens.LeolaTokenType;

/**
 * Parses an error declaration
 * 
 * @author Tony
 *
 */
public class ArrayDeclExprParser extends ExprParser {

    // Synchronization set for the , token.
    protected static final EnumSet<LeolaTokenType> COMMA_SET =
        ExprParser.EXPR_START_SET.clone();
    static {
        COMMA_SET.add(COMMA);
        COMMA_SET.add(LeolaTokenType.RIGHT_BRACKET);
    };
	
	/**
	 * @param parser
	 */
	public ArrayDeclExprParser(LeolaParser parser) {
		super(parser);
	}

	/* (non-Javadoc)
	 * @see leola.frontend.parsers.ExprParser#parse(leola.frontend.Token)
	 */
	@Override
	public ASTNode parse(Token token) throws Exception {						
		Expr[] elements = ParserUtils.parseActualParameters(this, token, COMMA_SET
													, LeolaTokenType.RIGHT_BRACKET);
		
		ArrayDeclExpr expr = new ArrayDeclExpr(elements);
		setLineNumber(expr, currentToken());
		
		return expr;
	}
}

