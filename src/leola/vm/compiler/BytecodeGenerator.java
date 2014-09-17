/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.vm.compiler;

import java.util.List;
import java.util.Stack;

import leola.ast.ASTAttributes;
import leola.ast.ASTNode;
import leola.ast.ASTNodeVisitor;
import leola.ast.ArrayAccessExpr;
import leola.ast.ArrayAccessSetExpr;
import leola.ast.ArrayDeclExpr;
import leola.ast.AssignmentExpr;
import leola.ast.BinaryAssignmentExpr;
import leola.ast.BinaryExpr;
import leola.ast.BinaryExpr.BinaryOp;
import leola.ast.BooleanExpr;
import leola.ast.BreakStmt;
import leola.ast.CaseExpr;
import leola.ast.ChainedArrayAccessExpr;
import leola.ast.ChainedArrayAccessSetExpr;
import leola.ast.ChainedAssignmentExpr;
import leola.ast.ChainedBinaryAssignmentExpr;
import leola.ast.ChainedFuncInvocationExpr;
import leola.ast.ChainedMemberAccessExpr;
import leola.ast.ClassDeclStmt;
import leola.ast.CompoundExpr;
import leola.ast.CompoundStmt;
import leola.ast.ContinueStmt;
import leola.ast.EmptyStmt;
import leola.ast.Expr;
import leola.ast.FuncDefExpr;
import leola.ast.FuncInvocationExpr;
import leola.ast.GenDefExpr;
import leola.ast.IfStmt;
import leola.ast.IntegerExpr;
import leola.ast.IsExpr;
import leola.ast.LongExpr;
import leola.ast.MapDeclExpr;
import leola.ast.MemberAccessExpr;
import leola.ast.NamespaceAccessExpr;
import leola.ast.NamespaceStmt;
import leola.ast.NewExpr;
import leola.ast.NullExpr;
import leola.ast.OnExpr;
import leola.ast.OnExpr.OnClause;
import leola.ast.OwnableExpr;
import leola.ast.ProgramStmt;
import leola.ast.RealExpr;
import leola.ast.ReturnStmt;
import leola.ast.Stmt;
import leola.ast.StringExpr;
import leola.ast.SwitchStmt;
import leola.ast.ThrowStmt;
import leola.ast.UnaryExpr;
import leola.ast.UnaryExpr.UnaryOp;
import leola.ast.VarDeclStmt;
import leola.ast.VarExpr;
import leola.ast.WhileStmt;
import leola.ast.YieldStmt;
import leola.frontend.EvalException;
import leola.frontend.parsers.ParameterList;
import leola.vm.Leola;
import leola.vm.asm.Asm;
import leola.vm.asm.Bytecode;
import leola.vm.asm.Constants;
import leola.vm.asm.Pair;
import leola.vm.asm.Scope.ScopeType;
import leola.vm.asm.Symbols;
import leola.vm.types.LeoString;

/**
 * Generates {@link Bytecode} based off of an Abstract Syntax Tree.
 * 
 * 
 * @author Tony
 *
 */
public class BytecodeGenerator implements ASTNodeVisitor {

	/**
	 * The assembler
	 */
	private Asm asm;
		
	private Stack<String> breakLabelStack;
	private Stack<String> continueLabelStack;
	private Stack<Tailcall> tailCallStack;
	
	class Tailcall {
		String name;		
		FuncInvocationExpr tailcallExpr;
		
		Tailcall(String name, FuncInvocationExpr tailcallExpr) { 
			this.name = name; 		 
			this.tailcallExpr = tailcallExpr;
		}
	}
	
	//private Leola runtime;
	
	public BytecodeGenerator(Leola runtime, Symbols symbols) {
		//this.runtime = runtime;
		
		this.asm = new Asm(symbols);
		this.asm.setDebug(runtime.getArgs().isDebugMode());
			
		this.breakLabelStack = new Stack<String>();
		this.continueLabelStack = new Stack<String>();
		this.tailCallStack = new Stack<BytecodeGenerator.Tailcall>();
	}
		
	/**
	 * @return the asm
	 */
	public Asm getAsm() {
		return asm;
	}

	/* (non-Javadoc)
	 * @see leola.ast.ASTNodeVisitor#visit(leola.ast.ArrayAccessExpr)
	 */
	@Override
	public void visit(ArrayAccessExpr s) throws EvalException {
		asm.line(s.getLineNumber());
		
		if(s.isMemberAccessChild()) {
			Expr indexExpr = s.getElementIndex();	
			
			String reference = s.getVariableName();		
			asm.storeAndloadconst(reference);
			asm.get();
						
			indexExpr.visit(this);
			asm.get();
			
		}
		else {					
			String reference = s.getVariableName();
			loadglobalmember(reference);
			// IF arrays fail, check SVN history...			
			Expr indexExpr = s.getElementIndex();
			indexExpr.visit(this);
			asm.get();
		}

	}

