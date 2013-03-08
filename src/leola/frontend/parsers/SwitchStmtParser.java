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
import leola.ast.Expr;
import leola.ast.Stmt;
import leola.ast.SwitchStmt;
import leola.frontend.LeolaParser;
import leola.frontend.Token;
import leola.frontend.tokens.LeolaErrorCode;
import leola.frontend.tokens.LeolaTokenType;
import leola.vm.asm.Pair;

/**
 * The Switch Statement Parser:
 *
 * <pre>
 * switch (expr) {
 *   when (expr) -> (stmt)
 *   when (expr) -> (stmt)
 *   else (stmt)
 * }
 * </pre>
 *
 * @author Tony
 *
 */
public class SwitchStmtParser extends StmtParser {

	/*
	 * TODO - Merge code from CASE statement parser
	 */

	// Synchronization set for THEN.
    private static final EnumSet<LeolaTokenType> SWITCH_SET =
        EnumSet.of(LEFT_BRACE, WHEN);

	/**
	 * @param parser
	 */
	public SwitchStmtParser(LeolaParser parser) {
		super(parser);
	}

	/* (non-Javadoc)
	 * @see leola.frontend.parsers.StmtParser#parse(leola.frontend.Token)
	 */
	@Override
	public ASTNode parse(Token token) throws Exception {
		token = nextToken();  // consume the SWITCH

		Expr conditionNode = null;
		Stmt elseStmt = null;
		List<Pair<Expr, Stmt>> whenStmts = new ArrayList<Pair<Expr,Stmt>>();

		// if { or WHEN, put in a TRUE
		LeolaTokenType type = token.getType();
		if ( SWITCH_SET.contains(type) ) {
			conditionNode = new BooleanExpr(true);
		}
		else {
	        // Parse the expression.
	        // The CASE node adopts the expression subtree as its first child.
	        ExprParser expressionParser = new ExprParser(this);
	        conditionNode = (Expr)expressionParser.parse(token);
		}

        boolean hasOpeningBrace = false;

        // Synchronize at the { or WHEN.
        token = synchronize(SWITCH_SET);
        type = token.getType();

        if ( type.equals(WHEN) ) {
            token = nextToken(); // eat the token ({ or WHEN)

            Pair<Expr, Stmt> whenExpr = parseWhen(token);
            whenStmts.add(whenExpr);

            eatOptionalStmtEnd(currentToken());

            token = currentToken();

            if ( token.getType().equals(ELSE) ) {
                token = nextToken(); // eat ELSE
                elseStmt = (Stmt)(new StmtParser(this).parse(token));
            }

        }
        else if ( type.equals(LEFT_BRACE) ) {
            hasOpeningBrace = true;

            token = nextToken(); // eat the token {
            type = token.getType();

            while( type.equals(WHEN)) {
                token = nextToken(); // eat the token WHEN

                Pair<Expr, Stmt> whenExpr = parseWhen(token);
                whenStmts.add(whenExpr);

                eatOptionalStmtEnd(currentToken());
                type = currentToken().getType();

            }

            token = currentToken();

            if ( token.getType().equals(ELSE) ) {
                token = nextToken(); // eat ELSE
                elseStmt = (Stmt)(new StmtParser(this).parse(token));
            }

        }
        else {
            getExceptionHandler().errorToken(token, this, LeolaErrorCode.MISSING_LEFT_BRACE);
        }

        token = currentToken();
        if ( token.getType().equals(RIGHT_BRACE) && hasOpeningBrace) {
            nextToken();
        }
        else if ( hasOpeningBrace ) {
            getExceptionHandler().errorToken(token, this, LeolaErrorCode.MISSING_RIGHT_BRACE);
        }

        SwitchStmt switchStmt = new SwitchStmt(conditionNode, whenStmts, elseStmt);
        setLineNumber(switchStmt, currentToken());

        return switchStmt;
	}

	private Pair<Expr, Stmt> parseWhen(Token token) throws Exception {
	    Pair<Expr, Stmt> whenExprPair = new Pair<Expr, Stmt>();

	    ExprParser expressionParser = new ExprParser(this);
	    Expr whenExpr = (Expr)expressionParser.parse(token);
	    whenExprPair.setFirst(whenExpr);

	    token = currentToken();
	    if ( ! token.getType().equals(LeolaTokenType.ARROW)) {
	        getExceptionHandler().errorToken(token, this, LeolaErrorCode.MISSING_ARROW);
	    }
	    else {
	        token = nextToken();
	    }

	    Stmt valueStmt = (Stmt)(new StmtParser(this).parse(token));
	    whenExprPair.setSecond(valueStmt);

	    return whenExprPair;
	}
}

