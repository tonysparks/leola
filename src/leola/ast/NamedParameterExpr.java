/*
 * (c)2014 Expeditors International of Washington, Inc.
 * Business confidential and proprietary.  This information may not be reproduced 
 * in any form without advance written consent of an authorized officer of the 
 * copyright holder.
 *
 */
package leola.ast;

import leola.frontend.EvalException;

/**
 * Named Parameter Expression
 * 
 * @author chq-tonys
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
