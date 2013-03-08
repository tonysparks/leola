/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.ast;

import leola.frontend.EvalException;

/**
 * Unary operator
 *
 * @author Tony
 *
 */
public class UnaryExpr extends Expr {

	private Expr expr;
	private UnaryOp op;

	public enum UnaryOp {
		  NOT
		, NEGATE
		, BIT_NOT
	}

	/**
	 * @param expr
	 */
	public UnaryExpr(Expr expr, UnaryOp op) {
		this.expr = becomeParentOf(expr);
		this.op = op;
	}

	/**
	 * @return the op
	 */
	public UnaryOp getOp() {
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

