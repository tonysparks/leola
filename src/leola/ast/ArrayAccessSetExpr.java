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
public class ArrayAccessSetExpr extends OwnableExpr {

	/**
	 * element index
	 */
	private Expr elementIndex;

	/**
	 * Variable name
	 */
	private String variableName;

	/**
	 * @param variableName
	 * @param elementIndex
	 */
	public ArrayAccessSetExpr(String variableName, Expr elementIndex) {
		this(null, variableName, elementIndex);
	}

	/**
	 * @param elementIndex
	 */
	public ArrayAccessSetExpr(String owner, String variableName, Expr elementIndex) {
		super(owner);
		this.variableName = variableName;
		this.elementIndex = becomeParentOf(elementIndex);
	}



	/* (non-Javadoc)
	 * @see leola.ast.ASTNode#visit(leola.ast.ASTNodeVisitor)
	 */
	@Override
	public void visit(ASTNodeVisitor v) throws EvalException {
		v.visit(this);
	}

	/**
	 * @return the variableName
	 */
	public String getVariableName() {
		return variableName;
	}

	/**
	 * @param variableName the variableName to set
	 */
	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}

	/**
	 * @return the elementIndex
	 */
	public Expr getElementIndex() {
		return elementIndex;
	}
}

