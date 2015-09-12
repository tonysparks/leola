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
import leola.ast.CatchStmt;
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
import leola.ast.NamedParameterExpr;
import leola.ast.NamespaceAccessExpr;
import leola.ast.NamespaceStmt;
import leola.ast.NewExpr;
import leola.ast.NullExpr;
import leola.ast.OwnableExpr;
import leola.ast.ProgramStmt;
import leola.ast.RealExpr;
import leola.ast.ReturnStmt;
import leola.ast.Stmt;
import leola.ast.StringExpr;
import leola.ast.SwitchStmt;
import leola.ast.ThrowStmt;
import leola.ast.TryStmt;
import leola.ast.UnaryExpr;
import leola.ast.UnaryExpr.UnaryOp;
import leola.ast.VarDeclStmt;
import leola.ast.VarExpr;
import leola.ast.WhileStmt;
import leola.ast.YieldStmt;
import leola.frontend.EvalException;
import leola.frontend.parsers.ParameterList;
import leola.vm.Leola;
import leola.vm.compiler.EmitterScope.ScopeType;
import leola.vm.types.LeoString;
import leola.vm.util.Pair;

/**
 * Generates {@link Bytecode} based off of an Abstract Syntax Tree.
 * 
 * 
 * @author Tony
 *
 */
public class BytecodeGeneratorVisitor implements ASTNodeVisitor {

	/**
	 * The assembler
	 */
	private BytecodeEmitter asm;
		
	private Stack<String> breakLabelStack;
	private Stack<String> continueLabelStack;
	private Stack<Tailcall> tailCallStack;

	/**
	 * Marks a tail call recursive method.
	 */
	static class Tailcall {
		String name;		
		FuncInvocationExpr tailcallExpr;
		
		Tailcall(String name, FuncInvocationExpr tailcallExpr) { 
			this.name = name; 		 
			this.tailcallExpr = tailcallExpr;
		}
	}
	
	
	/**
	 * @param runtime
	 * @param symbols
	 */
	public BytecodeGeneratorVisitor(Leola runtime, EmitterScopes symbols) {
		this.asm = new BytecodeEmitter(symbols);
		this.asm.setDebug(runtime.getArgs().isDebugMode());
			
		this.breakLabelStack = new Stack<String>();
		this.continueLabelStack = new Stack<String>();
		this.tailCallStack = new Stack<BytecodeGeneratorVisitor.Tailcall>();
	}
		
	/**
	 * @return the asm
	 */
	public BytecodeEmitter getAsm() {
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
			asm.getk(reference);
						
			indexExpr.visit(this);
			asm.idx();
			
		}
		else {					
			String reference = s.getVariableName();
			loadglobalmember(reference);

			Expr indexExpr = s.getElementIndex();
			indexExpr.visit(this);
			asm.idx();
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
			asm.getk(reference);				
		}
		else {						
			String reference = s.getVariableName();
			loadglobalmember(reference);
		}
		
		Expr indexExpr = s.getElementIndex();
		indexExpr.visit(this);		
		asm.sidx();
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
			asm.swap();
			
			Expr lhs = s.getLhsExpr();
			if ( lhs != null ) {																
				lhs.setMemberAccess(true);
				lhs.appendFlag(s.getFlags()); 
				lhs.visit(this);												
				
			}
			else {				
				asm.setk(s.getVarName());
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
					asm.getk(reference);
					
					Expr indexExpr = setExpr.getElementIndex();
					indexExpr.visit(this);
					
					asm.idx();
				}
								
				Expr expr = s.getExpr();
				expr.visit(this);
		
				BinaryOp op = s.getBinaryOp();
				visitBinaryExpression(op);
				
