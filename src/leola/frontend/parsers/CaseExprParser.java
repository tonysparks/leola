/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.frontend.parsers;

import static leola.frontend.tokens.LeolaTokenType.ELSE;
import static leola.frontend.tokens.LeolaTokenType.LEFT_BRACE;
import static leola.frontend.tokens.LeolaTokenType.RIGHT_BRACE;
import static leola.frontend.tokens.LeolaTokenType.WHEN;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import leola.ast.ASTNode;
import leola.ast.BooleanExpr;
import leola.ast.CaseExpr;
import leola.ast.Expr;
import leola.frontend.LeolaParser;
import leola.frontend.Token;
import leola.frontend.tokens.LeolaErrorCode;
import leola.frontend.tokens.LeolaTokenType;
import leola.vm.util.Pair;

/**
 * The Case Expression Parser:
 *
 * <pre>
 * case (expr) {
 *   when (expr) -> (expr)
 *   when (expr) -> (expr)
 *   else (expr)
 * }
 * </pre>
 *
 * @author Tony
 *
 */
public class CaseExprParser extends ExprParser {

    private static final EnumSet<LeolaTokenType> CASE_SET =
        EnumSet.of(LEFT_BRACE, WHEN);

	/**
	 * @param parser
	 */
	public CaseExprParser(LeolaParser parser) {
		super(parser);
	}

	/* (non-Javadoc)
	 * @see leola.frontend.parsers.StmtParser#parse(leola.frontend.Token)
	 */
	@Override
	public ASTNode parse(Token token) throws Exception {
	    Token startingToken = token;
		token = nextToken();  // consume the CASE

		Expr conditionNode = null;
		Expr elseExpr = null;
		List<Pair<Expr, Expr>> whenExprs = new ArrayList<Pair<Expr,Expr>>();

		// if { or WHEN, put in a TRUE
		LeolaTokenType type = token.getType();
		if ( CASE_SET.contains(type) ) {
			conditionNode = new BooleanExpr(true);
		}
		else {
	        // Parse the expression.
	        // The CASE node adopts the expression subtree as its first child.
	        conditionNode = parseExpr(token);
		}

        boolean hasOpeningBrace = false;

        // expect either the { or WHEN.
        token = expectedTokens(CASE_SET);
        type = token.getType();

        if ( type.equals(WHEN) ) {
            token = nextToken(); // eat the token ({ or WHEN)

            Pair<Expr, Expr> whenExpr = parseWhen(token);
            whenExprs.add(whenExpr);

            eatOptionalStmtEnd(currentToken());

            token = currentToken();

            if ( token.getType().equals(ELSE) ) {
                token = nextToken(); // eat ELSE
                elseExpr = parseExpr(token);
            }

        }
        else if ( type.equals(LEFT_BRACE) ) {
            hasOpeningBrace = true;

            token = nextToken(); // eat the token {
            type = token.getType();

            while( type.equals(WHEN)) {
                token = nextToken(); // eat the token WHEN

                Pair<Expr, Expr> whenExpr = parseWhen(token);
                whenExprs.add(whenExpr);

                eatOptionalStmtEnd(currentToken());
                type = currentToken().getType();

            }

            token = currentToken();

            if ( token.getType().equals(ELSE) ) {
                token = nextToken(); // eat ELSE
                elseExpr = parseExpr(token);
            }

        }
        else {
            throwParseError(token, LeolaErrorCode.MISSING_LEFT_BRACE);
        }

        token = currentToken();
        if ( token.getType().equals(RIGHT_BRACE) && hasOpeningBrace) {
            nextToken();
        }
        else if ( hasOpeningBrace ) {
            throwParseError(token, LeolaErrorCode.MISSING_RIGHT_BRACE);
        }

        CaseExpr caseExpr = new CaseExpr(conditionNode, whenExprs, elseExpr);
        setLineNumber(caseExpr, startingToken);

        return caseExpr;
	}

	private Pair<Expr, Expr> parseWhen(Token token) throws Exception {
	    Pair<Expr, Expr> whenExprPair = new Pair<Expr, Expr>();

	    Expr whenExpr = parseExpr(token);
	    whenExprPair.setFirst(whenExpr);

	    token = expectTokenNext(currentToken(), LeolaTokenType.ARROW, LeolaErrorCode.MISSING_ARROW);
	    
	    Expr valueExpr = parseExpr(token);
	    whenExprPair.setSecond(valueExpr);

	    return whenExprPair;
	}
}

