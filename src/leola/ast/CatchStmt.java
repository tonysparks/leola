/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.ast;

import leola.vm.EvalException;

/**
 * ON Statement:
 * 
 * <pre>
 * try 
 *   stmt
 * catch identifier
 *   stmt
 * </pre>
 *
 * @author Tony
 *
 */
public class CatchStmt extends Stmt {
   
    private String identifier;

    private Stmt body;
    
    /**
     * @param identifier
     * @param body
     */
    public CatchStmt(String identifier, Stmt body) {
        super();
        this.identifier = identifier;
        this.body = body;
    }



    /* (non-Javadoc)
     * @see leola.ast.ASTNode#visit(leola.ast.ASTNodeVisitor)
     */
    @Override
    public void visit(ASTNodeVisitor v) throws EvalException {
        v.visit(this);
    }

    /**
     * @return the identifier
     */
    public String getIdentifier() {
        return identifier;
    }
    
    /**
     * @return the body
     */
    public Stmt getBody() {
        return body;
    }
}

