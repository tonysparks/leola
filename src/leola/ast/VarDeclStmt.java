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
public class VarDeclStmt extends Stmt {

	private String varName;
	private Expr value;



	/**
	 * @param varName
	 * @param value
	 */
	public VarDeclStmt(String varName, Expr value) {
		this.varName = varName;
		this.value = becomeParentOf(value);
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
	 * @return the value
	 */
	public Expr getValue() {
		return value;
	}

}