	/* (non-Javadoc)
	 * @see leola.ast.ASTNodeVisitor#visit(leola.ast.ArrayAccessSetExpr)
	 */
	@Override
	public void visit(ArrayAccessSetExpr s) throws EvalException {
		asm.line(s.getLineNumber());
		if(s.isMemberAccessChild()) {						
			String reference = s.getVariableName();
			asm.storeAndloadconst(reference);						
			asm.get();						
		}
		else {						
			String reference = s.getVariableName();
			loadglobalmember(reference);
			//loadmember(s, reference, true, true);						
		}
		
		Expr indexExpr = s.getElementIndex();
		indexExpr.visit(this);		
		asm.set();
	}

	/* (non-Javadoc)
	 * @see leola.ast.ASTNodeVisitor#visit(leola.ast.ArrayDeclExpr)
	 */
	@Override
	public void visit(ArrayDeclExpr s) throws EvalException {
		asm.line(s.getLineNumber());
		
		Expr exprs[] = s.getElements();
		for(Expr expr : exprs) {
			expr.visit(this);
		}
		
		asm.newarray(exprs.length);		
	}

	/* (non-Javadoc)
	 * @see leola.ast.ASTNodeVisitor#visit(leola.ast.MapDeclExpr)
	 */
	@Override
	public void visit(MapDeclExpr s) throws EvalException {
		asm.line(s.getLineNumber());
		
		List<Pair<Expr, Expr>> elements = s.getElements();
		for(int i = 0; i < elements.size(); i++) {
			Pair<Expr, Expr> element = elements.get(i);
			Expr key = element.getFirst();
			key.appendFlag(ASTAttributes.IS_PROPERTY);  /* Let VarExpr be converted to strings */
			key.visit(this);
			
			Expr value = element.getSecond();
			value.visit(this);
		}		
		
		int numElements = elements.size();
		asm.newmap(numElements);		
	}

	/* (non-Javadoc)
	 * @see leola.ast.ASTNodeVisitor#visit(leola.ast.AssignmentExpr)
	 */
	@Override
	public void visit(AssignmentExpr s) throws EvalException {
		asm.line(s.getLineNumber());
		
		Expr e = s.getExpr();
		e.visit(this);
		
		if(s.isMemberAccessChild()) {
			asm.mov();
			
			Expr lhs = s.getLhsExpr();
			if ( lhs != null ) {																
				lhs.setMemberAccess(true);
				lhs.appendFlag(s.getFlags()); 
				lhs.visit(this);												
				
			}
			else {				
				asm.storeAndloadconst(s.getVarName());								
				asm.set();
			}			
		}
		else {						
			Expr l = s.getLhsExpr();
			if ( l != null ) {
				l.visit(this);
			}
			else {
				asm.dup(); /* account for Expr OPPOP */
				
				String ref = s.getVarName();
				asm.store(ref);								
			}
			
		}
		
//		asm.dup(); /* account for Expr OPPOP */
	}

	
	/**
	 * Visits a Binary Expression
	 * 
	 * @param op
	 * @throws EvalException
	 */
	private void visitBinaryExpression(BinaryOp op) throws EvalException {
		switch(op) {
			case ADD: {
				asm.add();
				break;
			}
			case SUB: {
				asm.sub();
				break;
			}
			case MUL: {
				asm.mul();
				break;
			}
			case DIV: {
				asm.div();
				break;
			}
			case MOD: {
				asm.mod();
				break;
			}
						
			// comparisons
			
			case AND: {
				asm.and();
				break;
			}
			case OR: {
				asm.or();
				break;
			}
			case REQ: {
				asm.req();
				break;
			}
			case EQ: {
				asm.eq();
				break;
			}
			case NEQ: {
				asm.neq();
				break;
			}
			case GT: {
				asm.gt();
				break;
			}
			case GTE: {
				asm.gte();
				break;
			}
			case LT: {
				asm.lt();
				break;
			}
			case LTE: {
				asm.lte();
				break;
			}
			
			// bit ops
			
			case BIT_AND: {
				asm.land();
				break;
				
			}
			case BIT_OR: {
				asm.lor();
				break;
			}
			case BIT_SHIFT_LEFT: {
				asm.bsl();
				break;
			}
			case BIT_SHIFT_RIGHT: {
				asm.bsr();
				break;
			}
			case BIT_XOR: {
				asm.xor();
				break;
			}
			default: {
				throw new EvalException("Unknown BinaryOperator: " + op);
			}
		}
		
	}
	
