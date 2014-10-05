/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.ast;

import leola.frontend.EvalException;


/**
 * Node Visitor
 *
 * @author Tony
 *
 */
public interface ASTNodeVisitor {

	void visit(ArrayAccessExpr s) throws EvalException;
	void visit(ArrayAccessSetExpr s) throws EvalException;
	void visit(ArrayDeclExpr s) throws EvalException;

	void visit(MapDeclExpr s) throws EvalException;

	void visit(AssignmentExpr s) throws EvalException;
	void visit(BinaryExpr s) throws EvalException;
	void visit(BinaryAssignmentExpr s) throws EvalException;
	void visit(BooleanExpr s) throws EvalException;
	void visit(BreakStmt s) throws EvalException;
	void visit(CaseExpr s) throws EvalException;
	void visit(ChainedArrayAccessExpr s) throws EvalException;
	void visit(ChainedArrayAccessSetExpr s) throws EvalException;
	void visit(ChainedAssignmentExpr s) throws EvalException;
	void visit(ChainedBinaryAssignmentExpr s) throws EvalException;
	void visit(ChainedFuncInvocationExpr s) throws EvalException;
	void visit(ChainedMemberAccessExpr s) throws EvalException;
	void visit(ClassDeclStmt s) throws EvalException;
	void visit(CompoundStmt s) throws EvalException;
	void visit(CompoundExpr s) throws EvalException;
	void visit(ContinueStmt s) throws EvalException;
	
	void visit(NamespaceStmt s) throws EvalException;
	void visit(NamespaceAccessExpr s) throws EvalException;
	
	void visit(OnExpr s) throws EvalException;
	void visit(OnStmt s) throws EvalException;
	
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
	
	void visit(MemberAccessExpr s) throws EvalException;
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

