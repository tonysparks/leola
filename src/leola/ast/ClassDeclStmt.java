/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.ast;

import java.util.List;

import leola.vm.EvalException;

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
    private ParameterList classParameters;

    /**
     * Parent Class name
     */
    private String parentClassName;

    /**
     * Parent classes params
     */
    private List<Expr> parentClassArguments;

    /**
     * Class body
     */
    private Stmt classBodyStmt;


    /**
     * @param className
     * @param classParams
     * @param classBodyStmt
     * @param parentClassName
     * @param interfaceNames
     */
    public ClassDeclStmt(String className
                      , ParameterList classParams
                      , Stmt classBodyStmt
                      , String parentClassName
                      , List<Expr> parentClassArguments) {
        this.className = className;
        this.classParameters = classParams;
        this.classBodyStmt = becomeParentOf(classBodyStmt);
        this.parentClassName = parentClassName;
        this.parentClassArguments = parentClassArguments;
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
     * @return the classBodyStmt
     */
    public Stmt getClassBodyStmt() {
        return classBodyStmt;
    }

    /**
     * @return the classParameters
     */
    public ParameterList getClassParameters() {
        return classParameters;
    }

    /**
     * @return the parentClassArguments
     */
    public List<Expr> getParentClassArguments() {
        return parentClassArguments;
    }

}

