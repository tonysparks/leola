/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.frontend.parsers;

import leola.ast.ASTNode;
import leola.ast.CatchStmt;
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
        Token startingToken = token;
        Token next = nextToken(); // eat the TRY keyword        
        Stmt stmt = parseStmt(next);
        
        boolean hasProperEnding = false;
        
        CatchStmt onStmt = null;
        if(currentToken().getType().equals(LeolaTokenType.CATCH)) {        
            onStmt = (CatchStmt)new CatchStmtParser(this).parse(next);
            hasProperEnding = true;
        }
        
        Stmt finallyStmt = null;
        if(currentToken().getType().equals(LeolaTokenType.FINALLY)) {        
            finallyStmt = parseStmt(nextToken()); // eat the finally key word
            hasProperEnding = true;
        }
        
        if(!hasProperEnding) {
            throwParseError(currentToken(), LeolaErrorCode.MISSING_CATCH_OR_FINALLY);
        }
        
        TryStmt tryStmt = new TryStmt(stmt, onStmt, finallyStmt);
        setLineNumber(tryStmt, startingToken);
        return tryStmt;
    }

}

