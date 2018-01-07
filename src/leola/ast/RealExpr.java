/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.ast;

import leola.vm.EvalException;


/**
 * Number expression
 *
 * @author Tony
 *
 */
public class RealExpr extends Expr {

    private double value;



    /**
     * @param value
     */
    public RealExpr(double value) {
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
    public double getValue() {
        return value;
    }
}

