/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.ast;

import leola.frontend.EvalException;

/**
 * Try statement
 *
 * @author Tony
 *
 */
public class TryStmt extends Stmt {

    private Stmt stmt;
    private CatchStmt catchStmt;
    private Stmt finallyStmt;

    /**
     * @param stmt
     * @param catchStmt
     * @param finallyStmt
     */
    public TryStmt(Stmt stmt, CatchStmt catchStmt, Stmt finallyStmt) {
        this.stmt = becomeParentOf(stmt);
        this.catchStmt = becomeParentOf(catchStmt);
        this.finallyStmt = becomeParentOf(finallyStmt);
    }



    /* (non-Javadoc)
     * @see leola.ast.ASTNode#visit(leola.ast.ASTNodeVisitor)
     */
    @Override
    public void visit(ASTNodeVisitor v) throws EvalException {
        v.visit(this);
    }

    /**
     * @return the stmt
     */
    public Stmt getStmt() {
        return stmt;
    }
    
    /**
     * @return the catchStmt
     */
    public CatchStmt getCatchStmt() {
        return catchStmt;
    }
    
    /**
     * @return the finallyStmt
     */
    public Stmt getFinallyStmt() {
        return finallyStmt;
    }
}

