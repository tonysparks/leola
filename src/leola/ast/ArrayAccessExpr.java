/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.ast;

import leola.frontend.EvalException;

/**
 * Accounts for array access expressions:
 * 
 * <pre>
 *  array[10]
 * </pre>
 * 
 * @author Tony
 *
 */
public class ArrayAccessExpr extends OwnableExpr {

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
	public ArrayAccessExpr(String variableName, Expr elementIndex) {
		this(null, variableName, elementIndex);
	}

	/**
	 * @param elementIndex
	 */
	public ArrayAccessExpr(String owner, String variableName, Expr elementIndex) {
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
	 * @return the elementIndex
	 */
	public Expr getElementIndex() {
		return elementIndex;
	}
}

