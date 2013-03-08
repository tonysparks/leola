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
public class ChainedMemberAccessExpr extends OwnableExpr {

	/**
	 * Expression
	 */
	private Expr access;


	/**
	 * @param parent
	 * @param access
	 */
	public ChainedMemberAccessExpr(String parentName, Expr access) {
		super(parentName);
		this.access = becomeParentOf(access);
	}



	/* (non-Javadoc)
	 * @see leola.ast.ASTNode#visit(leola.ast.ASTNodeVisitor)
	 */
	@Override
	public void visit(ASTNodeVisitor v) throws EvalException {
		v.visit(this);
	}

	/**
	 * @return the access
	 */
	public Expr getAccess() {
		return access;
	}
}

