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
public class ChainedArrayAccessExpr extends Expr {

	/**
	 * element index
	 */
	private Expr elementIndex;


	/**
	 * @param elementIndex
	 */
	public ChainedArrayAccessExpr(Expr elementIndex) {
		this.elementIndex = becomeParentOf( elementIndex );
	}



	/* (non-Javadoc)
	 * @see leola.ast.ASTNode#visit(leola.ast.ASTNodeVisitor)
	 */
	@Override
	public void visit(ASTNodeVisitor v) throws EvalException {
		v.visit(this);
	}

	/**
	 * @return the elementIndex
	 */
	public Expr getElementIndex() {
		return elementIndex;
	}
}

