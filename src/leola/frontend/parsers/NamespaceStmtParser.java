/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.frontend.parsers;

import java.util.EnumSet;

import leola.ast.ASTNode;
import leola.ast.NamespaceStmt;
import leola.ast.Stmt;
import leola.frontend.LeolaParser;
import leola.frontend.Token;
import leola.frontend.tokens.LeolaErrorCode;
import leola.frontend.tokens.LeolaTokenType;

/**
 * @author Tony
 *
 */
public class NamespaceStmtParser extends StmtParser {

	// Synchronization set for THEN.
    private static final EnumSet<LeolaTokenType> THEN_SET =
        StmtParser.STMT_START_SET.clone();
    static {
        THEN_SET.addAll(StmtParser.STMT_FOLLOW_SET);
    }

	/**
	 * @param parser
	 */
	public NamespaceStmtParser(LeolaParser parser) {
		super(parser);
	}

	/* (non-Javadoc)
	 * @see leola.frontend.parsers.StmtParser#parse(leola.frontend.Token)
	 */
	@Override
	public ASTNode parse(Token token) throws Exception {
	    Token startingToken = token;
		token = nextToken();  // consume the NAMESPACE

		expectToken(token, LeolaTokenType.IDENTIFIER, LeolaErrorCode.MISSING_IDENTIFIER);
		
		// parse the namespace name
		String namespaceName = token.getText();

		token = nextToken();

        StmtParser statementParser = new StmtParser(this);
        Stmt stmt = (Stmt) statementParser.parse(token);

        token = currentToken();

        NamespaceStmt namespaceStmt = new NamespaceStmt(stmt, namespaceName);
        setLineNumber(namespaceStmt, startingToken);
        return namespaceStmt;
	}

}

