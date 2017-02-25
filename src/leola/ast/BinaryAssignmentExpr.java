/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.ast;

import leola.ast.BinaryExpr.BinaryOp;
import leola.frontend.EvalException;

/**
 * Binary Assignment expression
 *
 * @author Tony
 *
 */
public class BinaryAssignmentExpr extends OwnableExpr {

    /**
     * Var name
     */
    private String varName;

    /**
     * Expr to assign
     */
    private Expr expr;

    /**
     * If this is a map or an array index assignment
     */
    private Expr lhsExpr;

    /**
     * Binary operator
     */
    private BinaryOp binaryOp;

    /**
     * @param varName
     * @param lhsExpr
     * @param expr
     * @param binaryOp
     */
    public BinaryAssignmentExpr(String varName, Expr lhsExpr, Expr expr, BinaryOp binaryOp) {
        this(varName, varName, lhsExpr, expr, binaryOp);
    }

    /**
     * @param owner
     * @param varName
     * @param lhsExpr
     * @param expr
     * @param binaryOp
     */
    public BinaryAssignmentExpr(String owner, String varName, Expr lhsExpr, Expr expr, BinaryOp binaryOp) {
        super(owner);
        this.varName = varName;
        this.lhsExpr = becomeParentOf(lhsExpr);
        this.expr = becomeParentOf(expr);
        this.binaryOp = binaryOp;
    }



    /* (non-Javadoc)
     * @see leola.ast.ASTNode#visit(leola.ast.ASTNodeVisitor)
     */
    @Override
    public void visit(ASTNodeVisitor v) throws EvalException {
        v.visit(this);
    }

    /**
     * @return the varName
     */
    public String getVarName() {
        return varName;
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

    /**
     * @return the binaryOp
     */
    public BinaryOp getBinaryOp() {
        return binaryOp;
    }
}

