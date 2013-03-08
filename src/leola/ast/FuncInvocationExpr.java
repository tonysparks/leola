/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.ast;

import leola.frontend.EvalException;

/**
 * A Function invokation
 *
 * @author Tony
 *
 */
public class FuncInvocationExpr extends OwnableExpr {

	/**
	 * Function name
	 */
	private String functionName;

	/**
	 * Parameters
	 */
	private Expr[] parameters;


	/**
	 * @param functionName
	 * @param parameters
	 */
	public FuncInvocationExpr(String functionName, Expr ... parameters) {
		this(null, functionName, parameters);
	}

	/**
	 * @param owner
	 * @param functionName
	 * @param parameters
	 */
	public FuncInvocationExpr(String owner, String functionName, Expr ... parameters) {
		super(owner);
		this.functionName = functionName;
		this.parameters = parameters;
		if(this.parameters != null) {
			for(int i = 0; i < this.parameters.length; i++) {
				becomeParentOf(this.parameters[i]);
			}
		}
	}

	/* (non-Javadoc)
	 * @see leola.ast.ASTNode#visit(leola.ast.ASTNodeVisitor)
	 */
	@Override
	public void visit(ASTNodeVisitor v) throws EvalException {
		v.visit(this);
	}

	/**
	 * @return the functionName
	 */
	public String getFunctionName() {
		return functionName;
	}

	/**
	 * @return the parameters
	 */
	public Expr[] getParameters() {
		return parameters;
	}

}

