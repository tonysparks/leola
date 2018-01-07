/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.ast;

import leola.vm.EvalException;

/**
 * IS Expression
 *
 * @author Tony
 *
 */
public class IsExpr extends Expr {

    /**
     * Expression
     */
    private Expr object;

    private String className;

    /**
     * @param object
     * @param className
     */
    public IsExpr(Expr object, String className) {        
        this.object = object;
        this.className = className;
    }

    @Override
    public void visit(ASTNodeVisitor v) throws EvalException {
        v.visit(this);
    }

    public Expr getObject() {
        return object;
    }

    /**
     * @return the className
     */
    public String getClassName() {
        return className;
    }
}

