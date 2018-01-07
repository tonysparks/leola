/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.ast;

import leola.vm.EvalException;

/**
 * Namespace definition Statement
 *
 * @author Tony
 *
 */
public class NamespaceStmt extends Stmt {

    /**
     * The namespace
     */
    private Stmt stmt;

    /**
     * Name
     */
    private String name;

    /**
     * @param stmt
     * @param name
     */
    public NamespaceStmt(Stmt stmt, String name) {
        this.stmt = becomeParentOf(stmt);
        this.name = name;
    }


    /* (non-Javadoc)
     * @see leola.ast.ASTNode#visit(leola.ast.ASTNodeVisitor)
     */
    @Override
    public void visit(ASTNodeVisitor v) throws EvalException {
        v.visit(this);
    }
    /**
     * @return the stmt
     */
    public Stmt getStmt() {
        return stmt;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
}

