/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.frontend.parsers;

import leola.ast.ASTNode;
import leola.ast.OnStmt;
import leola.ast.Stmt;
import leola.ast.TryStmt;
import leola.frontend.LeolaParser;
import leola.frontend.Token;
import leola.frontend.tokens.LeolaErrorCode;
import leola.frontend.tokens.LeolaTokenType;

/**
 * @author Tony
 *
 */
public class TryStmtParser extends StmtParser {

	/**
	 * @param parser
	 */
	public TryStmtParser(LeolaParser parser) {
		super(parser);
	}

	/* (non-Javadoc)
	 * @see leola.frontend.parsers.StmtParser#parse(leola.frontend.Token)
	 */
	@Override
	public ASTNode parse(Token token) throws Exception {
		Token next = nextToken(); // eat the TRY keyword
		StmtParser stmtParser = new StmtParser(this);
		Stmt stmt = (Stmt)stmtParser.parse(next);
		
		boolean hasProperEnding = false;
		
		OnStmt onStmt = null;
		if(currentToken().getType().equals(LeolaTokenType.ON)) {		
			onStmt = (OnStmt)new OnStmtParser(this).parse(next);
			hasProperEnding = true;
		}
		
		Stmt finallyStmt = null;
		if(currentToken().getType().equals(LeolaTokenType.FINALLY)) {		
			finallyStmt = (Stmt)new StmtParser(this).parse(nextToken()); // eat the finally key word
			hasProperEnding = true;
		}
		
		if(!hasProperEnding) {
			getExceptionHandler().errorToken(currentToken(), this, LeolaErrorCode.MISSING_ON_OR_FINALLY);
		}
		
		TryStmt tryStmt = new TryStmt(stmt, onStmt, finallyStmt);
		setLineNumber(tryStmt, currentToken());
		return tryStmt;
	}

}

