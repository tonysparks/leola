/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.ast;

import leola.frontend.Token;
import leola.vm.EvalException;


/**
 * Accounts for setting an array access expressions:
 * 
 * <pre>
 *  array[10] = "x"
 * </pre>
 * 
 * @author Tony
 *
 */
public class SubscriptSetExpr extends Expr {

    /**
     * element index
     */
    private Expr object;
    private Expr elementIndex;
    private Expr value;
    private Token operator;

    /**
     * @param elementIndex
     */
    public SubscriptSetExpr(Expr object, Expr elementIndex, Expr value, Token operator) {
        this.object = object;
        this.elementIndex = becomeParentOf(elementIndex);
        this.value = value;
        this.operator = operator;
    }

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
    
    public Expr getValue() {
        return value;
    }
    
    public Token getOperator() {
        return operator;
    }
}

