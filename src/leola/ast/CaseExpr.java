/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.ast;

import java.util.List;

import leola.frontend.EvalException;
import leola.vm.util.Pair;

/**
 * Case Expression
 *
 * @author Tony
 *
 */
public class CaseExpr extends Expr {

    private Expr condition;
    private List<Pair<Expr, Expr>> whenExprs;
    private Expr elseExpr;

    /**
     * @param condition
     * @param whenExprs
     */
    public CaseExpr(Expr condition, List<Pair<Expr, Expr>> whenExprs) {
        this(condition, whenExprs, null);
    }

    /**
     * @param condition
     * @param whenExprs
     * @param elseExpr
     */
    public CaseExpr(Expr condition, List<Pair<Expr, Expr>> whenExprs, Expr elseExpr) {
        this.condition = becomeParentOf(condition);
        this.whenExprs = whenExprs;
        this.elseExpr = becomeParentOf(elseExpr);
        
        for(Pair<Expr, Expr> p : whenExprs) {
            becomeParentOf(p.getFirst());
            becomeParentOf(p.getSecond());
        }
    }


    /* (non-Javadoc)
     * @see leola.ast.ASTNode#visit(leola.ast.ASTNodeVisitor)
     */
    @Override
    public void visit(ASTNodeVisitor v) throws EvalException {
        v.visit(this);
    }

    /**
     * @return the condition
     */
    public Expr getCondition() {
        return condition;
    }

    /**
     * @return the whenExprs
     */
    public List<Pair<Expr, Expr>> getWhenExprs() {
        return this.whenExprs;
    }

    /**
     * @return the elseExpr
     */
    public Expr getElseExpr() {
        return this.elseExpr;
    }

}

