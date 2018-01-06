/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.ast;

import leola.vm.EvalException;

/**
 * Named Parameter Expression
 * 
 * @author Tony
 *
 */
public class NamedParameterExpr extends Expr {

    private String parameterName;
    private Expr valueExpr;
    
    

    /**
     * @param parameterName
     * @param valueExpr
     */
    public NamedParameterExpr(String parameterName, Expr valueExpr) {
        this.parameterName = parameterName;
        this.valueExpr = valueExpr;
    }



    /* (non-Javadoc)
     * @see leola.ast.ASTNode#visit(leola.ast.ASTNodeVisitor)
     */
    @Override
    public void visit(ASTNodeVisitor v) throws EvalException {
        v.visit(this);
    }

    /**
     * @return the parameterName
     */
    public String getParameterName() {
        return parameterName;
    }
    
    
    /**
     * @return the valueExpr
     */
    public Expr getValueExpr() {
        return valueExpr;
    }
}
