/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.ast;

import leola.frontend.EvalException;

/**
 * @author Tony
 *
 */
public class WhileStmt extends Stmt {

	private Expr condition;
	private Stmt stmt;



	/**
	 * @param condition
	 * @param stmt
	 */
	public WhileStmt(Expr condition, Stmt stmt) {
		this.condition = becomeParentOf(condition);
		this.stmt = becomeParentOf(stmt);
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
}

