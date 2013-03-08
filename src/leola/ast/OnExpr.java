/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.ast;

import java.util.List;

import leola.frontend.EvalException;

/**
 * ON Expression
 *
 * @author Tony
 *
 */
public class OnExpr extends Expr {

	/**
	 * Clauses to the {@link OnExpr}
	 * 
	 * @author chq-tonys
	 *
	 */
	public static class OnClause {
		private String type;
		private String identifier;
		private Stmt stmt;
		/**
		 * @param type
		 * @param identifier
		 * @param stmt
		 */
		public OnClause(String type, String identifier, Stmt stmt) {
			super();
			this.type = type;
			this.identifier = identifier;
			this.stmt = stmt;
		}
		/**
		 * @return the type
		 */
		public String getType() {
			return type;
		}
		/**
		 * @return the identifier
		 */
		public String getIdentifier() {
			return identifier;
		}
		/**
		 * @return the stmt
		 */
		public Stmt getStmt() {
			return stmt;
		}
		
		
	}
	
	/**
	 * Expression
	 */
	private Expr expr;

	/**
	 * the on clauses
	 */
	private List<OnClause> onClauses;

	/**
	 * @param owner
	 * @param expr
	 * @param onClauses
	 */
	public OnExpr(Expr expr, List<OnClause> onClauses) {
		super();
		this.expr = becomeParentOf(expr);
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
	 * @return the expr
	 */
	public Expr getExpr() {
		return expr;
	}
	
	/**
	 * @return the onClauses
	 */
	public List<OnClause> getOnClauses() {
		return onClauses;
	}
}

