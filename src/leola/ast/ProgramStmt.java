/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.ast;

import leola.frontend.EvalException;

/**
 * A program, the root node
 *
 * @author Tony
 *
 */
public class ProgramStmt extends Stmt {

	/**
	 * @param firstStmt
	 * @param secondStmt
	 */
	public ProgramStmt() {
	}

	/* (non-Javadoc)
	 * @see leola.ast.ASTNode#visit(leola.ast.ASTNodeVisitor)
	 */
	@Override
	public void visit(ASTNodeVisitor v) throws EvalException {
		v.visit(this);
	}

}

