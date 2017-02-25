/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.frontend.parsers;

import leola.ast.ASTNode;
import leola.ast.MemberAccessExpr;
import leola.ast.OwnableExpr;
import leola.frontend.LeolaParser;
import leola.frontend.Token;

/**
 * @author Tony
 *
 */
public class MemberAccessParser extends ExprParser {

    /**
     * @param parser
     */
    public MemberAccessParser(LeolaParser parser) {
        super(parser);
    }

    /* (non-Javadoc)
     * @see leola.frontend.parsers.ExprParser#parse(leola.frontend.Token)
     */
    @Override
    public ASTNode parse(Token token) throws Exception {
        Token startingToken = token;
        String parentName = token.getText();

        token = nextToken(); // eat the DOT
        
        ASTNode node = parseIdentifier(token);
        OwnableExpr expr = (OwnableExpr)node;                
        MemberAccessExpr memExpr = new MemberAccessExpr(parentName, expr.getOwner(), expr);
        memExpr.setParent(true);
        
        expr.setOwner(parentName);
        expr.setParent(false);
        
        setLineNumber(memExpr, startingToken);
        return memExpr;
    }

}

