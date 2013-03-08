/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.ast;

import leola.frontend.EvalException;

/**
 * IS Expression
 *
 * @author Tony
 *
 */
public class IsExpr extends OwnableExpr {

	/**
	 * Expression
	 */
	private Expr lhsExpr;

	private String className;

	/**
	 * @param value
	 */
	public IsExpr(String owner, Expr lhsExpr, String className) {
		super(owner);
		this.lhsExpr = becomeParentOf(lhsExpr);
		this.className = className;
	}



	/* (non-Javadoc)
	 * @see leola.ast.ASTNode#visit(leola.ast.ASTNodeVisitor)
	 */
	@Override
	public void visit(ASTNodeVisitor v) throws EvalException {
		v.visit(this);
	}

	/**
	 * @return the lhsExpr
	 */
	public Expr getLhsExpr() {
		return lhsExpr;
	}

	/**
	 * @return the className
	 */
	public String getClassName() {
		return className;
	}
}

