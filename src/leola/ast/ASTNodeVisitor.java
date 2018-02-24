/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.ast;

import leola.vm.EvalException;


/**
 * Node Visitor
 *
 * @author Tony
 *
 */
public interface ASTNodeVisitor {

    void visit(SubscriptGetExpr s) throws EvalException;
    void visit(SubscriptSetExpr s) throws EvalException;
    void visit(ArrayDeclExpr s) throws EvalException;

    void visit(MapDeclExpr s) throws EvalException;

    void visit(AssignmentExpr s) throws EvalException;
    void visit(BinaryExpr s) throws EvalException;
    void visit(BooleanExpr s) throws EvalException;
    void visit(BreakStmt s) throws EvalException;
    void visit(CaseExpr s) throws EvalException;
    void visit(ClassDeclStmt s) throws EvalException;
    void visit(BlockStmt s) throws EvalException;
    void visit(ContinueStmt s) throws EvalException;
    
    void visit(DecoratorExpr s) throws EvalException;
    
    void visit(NamespaceStmt s) throws EvalException;
    
    void visit(CatchStmt s) throws EvalException;
    
    void visit(RealExpr s) throws EvalException;
    void visit(IntegerExpr s) throws EvalException;
    void visit(LongExpr s) throws EvalException;
    
    void visit(ProgramStmt s) throws EvalException;
    
    void visit(IsExpr s) throws EvalException;

    void visit(EmptyStmt s) throws EvalException;
    void visit(GenDefExpr s) throws EvalException;
    void visit(FuncDefExpr s) throws EvalException;
    void visit(FuncInvocationExpr s) throws EvalException;
    void visit(IfStmt s) throws EvalException;
    
    void visit(NamespaceGetExpr s) throws EvalException;
    void visit(NamespaceSetExpr s) throws EvalException;
    void visit(ElvisGetExpr s) throws EvalException;
    void visit(GetExpr s) throws EvalException;
    void visit(SetExpr s) throws EvalException;
    void visit(NamedParameterExpr s) throws EvalException;
    void visit(NewExpr s) throws EvalException;    
    void visit(NullExpr s) throws EvalException;
    void visit(ReturnStmt s) throws EvalException;
    void visit(YieldStmt s) throws EvalException;
    void visit(StringExpr s) throws EvalException;
    void visit(SwitchStmt s) throws EvalException;
    
    void visit(TryStmt s) throws EvalException;
    void visit(ThrowStmt s) throws EvalException;
    void visit(UnaryExpr s) throws EvalException;
    
    void visit(VarDeclStmt s) throws EvalException;
    void visit(VarExpr s) throws EvalException;
    void visit(WhileStmt s) throws EvalException;
}

