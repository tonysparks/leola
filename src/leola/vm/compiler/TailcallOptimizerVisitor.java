/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.vm.compiler;

import java.util.List;

import leola.ast.ASTNode;
import leola.ast.ASTNodeVisitorAdapter;
import leola.ast.BlockStmt;
import leola.ast.Expr;
import leola.ast.FuncInvocationExpr;
import leola.ast.IfStmt;
import leola.ast.ProgramStmt;
import leola.ast.ReturnStmt;
import leola.ast.Stmt;
import leola.ast.YieldStmt;
import leola.vm.EvalException;

/**
 * Scans a function definition to see if it can apply a tail-call optimization
 * 
 * @author Tony
 *
 */
public class TailcallOptimizerVisitor extends ASTNodeVisitorAdapter {

    private boolean isTerminal;
    private String functionName;
    private boolean isTailcall;
    private FuncInvocationExpr tailCallExpr;
    
    /**
     * @param functionName
     */
    public TailcallOptimizerVisitor(String functionName) {
        this.functionName = functionName;
        this.isTailcall = false;
        this.isTerminal = false;
    }

    /**
     * @return the isTailcall
     */
    public boolean isTailcall() {
        return isTailcall;
    }
    
    /**
     * @return the tailCallExpr
     */
    public FuncInvocationExpr getTailCallExpr() {
        return tailCallExpr;
    }
    
    private void findTerminal(List<Stmt> nodes) throws EvalException {
        int size = nodes.size();
        if ( size > 0 ) {
            this.isTerminal = true;
            ASTNode lastStatement = nodes.get(size-1);
            lastStatement.visit(this);
        }
    }
    
    @Override
    public void visit(ProgramStmt s) throws EvalException {
        findTerminal(s.getStatements());
    }
    
    /* (non-Javadoc)
     * @see leola.ast.ASTNodeVisitor#visit(leola.ast.BlockStmt)
     */
    @Override
    public void visit(BlockStmt s) throws EvalException {
        findTerminal(s.getStatements());
    }


    /* (non-Javadoc)
     * @see leola.ast.ASTNodeVisitor#visit(leola.ast.FuncInvocationExpr)
     */
    @Override
    public void visit(FuncInvocationExpr s) throws EvalException {
        
        /* if this is a terminal node, and
         * it is referencing itself, we can
         * do the tail-call optimization
         */
        if (this.isTerminal) {
            if ( this.functionName.equals(s.getFunctionName())) {
                this.isTailcall = true;
                this.tailCallExpr = s;
            }
        }
    }

    /* (non-Javadoc)
     * @see leola.ast.ASTNodeVisitor#visit(leola.ast.IfStmt)
     */
    @Override
    public void visit(IfStmt s) throws EvalException {
        s.getStmt().visit(this);
        Stmt elseStmt = s.getElseStmt();
        if ( elseStmt != null ) {
            elseStmt.visit(this);
        }
    }

    
    /* (non-Javadoc)
     * @see leola.ast.ASTNodeVisitor#visit(leola.ast.ReturnStmt)
     */
    @Override
    public void visit(ReturnStmt s) throws EvalException {
        this.isTerminal = true;
        Expr r = s.getExpr();
        if ( r != null ) {
            r.visit(this);
        }
    }
        
    /* (non-Javadoc)
     * @see leola.ast.ASTNodeVisitor#visit(leola.ast.ReturnStmt)
     */
    @Override
    public void visit(YieldStmt s) throws EvalException {
        this.isTerminal = true;
        Expr r = s.getExpr();
        if ( r != null ) {
            r.visit(this);
        }
    }
}

