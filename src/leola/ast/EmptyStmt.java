/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.ast;

import leola.vm.EvalException;

/**
 * Empty block
 *
 * @author Tony
 *
 */
public class EmptyStmt extends Stmt {

    /* (non-Javadoc)
     * @see leola.ast.ASTNode#visit(leola.ast.ASTNodeVisitor)
     */
    @Override
    public void visit(ASTNodeVisitor v) throws EvalException {
        v.visit(this);
    }

}

