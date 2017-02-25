/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.frontend.parsers;

import leola.ast.ASTNode;
import leola.ast.Expr;
import leola.ast.NamedParameterExpr;
import leola.ast.CatchStmt;
import leola.frontend.LeolaParser;
import leola.frontend.Token;



/**
 * Named Parameter Statement Parser
 *
 * @author Tony
 *
 */
public class NamedParameterExprParser extends StmtParser {

    /**
     * @param parser
     */
    public NamedParameterExprParser(LeolaParser parser) {
        super(parser);
    }


    /**
     * Parse the {@link CatchStmt}
     * 
     * @param token the initial token.
     * @return the root node of the generated parse tree.
     * @throws Exception if an error occurred.
     */
    @Override
    public ASTNode parse(Token token)
        throws Exception {        
        
        Token startingToken = token;
        String parameterName = token.getText();
        
        Expr valueExpr = new ExprParser(this, true).parseExpr(nextToken());
        
        NamedParameterExpr stmt = new NamedParameterExpr(parameterName, valueExpr);
        setLineNumber(stmt, startingToken);
        return stmt;
    }

}

