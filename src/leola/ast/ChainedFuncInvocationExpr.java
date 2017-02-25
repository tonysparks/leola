/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.ast;

import leola.frontend.EvalException;

/**
 * A Function invocation that reads from the top of the stack.
 *
 * @author Tony
 *
 */
public class ChainedFuncInvocationExpr extends Expr {

    /**
     * Parameters
     */
    private Expr[] parameters;


    /**
     * @param parameters
     */
    public ChainedFuncInvocationExpr(Expr ... parameters) {
        this.parameters = parameters;
        if(this.parameters!=null) {
            for(int i =0; i < this.parameters.length;i++) {
                becomeParentOf(this.parameters[i]);
            }
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
     * @return the parameters
     */
    public Expr[] getParameters() {
        return parameters;
    }

}