	/* (non-Javadoc)
	 * @see leola.ast.ASTNodeVisitor#visit(leola.ast.BinaryExpr)
	 */
	@Override
	public void visit(BinaryExpr s) throws EvalException {
		asm.line(s.getLineNumber());
		BinaryOp op = s.getOp();
		switch(op) {
			case AND: {
				s.getLeft().visit(this);
				String escape = asm.ifeq();
				
				s.getRight().visit(this);
				String endif = asm.jmp();
				asm.label(escape);
				asm.loadfalse();
				asm.label(endif);
				break;
			}
			case OR: {
				s.getLeft().visit(this);
				String secondConditional = asm.ifeq();
				String skip = asm.jmp();
				
				asm.label(secondConditional);
				s.getRight().visit(this);							
				String end = asm.jmp();
				
				asm.label(skip);
				asm.loadtrue();
				asm.label(end);
				
				break;
			}
			default: {
				s.getLeft().visit(this);
				s.getRight().visit(this);
				
				
				visitBinaryExpression(op);		
			}
		}						
	}

	/* (non-Javadoc)
	 * @see leola.ast.ASTNodeVisitor#visit(leola.ast.BinaryAssignmentExpr)
	 */
	@Override
	public void visit(BinaryAssignmentExpr s) throws EvalException {	
		asm.line(s.getLineNumber());
		
		if(s.isMemberAccessChild()) {
			/* left hand side == access to an object */
			Expr lhs = s.getLhsExpr();
			if ( lhs != null ) {		
				lhs.setMemberAccess(true);
				if ( lhs instanceof ArrayAccessSetExpr) {
					asm.dup();
					
					ArrayAccessSetExpr setExpr = (ArrayAccessSetExpr)lhs;
					
					String reference = setExpr.getVariableName();
					asm.storeAndloadconst(reference);
					asm.get();
					
					Expr indexExpr = setExpr.getElementIndex();
					indexExpr.visit(this);
					
					asm.get();
				}
								
				Expr expr = s.getExpr();
				expr.visit(this);
		
				BinaryOp op = s.getBinaryOp();
				visitBinaryExpression(op);
				
				asm.mov();
				lhs.visit(this);		
		
			}
			else {			

				asm.dup();
				
				String ref = s.getVarName();
				asm.storeAndloadconst(ref);
				asm.get();
								
				Expr e = s.getExpr();
				e.visit(this);
				
				visitBinaryExpression(s.getBinaryOp());
				asm.mov();
												
				asm.storeAndloadconst(ref);												
				asm.set();
								
				//asm.dup(); /* account for OPPOP */
			}	
		}
		else {						
			/* left hand side == access to an object */
			Expr lhs = s.getLhsExpr();
			if ( lhs != null ) {		
				if ( lhs instanceof ArrayAccessSetExpr) {
					ArrayAccessSetExpr setExpr = (ArrayAccessSetExpr)lhs;
					
					String reference = setExpr.getVariableName();
					loadglobalmember(reference);
										
					Expr indexExpr = setExpr.getElementIndex();
					indexExpr.visit(this);
					
					asm.get();
				}
											
				Expr expr = s.getExpr();
				expr.visit(this);
				
				BinaryOp op = s.getBinaryOp();
				visitBinaryExpression(op);
				
				lhs.visit(this);		
					
				
			}
			else {			 
				String ref = s.getVarName();				
				loadglobalmember(ref);
				
				Expr expr = s.getExpr();
				expr.visit(this);
				
				visitBinaryExpression(s.getBinaryOp());
				
				asm.dup(); /* account for OPPOP */
				asm.store(ref);												
			}
		}
	}

	/* (non-Javadoc)
	 * @see leola.ast.ASTNodeVisitor#visit(leola.ast.BooleanExpr)
	 */
	@Override
	public void visit(BooleanExpr s) throws EvalException {
		asm.line(s.getLineNumber());
		
		if ( s.getValue() ) {
			asm.loadtrue();
		}
		else {
			asm.loadfalse();
		}
	}

	/* (non-Javadoc)
	 * @see leola.ast.ASTNodeVisitor#visit(leola.ast.BreakStmt)
	 */
	@Override
	public void visit(BreakStmt s) throws EvalException {
		asm.line(s.getLineNumber());
		
		if ( ! this.breakLabelStack.isEmpty() ) {
			String label = this.breakLabelStack.peek();
			asm.brk(label);
		}
	}



	/* (non-Javadoc)
	 * @see leola.ast.ASTNodeVisitor#visit(leola.ast.ChainedArrayAccessExpr)
	 */
	@Override
	public void visit(ChainedArrayAccessExpr s) throws EvalException {
		asm.line(s.getLineNumber());
		
		s.getElementIndex().visit(this);
		asm.get();
	}

	/* (non-Javadoc)
	 * @see leola.ast.ASTNodeVisitor#visit(leola.ast.ChainedArrayAccessSetExpr)
	 */
	@Override
	public void visit(ChainedArrayAccessSetExpr s) throws EvalException {
		asm.line(s.getLineNumber());
		
		s.getElementIndex().visit(this);
		asm.set();
	}

	/* (non-Javadoc)
	 * @see leola.ast.ASTNodeVisitor#visit(leola.ast.ChainedAssignmentExpr)
	 */
	@Override
	public void visit(ChainedAssignmentExpr s) throws EvalException {
		asm.line(s.getLineNumber());
		
		Expr valueExpr = s.getExpr();
		valueExpr.visit(this);
		
		Expr lhs = s.getLhsExpr();
		if ( lhs != null ) {
			asm.mov();
			lhs.visit(this);
		}		
	}

