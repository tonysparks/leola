/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.ast;

import leola.frontend.EvalException;

/**
 * Decorator expression
 *
 * @author Tony
 *
 */
public class DecoratorExpr extends Expr {

	private String decoratorName;
	private Expr[] parameters;
	private Expr followingExpr;

	
	
	/**
     * @param decoratorName
     * @param parameters
     * @param followingExpr
     */
    public DecoratorExpr(String decoratorName, Expr[] parameters, Expr followingExpr) {
        this.decoratorName = decoratorName;
        this.parameters = parameters;
        this.followingExpr = becomeParentOf(followingExpr);
    }



    /* (non-Javadoc)
	 * @see leola.ast.ASTNode#visit(leola.ast.ASTNodeVisitor)
	 */
	@Override
	public void visit(ASTNodeVisitor v) throws EvalException {
		v.visit(this);
	}

	/**
     * @return the decoratorName
     */
    public String getDecoratorName() {
        return decoratorName;
    }
    
    /**
     * @return the followingExpr
     */
    public Expr getFollowingExpr() {
        return followingExpr;
    }
    
    /**
     * @return the parameters
     */
    public Expr[] getParameters() {
        return parameters;
    }

}

