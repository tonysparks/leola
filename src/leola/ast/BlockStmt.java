/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.ast;

import java.util.List;

import leola.vm.EvalException;

/**
 * A statement composed of two statements
 *
 * @author Tony
 *
 */
public class BlockStmt extends Stmt {
    
    private List<Stmt> statements;
    
    /**
     */
    public BlockStmt(List<Stmt> statements) {
        this.statements = statements;
    }

    @Override
    public void visit(ASTNodeVisitor v) throws EvalException {
        v.visit(this);
    }

    
    public List<Stmt> getStatements() {
        return statements;
    }
}