	/* (non-Javadoc)
	 * @see leola.ast.ASTNodeVisitor#visit(leola.ast.ChainedBinaryAssignmentExpr)
	 */
	@Override
	public void visit(ChainedBinaryAssignmentExpr s) throws EvalException {
		asm.line(s.getLineNumber());
		
		asm.dup();
		
		Expr valueExpr = s.getExpr();
		valueExpr.visit(this);
		
		
		Expr lhs = s.getLhsExpr();
		if ( lhs != null ) {
			// TODO
			if ( lhs instanceof ChainedArrayAccessSetExpr) {
				asm.mov();
				
				ChainedArrayAccessSetExpr e = (ChainedArrayAccessSetExpr)lhs;
				e.getElementIndex().visit(this);				
				asm.dup();
				asm.shift(4);
				asm.get();
				
				visitBinaryExpression(s.getBinaryOp());
				asm.shift(3);					
				asm.set();
			}
		}
		else {
		
			visitBinaryExpression(s.getBinaryOp());
		}
	}

	/* (non-Javadoc)
	 * @see leola.ast.ASTNodeVisitor#visit(leola.ast.ChainedFuncInvocationExpr)
	 */
	@Override
	public void visit(ChainedFuncInvocationExpr s) throws EvalException {
		asm.line(s.getLineNumber());
		
		int nargs = 0;
		Expr[] params = s.getParameters();
		if ( params != null ) {
			for(Expr param : params) {
				param.visit(this);
			}
			
			nargs = params.length;
		}
		
		asm.movn(nargs);
		asm.invoke(nargs);
	}

	/* (non-Javadoc)
	 * @see leola.ast.ASTNodeVisitor#visit(leola.ast.ChainedMemberAccessExpr)
	 */
	@Override
	public void visit(ChainedMemberAccessExpr s) throws EvalException {
		asm.line(s.getLineNumber());
		
		Expr expr = s.getAccess();
		expr.appendFlag(ASTAttributes.IS_PROPERTY);

		// account for x[0].y.C
		if(expr instanceof MemberAccessExpr) {
			loadmember(s, s.getOwner(), true, true);
			asm.get();
		}
		
		expr.visit(this);		
	}

	/* (non-Javadoc)
	 * @see leola.ast.ASTNodeVisitor#visit(leola.ast.ClassDeclStmt)
	 */
	@Override
	public void visit(ClassDeclStmt s) throws EvalException {
		asm.line(s.getLineNumber());
		
		String className = s.getClassName();
		
				
		// this class name
		asm.storeAndloadconst(className);
		
		
		// parent class name
		String parentClassNAme = s.getParentClassName();
		if ( parentClassNAme != null ) {			
			asm.storeAndloadconst(parentClassNAme);
		}
		else {
			asm.loadnull();
		}
		
		// interfaces
		String[] interfaces = s.getInterfaceNames();
		if ( interfaces != null ) {
			for(String i : interfaces) {				
				asm.storeAndloadconst(i);
			}
		}
		
		// TODO - Determine how this will take advantage of varargs
		ParameterList params = s.getClassParameters();
		if ( params != null ) {
			for(String ref:params.getParameters()) {				
				asm.storeAndloadconst(ref);
			}
		}							
		asm.storeAndloadconst(params!=null?params.size():0);
		
		/*
		 * NOTE: this places Constants in the parameters and
		 * if there is a reference it will put the 
		 * reference name as a string so it can later look the
		 * reference up via the passed constructor value
		 * 
		 * @see ClassDefinitions
		 */
		Expr[] superParams = s.getParentClassParams();		
		if( superParams != null) {
			for(Expr e: superParams) {
				
				/* may pass values from sibling class to parent class */
				if(e instanceof VarExpr) {
                    e.appendFlag(ASTAttributes.IS_PROPERTY);
				}
				
				e.visit(this);
			}
		}
		asm.storeAndloadconst(superParams!=null?superParams.length:0);
		
		asm.storeAndloadconst(asm.getBytecodeIndex());		
		asm.classdef(interfaces!=null?interfaces.length:0);
		{			
			Stmt body = s.getClassBodyStmt();
			body.visit(this);
		}
		asm.end();				
		
		
	}

	/* (non-Javadoc)
	 * @see leola.ast.ASTNodeVisitor#visit(leola.ast.CompoundStmt)
	 */
	@Override
	public void visit(CompoundStmt s) throws EvalException {
		asm.line(s.getLineNumber());
			
		/* if the parent has already created a new scope, we don't
		 * want to force a new lexical scope
		 */
		ASTNode parent = s.getParentNode();
		boolean newLexicalScope =  ((parent instanceof ClassDeclStmt) ||
									(parent instanceof NamespaceStmt) ||
									(parent instanceof FuncDefExpr) );
									
		if(!newLexicalScope) {
			asm.markLexicalScope();
		}
		
		List<ASTNode> nodes = s.getChildren();
		int numNodes = nodes.size();
		for(int i = 0; i < numNodes; i++) {
			ASTNode n = nodes.get(i);
			n.visit(this);			
			
			if(n instanceof Expr) {
				asm.oppop();				
			}					
		}
		
		if(!newLexicalScope) {
			asm.unmarkLexicalScope();
		}
	}

