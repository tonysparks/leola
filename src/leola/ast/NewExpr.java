/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.ast;

import leola.frontend.EvalException;

/**
 * Instantiates a new Object
 *
 * @author Tony
 *
 */
public class NewExpr extends Expr {

    /**
     * Class name
     */
    private String className;

    /**
     * The parameters for the constructor
     */
    private Expr[] parameters;



    /**
     * @param className
     * @param parameters
     */
    public NewExpr(String className, Expr[] parameters) {
        this.className = className;
        this.parameters = parameters;
        if(this.parameters != null) {
            for(int i = 0; i < this.parameters.length; i++) {
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
     * @return the className
     */
    public String getClassName() {
        return className;
    }



    /**
     * @return the parameters
     */
    public Expr[] getParameters() {
        return parameters;
    }

}

