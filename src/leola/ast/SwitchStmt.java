/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.ast;

import java.util.List;

import leola.frontend.EvalException;
import leola.vm.util.Pair;

/**
 * Switch Stmt
 *
 * @author Tony
 *
 */
public class SwitchStmt extends Stmt {

	private Expr condition;
	private List<Pair<Expr, Stmt>> whenStmts;
	private Stmt elseStmt;

	/**
     * @param condition
     * @param whenStmts
     */
    public SwitchStmt(Expr condition, List<Pair<Expr, Stmt>> whenStmts) {
        this(condition, whenStmts, null);
    }

	/**
     * @param condition
     * @param whenStmts
     * @param elseStmt
     */
    public SwitchStmt(Expr condition, List<Pair<Expr, Stmt>> whenStmts, Stmt elseStmt) {        
        this.condition = becomeParentOf(condition);
        this.whenStmts = whenStmts;
        this.elseStmt = becomeParentOf(elseStmt);
        
        for(Pair<Expr, Stmt> p : whenStmts) {
        	becomeParentOf(p.getFirst());
        	becomeParentOf(p.getSecond());
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
	 * @return the condition
	 */
	public Expr getCondition() {
		return condition;
	}

	/**
	 * @return the elseStmt
	 */
	public Stmt getElseStmt() {
		return elseStmt;
	}

	/**
	 * @return the whenStmts
	 */
	public List<Pair<Expr, Stmt>> getWhenStmts() {
		return whenStmts;
	}

}