	/* (non-Javadoc)
	 * @see leola.ast.ASTNodeVisitor#visit(leola.ast.CompoundExpr)
	 */
	@Override
	public void visit(CompoundExpr s) throws EvalException {	
		asm.line(s.getLineNumber());
		
		for(ASTNode n : s.getChildren()) {
			n.visit(this);
		}
	}

	/* (non-Javadoc)
	 * @see leola.ast.ASTNodeVisitor#visit(leola.ast.ContinueStmt)
	 */
	@Override
	public void visit(ContinueStmt s) throws EvalException {
		asm.line(s.getLineNumber());
		
		String label = this.continueLabelStack.peek();
		asm.cont(label);
	}

	
	/* (non-Javadoc)
	 * @see leola.ast.ASTNodeVisitor#visit(leola.ast.NamespaceStmt)
	 */
	@Override
	public void visit(NamespaceStmt s) throws EvalException {
		asm.line(s.getLineNumber());
				
		String name = s.getName();		
		asm.storeAndloadconst(name);
		
		asm.newnamespace();
		{			
			s.getStmt().visit(this);
		}
		asm.end();
		int index = asm.addLocal(name);
		asm.storelocal(index);						
	}

	/* (non-Javadoc)
	 * @see leola.ast.ASTNodeVisitor#visit(leola.ast.NamespaceAccessExpr)
	 */
	@Override
	public void visit(NamespaceAccessExpr s) throws EvalException {
		s.appendFlag(ASTAttributes.NAMESPACE_PROPERTY);
		
		visit( (MemberAccessExpr) s);						
	}

	/* (non-Javadoc)
	 * @see leola.ast.ASTNodeVisitor#visit(leola.ast.NumberExpr)
	 */
	@Override
	public void visit(RealExpr s) throws EvalException {
		asm.line(s.getLineNumber());

		asm.storeAndloadconst(s.getValue());
	}
	
	@Override
	public void visit(IntegerExpr s) throws EvalException {
		asm.line(s.getLineNumber());

		asm.storeAndloadconst(s.getValue());
	}

	/* (non-Javadoc)
	 * @see leola.ast.ASTNodeVisitor#visit(leola.ast.LongExpr)
	 */
	@Override
	public void visit(LongExpr s) throws EvalException {
		asm.line(s.getLineNumber());

		asm.storeAndloadconst(s.getValue());		
	}
	
	/* (non-Javadoc)
	 * @see leola.ast.ASTNodeVisitor#visit(leola.ast.ProgramStmt)
	 */
	@Override
	public void visit(ProgramStmt s) throws EvalException {					
		asm.start(ScopeType.GLOBAL_SCOPE);
		{
			for(ASTNode n : s.getChildren()) {			
				n.visit(this);		
			}
		}
		asm.end();
	}

	/* (non-Javadoc)
	 * @see leola.ast.ASTNodeVisitor#visit(leola.ast.IsExpr)
	 */
	@Override
	public void visit(IsExpr s) throws EvalException {	
		asm.line(s.getLineNumber());
		if (s.isMemberAccessChild()){
			Expr lhs = s.getLhsExpr();
			lhs.setMemberAccess(true);		
			lhs.visit(this);
			
			String type = s.getClassName();			
			asm.storeAndloadconst(type);
			
			asm.mov();
		}
		else {

			String type = s.getClassName();			
			asm.storeAndloadconst(type);
			
			Expr lhs = s.getLhsExpr();		
			lhs.visit(this);
			
		}
		asm.isa();
	}

	/* (non-Javadoc)
	 * @see leola.ast.ASTNodeVisitor#visit(leola.ast.EmptyStmt)
	 */
	@Override
	public void visit(EmptyStmt s) throws EvalException {
		asm.line(s.getLineNumber());
	}

	/* (non-Javadoc)
	 * @see leola.ast.ASTNodeVisitor#visit(leola.ast.GenDefExpr)
	 */
	@Override
	public void visit(GenDefExpr s) throws EvalException {
		asm.line(s.getLineNumber());
		
		ParameterList parameters = s.getParameters();
		asm.gen(parameters.size(), parameters.isVarargs());
		{
			asm.line(s.getLineNumber());
						
			for(String name : parameters.getParameters()) {
				asm.addLocal(name);
			}
			
			s.getBody().visit(this);
		} 
		asm.end();		
	}
	
