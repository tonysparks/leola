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
public class ArrayDeclExpr extends Expr {

	/**
	 * Elements
	 */
	private Expr[] elements;



	/**
	 * @param elements
	 */
	public ArrayDeclExpr(Expr[] elements) {
		this.elements = elements;
		if(elements!=null) {
			for(int i = 0; i < this.elements.length; i++) {
				becomeParentOf( this.elements[i] );
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
	 * @return the elements
	 */
	public Expr[] getElements() {
		return elements;
	}
}

