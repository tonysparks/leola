/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.ast;

import leola.frontend.EvalException;

/**
 * Class Declaration
 *
 * @author Tony
 *
 */
public class ClassDeclStmt extends Stmt {

	/**
	 * Class name
	 */
	private String className;

	/**
	 * Class parameters
	 */
	private String[] classParameters;

	/**
	 * Parent Class name
	 */
	private String parentClassName;

	/**
	 * Parent classes params
	 */
	private Expr[] parentClassParams;

	/**
	 * Interfaces names
	 */
	private String[] interfaceNames;

	/**
	 * Class body
	 */
	private Stmt classBodyStmt;


	/**
	 * @param className
	 * @param variables
	 * @param parentClassName
	 * @param interfaceNames
	 * @param constructorExprs
	 */
	public ClassDeclStmt(String className
					  , String[] classParams
					  , Stmt classBodyStmt
					  , String parentClassName
					  , Expr[] parentClassParams
					  , String[] interfaceNames) {
		this.className = className;
		this.classParameters = classParams;
		this.classBodyStmt = becomeParentOf(classBodyStmt);
		this.parentClassName = parentClassName;
		this.parentClassParams = parentClassParams;
		this.interfaceNames = interfaceNames;
		
		if(this.parentClassParams!=null) {
			for(int i = 0; i < this.parentClassParams.length; i++) {
				becomeParentOf(this.parentClassParams[i]);
			}
		}
	}

	/* (non-Javadoc)
	 * @see leola.ast.ASTNode#visit(leola.ast.ASTNodeVisitor)
	 */
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
	 * @return the parentClassName
	 */
	public String getParentClassName() {
		return parentClassName;
	}

	/**
	 * @return the interfaceNames
	 */
	public String[] getInterfaceNames() {
		return interfaceNames;
	}

	/**
	 * @return the classBodyStmt
	 */
	public Stmt getClassBodyStmt() {
		return classBodyStmt;
	}

	/**
	 * @return the classParameters
	 */
	public String[] getClassParameters() {
		return classParameters;
	}

	/**
	 * @return the parentClassParams
	 */
	public Expr[] getParentClassParams() {
		return parentClassParams;
	}

}

