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
public class GetExpr extends Expr {

    private Expr object;
    private String identifier;


    public GetExpr(Expr object, String identifier) {
        this.object = object;
        this.identifier = identifier;
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

}

