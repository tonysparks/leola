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
public class MemberAccessExpr extends OwnableExpr {

	private OwnableExpr access;
	private String identifier;

	/**
	 * @param owner
	 * @param identifer
	 * @param access
	 */
	public MemberAccessExpr(String owner, String identifer, OwnableExpr access) {
		super(owner);
		this.identifier = identifer;
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
	public OwnableExpr getAccess() {
		return access;
	}

	/**
	 * @return the identifier
	 */
	public String getIdentifier() {
		return identifier;
	}
}

