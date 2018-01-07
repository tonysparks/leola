/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.ast;

import leola.vm.EvalException;

/**
 * Throw statement
 *
 * @author Tony
 *
 */
public class ThrowStmt extends Stmt {

    private Expr expr;



    /**
     * @param expr
     */
    public ThrowStmt(Expr expr) {
        this.expr = expr;
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