				asm.swap();
				lhs.visit(this);		
		
			}
			else {			

				asm.dup();
				
				String ref = s.getVarName();
				asm.getk(ref);
								
				Expr e = s.getExpr();
				e.visit(this);
				
				visitBinaryExpression(s.getBinaryOp());
				asm.swap();												
				asm.setk(ref);			
				
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
					asm.idx();
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
		asm.idx();
	}

	/* (non-Javadoc)
	 * @see leola.ast.ASTNodeVisitor#visit(leola.ast.ChainedArrayAccessSetExpr)
	 */
	@Override
	public void visit(ChainedArrayAccessSetExpr s) throws EvalException {
		asm.line(s.getLineNumber());
		
		s.getElementIndex().visit(this);
		asm.sidx();
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
			asm.swap();
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
			// TODO -- This seems extremely whonkie!
		    // Had to create new OPCODE (shift) to support
		    // this.  Revisit to fix so that: 
		    // a) we don't need this chained* non-sense
		    // b) we don't need a 'shift' opcode
			if ( lhs instanceof ChainedArrayAccessSetExpr) {							    
			    asm.swap();
                
                ChainedArrayAccessSetExpr e = (ChainedArrayAccessSetExpr)lhs;
                e.getElementIndex().visit(this);                
                asm.dup();
                asm.rotr(4);
                asm.idx();
                
                asm.swap();
                visitBinaryExpression(s.getBinaryOp());
                asm.rotr(3);                   
                asm.sidx();
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
            boolean hasNamedParameters = false;
            /* check to see if there are any NamedParameters,
             * if so, we need to add some additional instructions
             * that effect performance
             */
            for(Expr param : params) {
                if(param instanceof NamedParameterExpr) {
                    hasNamedParameters = true;
                    break;
                }
            }
            
            
            for(Expr param : params) {
                param.visit(this);
                
                /* mark the end of the parameter,
                 * so that we can properly index
                 * the named parameters
                 */
                if(hasNamedParameters) {
                    asm.paramend();
                }
            }
			
			nargs = params.length;
		}
		
		asm.rotl(nargs);
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
		asm.addAndloadconst(className);
		
		
		// parent class name
		String parentClassNAme = s.getParentClassName();
		if ( parentClassNAme != null ) {			
			asm.addAndloadconst(parentClassNAme);
		}
		else {
			asm.loadnull();
		}
		
		// interfaces
		String[] interfaces = s.getInterfaceNames();
		if ( interfaces != null ) {
			for(String i : interfaces) {				
				asm.addAndloadconst(i);
			}
		}
		
		// TODO - Determine how this will take advantage of varargs
		ParameterList params = s.getClassParameters();
		if ( params != null ) {
			for(String ref:params.getParameters()) {				
				asm.addAndloadconst(ref);
			}
		}							
		asm.addAndloadconst(params!=null?params.size():0);
		
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
		asm.addAndloadconst(superParams!=null?superParams.length:0);
		
		asm.addAndloadconst(asm.getBytecodeIndex());		
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
									(parent instanceof GenDefExpr) ||
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
		asm.addAndloadconst(name);
		
		asm.namespacedef();
		{			
			s.getStmt().visit(this);
		}
		asm.end();
		asm.addAndstorelocal(name);						
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

		asm.addAndloadconst(s.getValue());
	}
	
	@Override
	public void visit(IntegerExpr s) throws EvalException {
		asm.line(s.getLineNumber());

		asm.addAndloadconst(s.getValue());
	}

	/* (non-Javadoc)
	 * @see leola.ast.ASTNodeVisitor#visit(leola.ast.LongExpr)
	 */
	@Override
	public void visit(LongExpr s) throws EvalException {
		asm.line(s.getLineNumber());

		asm.addAndloadconst(s.getValue());		
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
			asm.addAndloadconst(type);
			
			asm.swap();
		}
		else {

			String type = s.getClassName();			
			asm.addAndloadconst(type);
			
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
		asm.gendef(parameters.size(), parameters.isVarargs());
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
	 * @see leola.ast.ASTNodeVisitor#visit(leola.ast.NamedParameterStmt)
	 */
	@Override
	public void visit(NamedParameterExpr s) throws EvalException {
	    int index = asm.getConstants().store(s.getParameterName());
	    
	    asm.loadname(index);
	    s.getValueExpr().visit(this);
	    
	}
	
	/* (non-Javadoc)
	 * @see leola.ast.ASTNodeVisitor#visit(leola.ast.FuncDefExpr)
	 */
	@Override
	public void visit(FuncDefExpr s) throws EvalException {
		asm.line(s.getLineNumber());
		
		ParameterList parameters = s.getParameters();
		asm.funcdef(parameters.size(), parameters.isVarargs());
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
		    
		    boolean hasNamedParameters = false;
		    /* check to see if there are any NamedParameters,
		     * if so, we need to add some additional instructions
		     * that effect performance
		     */
		    for(Expr param : params) {
		        if(param instanceof NamedParameterExpr) {
		            hasNamedParameters = true;
		            break;
		        }
		    }
		    
		    
			for(Expr param : params) {
				param.visit(this);
				
				/* mark the end of the parameter,
				 * so that we can properly index
				 * the named parameters
				 */
				if(hasNamedParameters) {
				    asm.paramend();
				}
			}
			
			nargs = params.length;
		}
				
		if ( s.isMemberAccessChild() ) {
			/* Member access */
			
			asm.rotl(nargs);
			
			String functionName = s.getFunctionName();
//			asm.addAndloadconst(functionName);			
//			asm.get(); 
			asm.getk(functionName);
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
		if ( identifier != null && (expr instanceof MemberAccessExpr && !(expr instanceof NamespaceAccessExpr))) {
		    asm.getk(identifier);
		}
			
		expr.visit(this);							
	}

	/* (non-Javadoc)
	 * @see leola.ast.ASTNodeVisitor#visit(leola.ast.NewExpr)
	 */
	@Override
	public void visit(NewExpr s) throws EvalException {
		asm.line(s.getLineNumber());
		
		int nargs = 0;
		Expr[] params = s.getParameters();
		if(params != null) {
            boolean hasNamedParameters = false;
            /* check to see if there are any NamedParameters,
             * if so, we need to add some additional instructions
             * that effect performance
             */
            for(Expr param : params) {
                if(param instanceof NamedParameterExpr) {
                    hasNamedParameters = true;
                    break;
                }
            }
            
            
            for(Expr param : params) {
                param.visit(this);
                
                /* mark the end of the parameter,
                 * so that we can properly index
                 * the named parameters
                 */
                if(hasNamedParameters) {
                    asm.paramend();
                }
            }
            
            nargs = params.length;
		}
		
		String className = s.getClassName();		
		asm.addAndloadconst(className);
		
		asm.newobj(nargs);
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
		
		asm.addAndloadconst(s.getValue());				
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
	 * @see leola.ast.ASTNodeVisitor#visit(leola.ast.ThrowStmt)
	 */
	@Override
	public void visit(ThrowStmt s) throws EvalException {
		asm.line(s.getLineNumber());
		
		s.getExpr().visit(this);
		asm.throw_();
		
	}

	/* (non-Javadoc)
	 * @see leola.ast.ASTNodeVisitor#visit(leola.ast.TryStmt)
	 */
	@Override
	public void visit(TryStmt s) throws EvalException {
		asm.line(s.getLineNumber());
		
		boolean hasCatch = s.getCatchStmt() != null;
		boolean hasFinally = s.getFinallyStmt() != null;
		
		if(hasFinally) {
			asm.initfinally();
		}
				
		if(hasCatch) {
			asm.initcatch();
		}
						
		s.getStmt().visit(this);
		
		/* if the VM has made it this
		 * far, go ahead and pop off
		 * the Catch block (if there is
		 * one)
		 */
		if(hasCatch) {
			asm.endblock();
		}
		
		String finallyLabel = null;
		String endLabel = null;
		
		/*
		 * If we have a finally block, 
		 * jump to that, otherwise jump
		 * over the Catch clause
		 */
		if(hasFinally) {			
			finallyLabel = asm.jmp();
		}
		else {
			endLabel = asm.jmp();
		}
					
		
		if(hasCatch) {
			asm.taginitcatch();
			s.getCatchStmt().visit(this);
			asm.endcatch();
		}
						
		if(hasFinally) {
			asm.label(finallyLabel);
			
			asm.taginitfinally();			
			s.getFinallyStmt().visit(this);
			asm.endfinally();	
		}
		else {
			
			/* 
			 * There is no finally block and the 
			 * VM made it without an Error being
			 * thrown, so this lets us skip the
			 * catch block
			 */
			asm.label(endLabel);					
		}
		
	}
	
	/* (non-Javadoc)
	 * @see leola.ast.ASTNodeVisitor#visit(leola.ast.OnStmt)
	 */
	@Override
	public void visit(CatchStmt s) throws EvalException {
		asm.line(s.getLineNumber());
				
		String endOn = asm.nextLabelName();													
		Stmt stmt = s.getBody();
		//asm.dup();
		asm.addAndstorelocal(s.getIdentifier());
		
		stmt.visit(this);
		
		if( stmt instanceof Expr) {
			asm.oppop(); /* remove any pushed items */
		}
		
		asm.jmp(endOn);										
		asm.label(endOn);		
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
		if(asm.usesLocals()) {				
			index = asm.addLocal(ref);							
		}
		
		boolean isTailcall = false;
		if ( expr instanceof FuncDefExpr ) {
			TailcallOptimizerVisitor tc = new TailcallOptimizerVisitor(ref);
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
		if(asm.usesLocals()) {									
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
			asm.addAndloadconst(ref);
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
		    asm.getk(s.getVarName());
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