	/* (non-Javadoc)
	 * @see leola.ast.ASTNodeVisitor#visit(leola.ast.FuncDefExpr)
	 */
	@Override
	public void visit(FuncDefExpr s) throws EvalException {
		asm.line(s.getLineNumber());
		
		ParameterList parameters = s.getParameters();
		asm.def(parameters.size(), parameters.isVarargs());
		{
			asm.line(s.getLineNumber());
						
			for(String name : parameters.getParameters()) {
				asm.addLocal(name);
			}
			
			s.getBody().visit(this);
		} 
		asm.end(); 		
	}
	
	/* (non-Javadoc)
	 * @see leola.ast.ASTNodeVisitor#visit(leola.ast.FuncInvocationExpr)
	 */
	@Override
	public void visit(FuncInvocationExpr s) throws EvalException {		
		asm.line(s.getLineNumber());
		
		int nargs = 0;
		Expr[] params = s.getParameters();
		if ( params != null ) {
			for(Expr param : params) {
				param.visit(this);
			}
			
			nargs = params.length;
		}
				
		if ( s.isMemberAccessChild() ) {
			/* Member access */
			
			asm.movn(nargs);
			
			String functionName = s.getFunctionName();
			asm.storeAndloadconst(functionName);			
			asm.get(); 
		}
		else {
			/* global/free function */
			
			String functionName = s.getFunctionName();
			loadglobalmember(functionName);
		}
			
		boolean isTailcall = false;
		if ( ! this.tailCallStack.isEmpty() ) {
			Tailcall tc = this.tailCallStack.peek();
			if ( tc.tailcallExpr == s) {
				asm.tailcall(nargs);
				isTailcall = true;
			}
		}
		
		if ( !isTailcall ) {
			asm.invoke(nargs);
		}				
	}
	
	/* (non-Javadoc)
	 * @see leola.ast.ASTNodeVisitor#visit(leola.ast.IfStmt)
	 */
	@Override
	public void visit(IfStmt s) throws EvalException {
		asm.line(s.getLineNumber());
		
		Expr cond = s.getCondition();
		cond.visit(this);
		
		String elseLabel = asm.ifeq();
		Stmt stmt = s.getStmt();
		stmt.visit(this);
		String endif = asm.jmp();
		
		asm.label(elseLabel);
		Stmt elseStmt = s.getElseStmt();
		if ( elseStmt != null ) {
			elseStmt.visit(this);			
		}
		asm.label(endif);
	}

	/* (non-Javadoc)
	 * @see leola.ast.ASTNodeVisitor#visit(leola.ast.MemberAccessExpr)
	 */
	@Override
	public void visit(MemberAccessExpr s) throws EvalException {
		asm.line(s.getLineNumber());
				
		OwnableExpr expr = s.getAccess();
		expr.appendFlag(ASTAttributes.IS_PROPERTY);
		
		if (s.isParent()) {
			String owner = s.getOwner();
			
			/* request for a namespace only */
			if(s.hasFlag(ASTAttributes.NAMESPACE_PROPERTY)) {
				asm.getnamespace(owner);
			}
			else {
				loadglobalmember(owner);
			}
		}
				
		String identifier = s.getIdentifier();
		if ( identifier != null && expr instanceof MemberAccessExpr) {
			asm.storeAndloadconst(identifier);
			asm.get();
		}
			
		expr.visit(this);							
	}

	/* (non-Javadoc)
	 * @see leola.ast.ASTNodeVisitor#visit(leola.ast.NewExpr)
	 */
	@Override
	public void visit(NewExpr s) throws EvalException {
		asm.line(s.getLineNumber());
			
		Expr[] exprs = s.getParameters();
		for(Expr expr : s.getParameters()) {
			expr.visit(this);
		}
		
		String className = s.getClassName();		
		asm.storeAndloadconst(className);
		
		asm.newobj(exprs!=null?exprs.length:0);
	}

	/* (non-Javadoc)
	 * @see leola.ast.ASTNodeVisitor#visit(leola.ast.NullExpr)
	 */
	@Override
	public void visit(NullExpr s) throws EvalException {
		asm.line(s.getLineNumber());
		
		asm.loadnull();
	}
	
	/* (non-Javadoc)
	 * @see leola.ast.ASTNodeVisitor#visit(leola.ast.ReturnStmt)
	 */
	@Override
	public void visit(YieldStmt s) throws EvalException {
		asm.line(s.getLineNumber());
		
		Expr expr = s.getExpr();
		if ( expr != null ) {		
			expr.visit(this);
		}
		else {
			asm.loadnull();
		}
		
		asm.yield();
	}

	/* (non-Javadoc)
	 * @see leola.ast.ASTNodeVisitor#visit(leola.ast.ReturnStmt)
	 */
	@Override
	public void visit(ReturnStmt s) throws EvalException {
		asm.line(s.getLineNumber());
		
		Expr expr = s.getExpr();
		if ( expr != null ) {		
			expr.visit(this);
		}
		else {
			asm.loadnull();
		}
		
		asm.ret();
	}

