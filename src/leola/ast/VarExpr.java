/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.ast;

import leola.vm.EvalException;

/**
 * @author Tony
 *
 */
public class VarExpr extends Expr {

    /**
     * The variable name
     */
    private String varName;

    /**
     * @param owner
     * @param varName
     */
    public VarExpr(String varName) {
        this.varName = varName;
    }

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

