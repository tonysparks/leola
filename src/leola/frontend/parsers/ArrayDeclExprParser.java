/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.frontend.parsers;

import leola.ast.ASTNode;
import leola.ast.ArrayDeclExpr;
import leola.ast.Expr;
import leola.frontend.LeolaParser;
import leola.frontend.Token;

/**
 * Parses an error declaration
 * 
 * @author Tony
 *
 */
public class ArrayDeclExprParser extends ExprParser {
    
    /**
     * @param parser
     */
    public ArrayDeclExprParser(LeolaParser parser) {
        super(parser);
    }

    /* (non-Javadoc)
     * @see leola.frontend.parsers.ExprParser#parse(leola.frontend.Token)
     */
    @Override
    public ASTNode parse(Token token) throws Exception {                        
        Expr[] elements = ParserUtils.parseArrayDeclaration(this, token);
        
        ArrayDeclExpr expr = new ArrayDeclExpr(elements);
        setLineNumber(expr, token);
        
        return expr;
    }
}

