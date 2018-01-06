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
public class NamespaceGetExpr extends Expr {

    private VarExpr namespace;
    private String identifier;


    public NamespaceGetExpr(VarExpr namespace, String identifier) {
        this.namespace = namespace;
        this.identifier = identifier;
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

}

