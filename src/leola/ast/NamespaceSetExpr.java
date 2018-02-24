/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.ast;

import leola.frontend.tokens.Token;
import leola.vm.EvalException;

/**
 * @author Tony
 *
 */
public class NamespaceSetExpr extends Expr {

    private VarExpr namespace;
    private String identifier;
    private Expr value;
    private Token operator;


    public NamespaceSetExpr(VarExpr namespace, String identifier, Expr value, Token operator) {
        this.namespace = namespace;
        this.identifier = identifier;
        this.value = value;
        this.operator = operator;
    }

    @Override
    public void visit(ASTNodeVisitor v) throws EvalException {
        v.visit(this);
    }

    public VarExpr getNamespace() {
        return namespace;
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

