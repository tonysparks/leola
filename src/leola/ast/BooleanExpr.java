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
public class BooleanExpr extends Expr {

	private boolean value;



	/**
	 * @param value
	 */
	public BooleanExpr(boolean value) {
		this.value = value;
	}



	/* (non-Javadoc)
	 * @see leola.ast.ASTNode#visit(leola.ast.ASTNodeVisitor)
	 */
	@Override
	public void visit(ASTNodeVisitor v) throws EvalException {
		v.visit(this);
	}

	public boolean getValue() {
		return this.value;
	}
}

