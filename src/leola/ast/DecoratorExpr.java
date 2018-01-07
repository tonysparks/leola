/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.ast;

import java.util.List;

import leola.vm.EvalException;

/**
 * Decorator expression
 *
 * @author Tony
 *
 */
public class DecoratorExpr extends Expr {

    private VarExpr decoratorName;
    private List<Expr> arguments;
    private Expr decoratedExpr;

    
    
    /**
     * @param decoratorName
     * @param arguments
     * @param decoratedExpr
     */
    public DecoratorExpr(VarExpr decoratorName, List<Expr> arguments, Expr decoratedExpr) {
        this.decoratorName = decoratorName;
        this.arguments = arguments;
        this.decoratedExpr = becomeParentOf(decoratedExpr);
    }

    @Override
    public void visit(ASTNodeVisitor v) throws EvalException {
        v.visit(this);
    }

    /**
     * @return the decoratorName
     */
    public VarExpr getDecoratorName() {
        return decoratorName;
    }

    public Expr getDecoratedExpr() {
        return decoratedExpr;
    }

    
    public List<Expr> getArguments() {
        return arguments;
    }

}

