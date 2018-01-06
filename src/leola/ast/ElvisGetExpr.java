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
public class ElvisGetExpr extends GetExpr {

    public ElvisGetExpr(Expr object, String identifier) {
        super(object, identifier);
    }

    @Override
    public void visit(ASTNodeVisitor v) throws EvalException {
        v.visit(this);
    }
}

