/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.ast;

import leola.ast.BinaryExpr.BinaryOp;
import leola.frontend.EvalException;

/**
 * Assignment statement
 *
 * @author Tony
 *
 */
public class ChainedBinaryAssignmentExpr extends ChainedAssignmentExpr {

	/**
	 * Binary op
	 */
	private BinaryOp binaryOp;

	/**
	 * @param lhsExpr
	 * @param expr
	 * @param binaryOp
	 */
	public ChainedBinaryAssignmentExpr(Expr lhsExpr, Expr expr, BinaryOp binaryOp) {
		super(lhsExpr, expr);

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
	 * @return the binaryOp
	 */
	public BinaryOp getBinaryOp() {
		return binaryOp;
	}
}