	/* (non-Javadoc)
	 * @see leola.ast.ASTNodeVisitor#visit(leola.ast.StringExpr)
	 */
	@Override
	public void visit(StringExpr s) throws EvalException {		
		asm.line(s.getLineNumber());
		
		asm.storeAndloadconst(s.getValue());				
	}

	/* (non-Javadoc)
	 * @see leola.ast.ASTNodeVisitor#visit(leola.ast.SwitchStmt)
	 */
	@Override
	public void visit(SwitchStmt s) throws EvalException {
		asm.line(s.getLineNumber());
		
		Expr condExpr = s.getCondition();
		condExpr.visit(this);
		
		String endCase = asm.nextLabelName();
		String nextWhenLabel = null;
						
		List<Pair<Expr, Stmt>> whenStmts = s.getWhenStmts();
		boolean isSingle = whenStmts.size() < 2;
		for(Pair<Expr, Stmt> whenExpr : whenStmts) {
			if ( !isSingle ) {
				asm.dup();
			}
			
			Expr cond = whenExpr.getFirst();			
			cond.visit(this);
			asm.eq();
			
			nextWhenLabel = asm.ifeq();						
			Stmt stmt = whenExpr.getSecond();
			if(!isSingle) {
				asm.pop();
			}
			
			stmt.visit(this);
			if( stmt instanceof Expr) {
				asm.oppop(); /* remove any pushed items */
			}
			
			asm.jmp(endCase);		
			asm.label(nextWhenLabel);
		}
		
		if(!isSingle) {
			asm.pop();
		}
		
		Stmt elseStmt = s.getElseStmt();
		if ( elseStmt != null ) {
			elseStmt.visit(this);
			if( elseStmt instanceof Expr) {
				asm.oppop(); /* remove any pushed items */
			}
		}
				
		asm.label(endCase);				
	}

	/* (non-Javadoc)
	 * @see leola.ast.ASTNodeVisitor#visit(leola.ast.CaseExpr)
	 */
	@Override
	public void visit(CaseExpr s) throws EvalException {	
		asm.line(s.getLineNumber());
		
		Expr condExpr = s.getCondition();
		condExpr.visit(this);
		
		String endCase = asm.nextLabelName();
		String nextWhenLabel = null;
						
		List<Pair<Expr, Expr>> whenExprs = s.getWhenExprs();
		boolean isSingle = whenExprs.size() < 2;		
		for(Pair<Expr, Expr> whenExpr : whenExprs) {
			if ( !isSingle ) 
			{
				asm.dup();
			}					
			
			Expr cond = whenExpr.getFirst();			
			cond.visit(this);			
			asm.eq();
			
			nextWhenLabel = asm.ifeq();						
			Expr stmt = whenExpr.getSecond();
			if(!isSingle) {
				asm.pop();
			}
			
			stmt.visit(this);
			asm.jmp(endCase);		
			asm.label(nextWhenLabel);
		}

		if(!isSingle) {
			asm.pop();
		}
		
		Expr elseExpr = s.getElseExpr();
		if ( elseExpr != null ) {
			elseExpr.visit(this);
		}
		else {			
			asm.loadnull();
		}
				
		asm.label(endCase);
	}
	
	/* (non-Javadoc)
	 * @see leola.ast.ASTNodeVisitor#visit(leola.ast.OnExpr)
	 */
	@Override
	public void visit(OnExpr s) throws EvalException {
		asm.line(s.getLineNumber());
		
		Expr expr = s.getExpr();
		expr.visit(this);
		
		String endOn = asm.nextLabelName();
		String nextOnLabel = null;
		
		asm.on();	/* used as a marker, if the function doesn't have an on statement
					   then it will bubble the exception */
		
		List<OnClause> onClauses = s.getOnClauses();				
		for(OnClause clause : onClauses) {			
			asm.dup();									
			asm.storeAndloadconst(clause.getType());
			asm.mov();
			asm.isa();			
						
			nextOnLabel = asm.ifeq();						
			Stmt stmt = clause.getStmt();
			asm.dup();
			int index = asm.addLocal(clause.getIdentifier());
			asm.storelocal(index);
			
			stmt.visit(this);
			
			if( stmt instanceof Expr) {
				asm.oppop(); /* remove any pushed items */
			}
			
			asm.jmp(endOn);		
			asm.label(nextOnLabel);
		}
				
		asm.label(endOn);			
	}
	
	

	/* (non-Javadoc)
	 * @see leola.ast.ASTNodeVisitor#visit(leola.ast.ThrowStmt)
	 */
	@Override
	public void visit(ThrowStmt s) throws EvalException {
		asm.line(s.getLineNumber());
		
		s.getExpr().visit(this);
		asm.throw_();
		
	}

