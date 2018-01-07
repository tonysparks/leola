/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.ast;

import leola.vm.EvalException;

/**
 * Accounts for array access expressions:
 * 
 * <pre>
 *  array[10]
 * </pre>
 * 
 * @author Tony
 *
 */
public class SubscriptGetExpr extends Expr {

    /**
     * element index
     */
    private Expr elementIndex;

    /**
     * Variable name
     */
    private Expr object;


    /**
     * @param elementIndex
     */
    public SubscriptGetExpr(Expr object, Expr elementIndex) {
        this.object = object;
        this.elementIndex = becomeParentOf(elementIndex); 
    }



    /* (non-Javadoc)
     * @see leola.ast.ASTNode#visit(leola.ast.ASTNodeVisitor)
     */
    @Override
    public void visit(ASTNodeVisitor v) throws EvalException {
        v.visit(this);
    }

    public Expr getObject() {
        return object;
    }

    /**
     * @return the elementIndex
     */
    public Expr getElementIndex() {
        return elementIndex;
    }
}

