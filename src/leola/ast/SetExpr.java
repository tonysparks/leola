/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.ast;

import leola.frontend.Token;
import leola.vm.EvalException;

/**
 * @author Tony
 *
 */
public class SetExpr extends Expr {

    private Expr object;
    private String identifier;
    private Expr value;
    private Token operator;


    public SetExpr(Expr object, String identifier, Expr value, Token operator) {
        this.object = object;
        this.identifier = identifier;
        this.value = value;
        this.operator = operator;
    }

    @Override
    public void visit(ASTNodeVisitor v) throws EvalException {
        v.visit(this);
    }

    public Expr getObject() {
        return object;
    }
    
    public String getIdentifier() {
        return identifier;
    }
    
    public Expr getValue() {
        return value;
    }
    
    public Token getOperator() {
        return operator;
    }

}

