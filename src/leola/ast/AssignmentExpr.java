/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.ast;

import leola.frontend.EvalException;

/**
 * Assignment statement
 *
 * @author Tony
 *
 */
public class AssignmentExpr extends OwnableExpr {

	/**
	 * Var name
	 */
	private String varName;

	/**
	 * Expr to assign
	 */
	private Expr expr;

	/**
	 * If this is a map or an array index assignment
	 */
	private Expr lhsExpr;

	/**
	 * @param varName
	 * @param lhsExpr
	 * @param expr
	 */
	public AssignmentExpr(String varName, Expr lhsExpr, Expr expr) {
		this(varName, varName, lhsExpr, expr);
	}


	/**
	 * @param owner
	 * @param varName
	 * @param lhsExpr
	 * @param expr
	 */
	public AssignmentExpr(String owner, String varName, Expr lhsExpr, Expr expr) {
		super(owner);
		this.varName = varName;
		this.lhsExpr = becomeParentOf(lhsExpr);
		this.expr = becomeParentOf(expr);
	}



	/* (non-Javadoc)
	 * @see leola.ast.ASTNode#visit(leola.ast.ASTNodeVisitor)
	 */
	@Override
	public void visit(ASTNodeVisitor v) throws EvalException {
		v.visit(this);
	}

	/**
	 * @return the varName
	 */
	public String getVarName() {
		return varName;
	}

	/**
	 * @return the lhsExpr
	 */
	public Expr getLhsExpr() {
		return lhsExpr;
	}

	/**
	 * @param lhsExpr the lhsExpr to set
	 */
	public void setLhsExpr(Expr lhsExpr) {
		this.lhsExpr = becomeParentOf(lhsExpr);		
	}

	/**
	 * @return the expr
	 */
	public Expr getExpr() {
		return expr;
	}
	
	/* (non-Javadoc)
	 * @see leola.ast.OwnableExpr#setOwner(java.lang.String)
	 */
	@Override
	public void setOwner(String owner) {	
		super.setOwner(owner);
		if ( this.lhsExpr != null ) {
			if ( this.lhsExpr instanceof OwnableExpr) {
				((OwnableExpr)this.lhsExpr).setOwner(owner);
			}
		}
	}
}

