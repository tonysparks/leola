/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.ast;

import java.util.List;

import leola.vm.EvalException;

/**
 * A Function invocation
 *
 * @author Tony
 *
 */
public class FuncInvocationExpr extends Expr {

    private Expr callee;
    private List<Expr> arguments;


    

    /**
     * @param callee
     * @param arguments
     */
    public FuncInvocationExpr(Expr callee, List<Expr> arguments) {
        super();
        this.callee = callee;
        this.arguments = arguments;
    }


    @Override
    public void visit(ASTNodeVisitor v) throws EvalException {
        v.visit(this);
    }

    public Expr getCallee() {
        return callee;
    }
    
    public List<Expr> getArguments() {
        return arguments;
    }
    
    public String getFunctionName()  {
        if(this.callee instanceof VarExpr) {
            return ((VarExpr)this.callee).getVarName();
        }
        
        return "";
    }
    
}