	/* (non-Javadoc)
	 * @see leola.ast.ASTNodeVisitor#visit(leola.ast.UnaryExpr)
	 */
	@Override
	public void visit(UnaryExpr s) throws EvalException {
		asm.line(s.getLineNumber());
		
		s.getExpr().visit(this);
		UnaryOp op = s.getOp();
		switch(op) {
			case BIT_NOT: {
				asm.bnot();
				break;
			}
			case NEGATE: {
				asm.neg();
				break;
			}
			case NOT: {
				asm.not();
				break;
			}
		}
	}

	
	/* (non-Javadoc)
	 * @see leola.ast.ASTNodeVisitor#visit(leola.ast.VarDeclStmt)
	 */
	@Override
	public void visit(VarDeclStmt s) throws EvalException {
		//asm.line(s.getLineNumber());
		
		String ref = s.getVarName();
		Expr expr = s.getValue();
		
		/*
		 * In case of recursion, we want to tell the locals
		 * about this
		 */
		int index = -1;
		if(asm.useLocals()) {				
			index = asm.addLocal(ref);							
		}
		
		boolean isTailcall = false;
		if ( expr instanceof FuncDefExpr ) {
			TailcallOptimizer tc = new TailcallOptimizer(ref);
			Stmt body = ((FuncDefExpr)expr).getBody();
			body.visit(tc);
			
			isTailcall = tc.isTailcall();
			if ( isTailcall ) {
				this.tailCallStack.add(new Tailcall(ref, tc.getTailCallExpr()));
			}
			
		}
		
		expr.visit(this);
		
		if ( isTailcall ) {
			this.tailCallStack.pop();
		}
		
		// asm.dup(); Seems to be causing a stack leak, I'm not sure why this
		// was here, Variable declaration isn't an 'Expression'
		if(asm.useLocals()) {									
			asm.storelocal(index);	
		}
		else {
			asm.setglobal(ref);
		}
	}
	
	/**
	 * Attempts to load from either local or scope, if not found it will
	 * do a Scope lookup
	 * @param ref
	 * @throws EvalException
	 */
    private void loadglobalmember(String ref) throws EvalException {
        if ( ! asm.load(ref) ) {
        	asm.getglobal(ref);
        }
    }
	
	/**
	 * Determines if the supplied "ref" is a constant, local, global or an
	 * "outstanding" global.
	 * 
	 * @param s
	 * @param ref
	 * @throws EvalException
	 */
	private void loadmember(ASTNode s, String ref, boolean checkConstantFirst) throws EvalException {
		loadmember(s, ref, checkConstantFirst, false);
	}
	
	/**
	 * Determines if the supplied "ref" is a constant, local, global or an
	 * "outstanding" global.
	 * 
	 * @param s
	 * @param ref
	 * @param checkConstantFirst
	 * @param loadconst 
	 * @throws EvalException
	 */
	private void loadmember(ASTNode s, String ref, boolean checkConstantFirst, boolean loadconst) throws EvalException {				
		if ( checkConstantFirst && s.hasFlag(ASTAttributes.IS_PROPERTY) ) {					
			asm.storeAndloadconst(ref);
		}
		else if ( ! asm.load(ref) ) {		
			/* check and see if this is a map lookup */
			LeoString member = LeoString.valueOf(ref);
			Constants constants = asm.getConstants();
			int index = constants.get(member);
			if ( index < 0) {
				
				/* inline string definition */
				if ( s.hasFlag(ASTAttributes.IS_PROPERTY) || loadconst ) {					
					index = constants.store(member);
					asm.loadconst(index);
				}
				else {
					
					/* A variable that hasn't been defined yet */
					asm.getglobal(ref);
				}
			}
			else {
				if(loadconst) {
					asm.loadconst(index);
				}
				else {
					/* A variable that hasn't been defined yet */
					asm.getglobal(ref);
				}
			}
		}		
	}
	
	/* (non-Javadoc)
	 * @see leola.ast.ASTNodeVisitor#visit(leola.ast.VarExpr)
	 */
	@Override
	public void visit(VarExpr s) throws EvalException {			
		asm.line(s.getLineNumber());
		if( s.isMemberAccessChild() ) {
			asm.storeAndloadconst(s.getVarName());
			asm.get();
		}
		else {
			String ref = s.getVarName();
			//loadglobalmember(ref);
			loadmember(s, ref, true);
		}
	}

	/* (non-Javadoc)
	 * @see leola.ast.ASTNodeVisitor#visit(leola.ast.WhileStmt)
	 */
	@Override
	public void visit(WhileStmt s) throws EvalException {
		asm.line(s.getLineNumber());
		
		String beginWhile = asm.label();	
		this.continueLabelStack.push(beginWhile);
		
		Expr cond = s.getCondition();
		cond.visit(this);
		
		String endWhile = asm.ifeq();
		this.breakLabelStack.push(endWhile);
		
		Stmt stmt = s.getStmt();
		stmt.visit(this);
		asm.jmp(beginWhile);		
		asm.label(endWhile);			
		
		this.breakLabelStack.pop();
		this.continueLabelStack.pop();
	}
}

