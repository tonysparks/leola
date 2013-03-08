/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.ast;

import leola.frontend.EvalException;

/**
 * A statement composed of two statements
 *
 * @author Tony
 *
 */
public class CompoundStmt extends Stmt {

	/**
	 * @param firstStmt
	 * @param secondStmt
	 */
	public CompoundStmt() {
	}

	/* (non-Javadoc)
	 * @see leola.ast.ASTNode#visit(leola.ast.ASTNodeVisitor)
	 */
	@Override
	public void visit(ASTNodeVisitor v) throws EvalException {
		v.visit(this);
	}

}

