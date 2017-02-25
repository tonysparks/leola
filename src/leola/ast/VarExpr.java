/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.ast;

import leola.frontend.EvalException;

/**
 * @author Tony
 *
 */
public class VarExpr extends OwnableExpr {

    /**
     * The variable name
     */
    private String varName;


    /**
     * @param varName
     */
    public VarExpr(String varName) {
        this(varName, varName);
    }

    /**
     * @param owner
     * @param varName
     */
    public VarExpr(String owner, String varName) {
        super(owner);
        this.varName = varName;
    }



    /* (non-Javadoc)
     * @see leola.ast.ASTNode#visit(leola.ast.ASTNodeVisitor)
     */
    @Override
    public void visit(ASTNodeVisitor v) throws EvalException {
        v.visit(this);
    }

    /**
     * @return the varName
     */
    public String getVarName() {
        return varName;
    }

}

