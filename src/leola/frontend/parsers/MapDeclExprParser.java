/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.frontend.parsers;

import static leola.frontend.tokens.LeolaTokenType.COMMA;

import java.util.EnumSet;
import java.util.List;

import leola.ast.ASTNode;
import leola.ast.Expr;
import leola.ast.MapDeclExpr;
import leola.frontend.LeolaParser;
import leola.frontend.Token;
import leola.frontend.tokens.LeolaTokenType;
import leola.vm.util.Pair;

/**
 * Parses an error declaration
 * 
 * @author Tony
 *
 */
public class MapDeclExprParser extends ExprParser {

    // Synchronization set for the , token.
    protected static final EnumSet<LeolaTokenType> COMMA_SET =
        ExprParser.EXPR_START_SET.clone();
    static {
        COMMA_SET.add(COMMA);
        COMMA_SET.add(LeolaTokenType.RIGHT_BRACE);
    };
	
	/**
	 * @param parser
	 */
	public MapDeclExprParser(LeolaParser parser) {
		super(parser);
	}

	/* (non-Javadoc)
	 * @see leola.frontend.parsers.ExprParser#parse(leola.frontend.Token)
	 */
	@Override
	public ASTNode parse(Token token) throws Exception {						
	    List<Pair<Expr, Expr>> elements = ParserUtils.parseMapParameters(this, token, COMMA_SET
													, LeolaTokenType.RIGHT_BRACE);
		
		MapDeclExpr expr = new MapDeclExpr(elements);
		setLineNumber(expr, currentToken());
		
		return expr;
	}
}

