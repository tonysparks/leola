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
public class GenDefExpr extends FuncDefExpr {

    /**
     * @param body
     * @param parameters
     */
    public GenDefExpr(Stmt body, ParameterList parameters) {
        super(body, parameters);    
    }

    @Override
    public void visit(ASTNodeVisitor v) throws EvalException {
        v.visit(this);
    }
}

