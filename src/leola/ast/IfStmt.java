/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.ast;

import leola.frontend.EvalException;

/**
 * If Statement
 *
 * @author Tony
 *
 */
public class IfStmt extends Stmt {

	private Expr condition;
	private Stmt stmt;
	private Stmt elseStmt;
	/**
	 * @param condition
	 * @param stmt
	 * @param elseStmt
	 */
	public IfStmt(Expr condition, Stmt stmt, Stmt elseStmt) {
		this.condition = becomeParentOf(condition);
		this.stmt = becomeParentOf(stmt);
		this.elseStmt = becomeParentOf(elseStmt);
	}

	/**
	 * @param condition
	 * @param stmt
	 */
	public IfStmt(Expr condition, Stmt stmt) {
		this(condition, stmt, null);
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
	 * @return the stmt
	 */
	public Stmt getStmt() {
		return stmt;
	}
	/**
	 * @return the elseStmt
	 */
	public Stmt getElseStmt() {
		return elseStmt;
	}

}

