/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.ast;

import java.util.List;

import leola.vm.EvalException;

/**
 * Instantiates a new Object
 *
 * @author Tony
 *
 */
public class NewExpr extends Expr {

    /**
     * Class name
     */
    private String className;

    /**
     * The parameters for the constructor
     */
    private List<Expr> arguments;



    /**
     * @param className
     * @param arguments
     */
    public NewExpr(String className, List<Expr> arguments) {
        this.className = className;
        this.arguments = arguments;        
    }

    @Override
    public void visit(ASTNodeVisitor v) throws EvalException {
        v.visit(this);
    }


    /**
     * @return the className
     */
    public String getClassName() {
        return className;
    }



    /**
     * @return the arguments
     */
    public List<Expr> getArguments() {
        return arguments;
    }

}

