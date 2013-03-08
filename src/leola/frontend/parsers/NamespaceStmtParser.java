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
	public ASTNode parse(Token tok) throws Exception {
		Token token = nextToken();  // consume the NAMESPACE

		if ( ! token.getType().equals(LeolaTokenType.IDENTIFIER) ) {
			getExceptionHandler().errorToken(token, this, LeolaErrorCode.MISSING_IDENTIFIER);
		}

		// parse the namespace name
		String namespaceName = token.getText();

		token = nextToken();

//		if (token.getType() == LeolaTokenType.LEFT_BRACE) {
//	        token = nextToken();  // consume the {
//	    }
//	    else {
//	        getExceptionHandler().errorToken(token, this, LeolaErrorCode.MISSING_LEFT_BRACE);
//	    }


        StmtParser statementParser = new StmtParser(this);
        Stmt stmt = (Stmt) statementParser.parse(token);

        token = currentToken();

        // Look for an }
//        if (token.getType() == LeolaTokenType.RIGHT_BRACE) {
//            token = nextToken();  // consume the }
//        }
//        else {
//	        getExceptionHandler().errorToken(token, this, LeolaErrorCode.MISSING_RIGHT_BRACE);
//        }

        NamespaceStmt namespaceStmt = new NamespaceStmt(stmt, namespaceName);
        setLineNumber(namespaceStmt, currentToken());
        return namespaceStmt;
	}

}

