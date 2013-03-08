/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.ast;

import leola.frontend.EvalException;

/**
 * Using a Namespace
 *
 * @author Tony
 *
 */
public class NamespaceAccessExpr extends MemberAccessExpr {

	/**
	 * @param name
	 */
	public NamespaceAccessExpr(String owner, String identifer, OwnableExpr access) {
		super(owner, identifer, access);
	}


	/* (non-Javadoc)
	 * @see leola.ast.ASTNode#visit(leola.ast.ASTNodeVisitor)
	 */
	@Override
	public void visit(ASTNodeVisitor v) throws EvalException {
		v.visit(this);
	}
}

