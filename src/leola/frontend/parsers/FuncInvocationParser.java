/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.frontend.parsers;

import leola.ast.ASTNode;
import leola.ast.Expr;
import leola.ast.FuncInvocationExpr;
import leola.frontend.LeolaParser;
import leola.frontend.Token;



/**
 * @author Tony
 *
 */
public class FuncInvocationParser extends ExprParser {
    
    /**
     * @param parser
     */
    public FuncInvocationParser(LeolaParser parser) {
        super(parser);
    }
    
    /* (non-Javadoc)
     * @see leola.frontend.parsers.ExprParser#parse(leola.frontend.Token)
     */
    @Override
    public ASTNode parse(Token token) throws Exception {
        Token startingToken = token;
        String functionName = token.getText();
        Expr[] params = ParserUtils.parseArgumentExpressions(this, token);
        FuncInvocationExpr expr = new FuncInvocationExpr(functionName, params);
        setLineNumber(expr, startingToken);
        return expr;
    }
}

