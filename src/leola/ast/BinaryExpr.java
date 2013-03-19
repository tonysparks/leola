/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.ast;

import leola.frontend.EvalException;

/**
 * A Binary expression.
 *
 * @author Tony
 *
 */
public class BinaryExpr extends Expr {

	/**
	 * Binary operator
	 *
	 * @author Tony
	 *
	 */
	public enum BinaryOp {
		  ADD
		, SUB
		, MUL
		, DIV
		, MOD

		, BIT_SHIFT_LEFT
		, BIT_SHIFT_RIGHT

		, OR
		, AND
		, EQ
		, REQ
		, LT
		, LTE
		, GT
		, GTE
		, NEQ

		, BIT_AND
		, BIT_OR
		, BIT_XOR
		, BIT_NOT

	}

	/**
	 * Expressions
	 */
	private Expr left, right;

	private BinaryOp op;

	/**
	 * @param left
	 * @param right
	 */
	public BinaryExpr(Expr left, Expr right, BinaryOp op) {
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
	public BinaryOp getOp() {
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

