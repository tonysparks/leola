/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.frontend.parsers;

import leola.ast.ASTNode;
import leola.ast.Expr;
import leola.ast.YieldStmt;
import leola.frontend.LeolaParser;
import leola.frontend.Token;

/**
 * @author Tony
 *
 */
public class YieldStmtParser extends ExprParser {

    /**
     * @param parser
     */
    public YieldStmtParser(LeolaParser parser) {
        super(parser);
    }
    
    /* (non-Javadoc)
     * @see leola.frontend.parsers.StmtParser#parse(leola.frontend.Token)
     */
    @Override
    public ASTNode parse(Token token) throws Exception {
        Token startingToken = token;
        Token next = nextToken(); // eat the YIELD keyword
                
        Expr expr = parseExpr(next);
        
        YieldStmt retStmt = new YieldStmt(expr);
        setLineNumber(retStmt, startingToken);
        return retStmt;
    }

}

