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
	private OnStmt onStmt;
	private Stmt finallyStmt;

	/**
	 * @param stmt
	 * @param onStmt
	 * @param finallyStmt
	 */
	public TryStmt(Stmt stmt, OnStmt onStmt, Stmt finallyStmt) {
		this.stmt = becomeParentOf(stmt);
		this.onStmt = becomeParentOf(onStmt);
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
	 * @return the onStmt
	 */
	public OnStmt getOnStmt() {
		return onStmt;
	}
	
	/**
	 * @return the finallyStmt
	 */
	public Stmt getFinallyStmt() {
		return finallyStmt;
	}
}

