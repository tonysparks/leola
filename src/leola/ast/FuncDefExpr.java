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
public class FuncDefExpr extends Expr {

	/**
	 * Body
	 */
	private Stmt body;

	/**
	 * Parameters
	 */
	private String[] parameters;



	/**
	 * @param body
	 * @param parameters
	 */
	public FuncDefExpr(Stmt body, String ... parameters) {
		this.body = becomeParentOf(body);
		this.parameters = parameters;		
	}



	/* (non-Javadoc)
	 * @see leola.ast.ASTNode#visit(leola.ast.ASTNodeVisitor)
	 */
	@Override
	public void visit(ASTNodeVisitor v) throws EvalException {
		v.visit(this);
	}

	/**
	 * @return the body
	 */
	public Stmt getBody() {
		return body;
	}

	/**
	 * @return the parameters
	 */
	public String[] getParameters() {
		return parameters;
	}

}

