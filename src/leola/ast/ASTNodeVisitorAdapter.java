/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.ast;

import leola.vm.EvalException;

/**
 * Serves as an Adapter
 * 
 * @author Tony
 *
 */
public class ASTNodeVisitorAdapter implements ASTNodeVisitor {
    
    /* (non-Javadoc)
     * @see leola.ast.ASTNodeVisitor#visit(leola.ast.SubscriptGetExpr)
     */
    @Override
    public void visit(SubscriptGetExpr s) throws EvalException {}

    /* (non-Javadoc)
     * @see leola.ast.ASTNodeVisitor#visit(leola.ast.SubscriptSetExpr)
     */
    @Override
    public void visit(SubscriptSetExpr s) throws EvalException {}
    

    /* (non-Javadoc)
     * @see leola.ast.ASTNodeVisitor#visit(leola.ast.ArrayDeclExpr)
     */
    @Override
    public void visit(ArrayDeclExpr s) throws EvalException {}

    /* (non-Javadoc)
     * @see leola.ast.ASTNodeVisitor#visit(leola.ast.MapDeclExpr)
     */
    @Override
    public void visit(MapDeclExpr s) throws EvalException {}

    /* (non-Javadoc)
     * @see leola.ast.ASTNodeVisitor#visit(leola.ast.AssignmentExpr)
     */
    @Override
    public void visit(AssignmentExpr s) throws EvalException {}

    /* (non-Javadoc)
     * @see leola.ast.ASTNodeVisitor#visit(leola.ast.BinaryExpr)
     */
    @Override
    public void visit(BinaryExpr s) throws EvalException {}

    /* (non-Javadoc)
     * @see leola.ast.ASTNodeVisitor#visit(leola.ast.BooleanExpr)
     */
    @Override
    public void visit(BooleanExpr s) throws EvalException {}

    /* (non-Javadoc)
     * @see leola.ast.ASTNodeVisitor#visit(leola.ast.BreakStmt)
     */
    @Override
    public void visit(BreakStmt s) throws EvalException {}

    /* (non-Javadoc)
     * @see leola.ast.ASTNodeVisitor#visit(leola.ast.CaseExpr)
     */
    @Override
    public void visit(CaseExpr s) throws EvalException {}

    /* (non-Javadoc)
     * @see leola.ast.ASTNodeVisitor#visit(leola.ast.ClassDeclStmt)
     */
    @Override
    public void visit(ClassDeclStmt s) throws EvalException {}

    /* (non-Javadoc)
     * @see leola.ast.ASTNodeVisitor#visit(leola.ast.BlockStmt)
     */
    @Override
    public void visit(BlockStmt s) throws EvalException {}

    /* (non-Javadoc)
     * @see leola.ast.ASTNodeVisitor#visit(leola.ast.ContinueStmt)
     */
    @Override
    public void visit(ContinueStmt s) throws EvalException {}

    /* (non-Javadoc)
     * @see leola.ast.ASTNodeVisitor#visit(leola.ast.DecoratorExpr)
     */
    @Override
    public void visit(DecoratorExpr s) throws EvalException {}
    
    /* (non-Javadoc)
     * @see leola.ast.ASTNodeVisitor#visit(leola.ast.NamespaceStmt)
     */
    @Override
    public void visit(NamespaceStmt s) throws EvalException {}

    /* (non-Javadoc)
     * @see leola.ast.ASTNodeVisitor#visit(leola.ast.NumberExpr)
     */
    @Override
    public void visit(RealExpr s) throws EvalException {}
    
    /*
     * (non-Javadoc)
     * @see leola.ast.ASTNodeVisitor#visit(leola.ast.IntegerExpr)
     */
    @Override
    public void visit(IntegerExpr s) throws EvalException {}
    
    /* (non-Javadoc)
     * @see leola.ast.ASTNodeVisitor#visit(leola.ast.LongExpr)
     */
    @Override
    public void visit(LongExpr s) throws EvalException {}

    /* (non-Javadoc)
     * @see leola.ast.ASTNodeVisitor#visit(leola.ast.ProgramStmt)
     */
    @Override
    public void visit(ProgramStmt s) throws EvalException {}

    /* (non-Javadoc)
     * @see leola.ast.ASTNodeVisitor#visit(leola.ast.IsExpr)
     */
    @Override
    public void visit(IsExpr s) throws EvalException {}

