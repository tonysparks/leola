/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.ast;

import java.util.List;

import leola.ast.OnExpr.OnClause;
import leola.frontend.EvalException;

/**
 * ON Statement:
 * 
 * <pre>
 * try 
 *   stmt
 * on type identifier
 *   stmt
 * </pre>
 *
 * @author Tony
 *
 */
public class OnStmt extends Expr {

	/**
	 * the on clauses
	 */
	private List<OnClause> onClauses;

	/**
	 * @param onClauses
	 */
	public OnStmt(List<OnClause> onClauses) {
		super();
		this.onClauses = onClauses;
	}



	/* (non-Javadoc)
	 * @see leola.ast.ASTNode#visit(leola.ast.ASTNodeVisitor)
	 */
	@Override
	public void visit(ASTNodeVisitor v) throws EvalException {
		v.visit(this);
	}

	/**
	 * @return the onClauses
	 */
	public List<OnClause> getOnClauses() {
		return onClauses;
	}
}

