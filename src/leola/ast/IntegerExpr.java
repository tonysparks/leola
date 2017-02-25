/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.ast;

import leola.frontend.EvalException;


/**
 * Number expression
 *
 * @author Tony
 *
 */
public class IntegerExpr extends Expr {

    private int value;



    /**
     * @param value
     */
    public IntegerExpr(int value) {
        this.value = value;
    }



    /* (non-Javadoc)
     * @see leola.ast.ASTNode#visit(leola.ast.ASTNodeVisitor)
     */
    @Override
    public void visit(ASTNodeVisitor v) throws EvalException {
        v.visit(this);
    }

    /**
     * @return the value
     */
    public int getValue() {
        return value;
    }
}

