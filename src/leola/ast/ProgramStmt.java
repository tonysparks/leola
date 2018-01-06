/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.ast;

import java.util.List;

import leola.vm.EvalException;

/**
 * A program, the root node
 *
 * @author Tony
 *
 */
public class ProgramStmt extends BlockStmt {

    /**
     */
    public ProgramStmt(List<Stmt> statements) {
        super(statements);
    }

    /* (non-Javadoc)
     * @see leola.ast.ASTNode#visit(leola.ast.ASTNodeVisitor)
     */
    @Override
    public void visit(ASTNodeVisitor v) throws EvalException {
        v.visit(this);
    }

}

