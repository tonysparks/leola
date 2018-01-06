/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.ast;

import leola.frontend.Token;
import leola.vm.EvalException;

/**
 * A Binary expression.
 *
 * @author Tony
 *
 */
public class BinaryExpr extends Expr {

    /**
     * Expressions
     */
    private Expr left, right;

    private Token op;

    /**
     * @param left
     * @param right
     */
    public BinaryExpr(Expr left, Expr right, Token op) {
        this.left = becomeParentOf(left);
        this.right = becomeParentOf(right);
        this.op =op;
    }


    /**
     * @return the left
     */
    public Expr getLeft() {
        return left;
    }

    /**
     * @return the right
     */
    public Expr getRight() {
        return right;
    }

    /**
     * @return the op
     */
    public Token getOp() {
        return op;
    }

    /* (non-Javadoc)
     * @see leola.ast.ASTNode#visit(leola.ast.ASTNodeVisitor)
     */
    @Override
    public void visit(ASTNodeVisitor v) throws EvalException {
        v.visit(this);
    }

}

