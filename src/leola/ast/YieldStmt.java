/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.ast;

import leola.vm.EvalException;

/**
 * Return statement
 *
 * @author Tony
 *
 */
public class YieldStmt extends Stmt {

    private Expr expr;



    /**
     * @param expr
     */
    public YieldStmt(Expr expr) {
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
     * @return the expr
     */
    public Expr getExpr() {
        return expr;
    }
}

