/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.frontend.parsers;

import java.util.EnumSet;

import leola.ast.ASTNode;
import leola.ast.Expr;
import leola.ast.Stmt;
import leola.ast.WhileStmt;
import leola.frontend.LeolaParser;
import leola.frontend.Token;
import leola.frontend.tokens.LeolaTokenType;

/**
 * @author Tony
 *
 */
public class WhileStmtParser extends StmtParser {

	/**
	 * @param parser
	 */
	public WhileStmtParser(LeolaParser parser) {
		super(parser);
	}

	 // Synchronization set for DO.
    private static final EnumSet<LeolaTokenType> DO_SET =
        StmtParser.STMT_START_SET.clone();
    static {
//        DO_SET.add(DO);
        DO_SET.addAll(StmtParser.STMT_FOLLOW_SET);
    }

    /**
     * Parse a WHILE statement.
     * @param token the initial token.
     * @return the root node of the generated parse tree.
     * @throws Exception if an error occurred.
     */
    @Override
	public ASTNode parse(Token token)
        throws Exception
    {
        Token startingToken = token;
        token = nextToken();  // consume the WHILE

        // Parse the expression.
        // The NOT node adopts the expression subtree as its only child.
        ExprParser expressionParser = new ExprParser(this);
        Expr exprNode = (Expr)expressionParser.parse(token);


        // Synchronize at the DO.
        token = synchronize(DO_SET);

        // Parse the statement.
        // The LOOP node adopts the statement subtree as its second child.
        StmtParser statementParser = new StmtParser(this);
        Stmt stmt = (Stmt)statementParser.parse(token);

        WhileStmt whileStmt = new WhileStmt(exprNode, stmt);
        setLineNumber(whileStmt, startingToken);
        return whileStmt;
    }
}

