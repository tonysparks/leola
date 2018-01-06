/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.ast;

import leola.vm.EvalException;

/**
 * @author Tony
 *
 */
public class StringExpr extends Expr {

    private String value;



    /**
     * @param value
     */
    public StringExpr(String value) {
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
    public String getValue() {
        return value;
    }
}

