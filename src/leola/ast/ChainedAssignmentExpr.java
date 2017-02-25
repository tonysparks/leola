/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.ast;

import leola.frontend.EvalException;

/**
 * Assignment statement
 *
 * @author Tony
 *
 */
public class ChainedAssignmentExpr extends Expr {

    /**
     * Expr to assign
     */
    private Expr expr;

    /**
     * If this is a map or an array index assignment
     */
    private Expr lhsExpr;


    /**
     * @param lhsExpr
     * @param expr
     */
    public ChainedAssignmentExpr(Expr lhsExpr, Expr expr) {
        this.lhsExpr = becomeParentOf(lhsExpr);
        this.expr = becomeParentOf(expr);
    }



    /* (non-Javadoc)
     * @see leola.ast.ASTNode#visit(leola.ast.ASTNodeVisitor)
     */
    @Override
    public void visit(ASTNodeVisitor v) throws EvalException {
        v.visit(this);
    }

    /**
     * @return the lhsExpr
     */
    public Expr getLhsExpr() {
        return lhsExpr;
    }

    /**
     * @param lhsExpr the lhsExpr to set
     */
    public void setLhsExpr(Expr lhsExpr) {
        this.lhsExpr = becomeParentOf(lhsExpr);
    }

    /**
     * @return the expr
     */
    public Expr getExpr() {
        return expr;
    }
}

