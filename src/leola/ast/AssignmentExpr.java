/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.ast;

import leola.frontend.tokens.Token;
import leola.vm.EvalException;

/**
 * Assignment statement
 *
 * @author Tony
 *
 */
public class AssignmentExpr extends Expr {

    /**
     * Var name
     */
    private VarExpr var;

    /**
     * Expr to assign
     */
    private Expr value;
    
    
    private Token operator;

    /**
     * @param var
     * @param value
     */
    public AssignmentExpr(VarExpr var, Expr value, Token operator) {
        this.var = var;
        this.value = value;
        this.operator = operator;
    }


    @Override
    public void visit(ASTNodeVisitor v) throws EvalException {
        v.visit(this);
    }
    
    public VarExpr getVar() {
        return var;
    }

    public Expr getValue() {
        return value;
    }
    
    public Token getOperator() {
        return operator;
    }
    
}