    /* (non-Javadoc)
     * @see leola.ast.ASTNodeVisitor#visit(leola.ast.EmptyStmt)
     */
    @Override
    public void visit(EmptyStmt s) throws EvalException {}
    
    /* (non-Javadoc)
     * @see leola.ast.ASTNodeVisitor#visit(leola.ast.GenDefExpr)
     */
    @Override
    public void visit(GenDefExpr s) throws EvalException {}

    /* (non-Javadoc)
     * @see leola.ast.ASTNodeVisitor#visit(leola.ast.FuncDefExpr)
     */
    @Override
    public void visit(FuncDefExpr s) throws EvalException {}

    /* (non-Javadoc)
     * @see leola.ast.ASTNodeVisitor#visit(leola.ast.FuncInvocationExpr)
     */
    @Override
    public void visit(FuncInvocationExpr s) throws EvalException {}

    /* (non-Javadoc)
     * @see leola.ast.ASTNodeVisitor#visit(leola.ast.IfStmt)
     */
    @Override
    public void visit(IfStmt s) throws EvalException {}

    @Override
    public void visit(NamespaceGetExpr s) throws EvalException {}
    
    @Override
    public void visit(NamespaceSetExpr s) throws EvalException {}
    
    @Override
    public void visit(ElvisGetExpr s) throws EvalException {}
    
    /* (non-Javadoc)
     * @see leola.ast.ASTNodeVisitor#visit(leola.ast.GetExpr)
     */
    @Override
    public void visit(GetExpr s) throws EvalException {}
    
    @Override
    public void visit(SetExpr s) throws EvalException {}

    /* (non-Javadoc)
     * @see leola.ast.ASTNodeVisitor#visit(leola.ast.NewExpr)
     */
    @Override
    public void visit(NewExpr s) throws EvalException {}

    /* (non-Javadoc)
     * @see leola.ast.ASTNodeVisitor#visit(leola.ast.NullExpr)
     */
    @Override
    public void visit(NullExpr s) throws EvalException {}
        
    /* (non-Javadoc)
     * @see leola.ast.ASTNodeVisitor#visit(leola.ast.ReturnStmt)
     */
    @Override
    public void visit(ReturnStmt s) throws EvalException {}

    /* (non-Javadoc)
     * @see leola.ast.ASTNodeVisitor#visit(leola.ast.StringExpr)
     */
    @Override
    public void visit(StringExpr s) throws EvalException {}
    
    /* (non-Javadoc)
     * @see leola.ast.ASTNodeVisitor#visit(leola.ast.YieldStmt)
     */
    @Override
    public void visit(YieldStmt s) throws EvalException {}

    /* (non-Javadoc)
     * @see leola.ast.ASTNodeVisitor#visit(leola.ast.SwitchStmt)
     */
    @Override
    public void visit(SwitchStmt s) throws EvalException {}


    /* (non-Javadoc)
     * @see leola.ast.ASTNodeVisitor#visit(leola.ast.ThrowStmt)
     */
    @Override
    public void visit(ThrowStmt s) throws EvalException {}
    
    /* (non-Javadoc)
     * @see leola.ast.ASTNodeVisitor#visit(leola.ast.TryStmt)
     */
    @Override
    public void visit(TryStmt s) throws EvalException {}
    
    /* (non-Javadoc)
     * @see leola.ast.ASTNodeVisitor#visit(leola.ast.OnStmt)
     */
    @Override
    public void visit(CatchStmt s) throws EvalException {}

    /* (non-Javadoc)
     * @see leola.ast.ASTNodeVisitor#visit(leola.ast.UnaryExpr)
     */
    @Override
    public void visit(UnaryExpr s) throws EvalException {}

    
    /* (non-Javadoc)
     * @see leola.ast.ASTNodeVisitor#visit(leola.ast.VarDeclStmt)
     */
    @Override
    public void visit(VarDeclStmt s) throws EvalException {}

    /* (non-Javadoc)
     * @see leola.ast.ASTNodeVisitor#visit(leola.ast.VarExpr)
     */
    @Override
    public void visit(VarExpr s) throws EvalException {}

    /* (non-Javadoc)
     * @see leola.ast.ASTNodeVisitor#visit(leola.ast.WhileStmt)
     */
    @Override
    public void visit(WhileStmt s) throws EvalException {}

    /* (non-Javadoc)
     * @see leola.ast.ASTNodeVisitor#visit(leola.ast.NamedParameterStmt)
     */
    @Override
    public void visit(NamedParameterExpr s) throws EvalException {}
}

