/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.frontend.parsers;

import leola.ast.ASTNode;
import leola.ast.ChainedMemberAccessExpr;
import leola.ast.Expr;
import leola.frontend.LeolaParser;
import leola.frontend.Token;
import leola.frontend.tokens.LeolaTokenType;

/**
 * @author Tony
 *
 */
public class ChainedMemberAccessParser extends ExprParser {

    /**
     * @param parser
     */
    public ChainedMemberAccessParser(LeolaParser parser) {
        super(parser);
    }

    /* (non-Javadoc)
     * @see leola.frontend.parsers.ExprParser#parse(leola.frontend.Token)
     */
    @Override
    public ASTNode parse(Token token) throws Exception {
        Token startingToken = token;
        token = nextToken(); // eat the DOT
        
        String parentName = null;
        
        /* If this has a parent lets assign it
         * Ex. 
         * new X().myattribute[0]
         */
        if ( token.getType().equals(LeolaTokenType.IDENTIFIER) ) {
            parentName = token.getText();
        }
                        
        Expr expr = (Expr)parseIdentifier(token);
                
        ChainedMemberAccessExpr memExpr = new ChainedMemberAccessExpr(parentName, expr);
        setLineNumber(memExpr, startingToken);
        return memExpr;
    }
            
}

