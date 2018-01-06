/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.ast;

import leola.frontend.Token;
import leola.vm.EvalException;

/**
 * Unary operator
 *
 * @author Tony
 *
 */
public class UnaryExpr extends Expr {

    private Expr expr;
    private Token op;


    /**
     * @param expr
     */
    public UnaryExpr(Expr expr, Token op) {
        this.expr = becomeParentOf(expr);
        this.op = op;
    }

    /**
     * @return the op
     */
    public Token getOp() {
        return op;
    }

    /**
     * @return the expr
     */
    public Expr getExpr() {
        return expr;
    }


    /* (non-Javadoc)
     * @see leola.ast.ASTNode#visit(leola.ast.ASTNodeVisitor)
     */
    @Override
    public void visit(ASTNodeVisitor v) throws EvalException {
        v.visit(this);
    }
}

