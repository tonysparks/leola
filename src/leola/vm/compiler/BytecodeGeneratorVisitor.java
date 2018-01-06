/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.vm.compiler;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import leola.ast.ASTNode;
import leola.ast.ASTNodeVisitor;
import leola.ast.ArrayDeclExpr;
import leola.ast.AssignmentExpr;
import leola.ast.BinaryExpr;
import leola.ast.BlockStmt;
import leola.ast.BooleanExpr;
import leola.ast.BreakStmt;
import leola.ast.CaseExpr;
import leola.ast.CatchStmt;
import leola.ast.ClassDeclStmt;
import leola.ast.ContinueStmt;
import leola.ast.DecoratorExpr;
import leola.ast.EmptyStmt;
import leola.ast.Expr;
import leola.ast.FuncDefExpr;
import leola.ast.FuncInvocationExpr;
import leola.ast.GenDefExpr;
import leola.ast.GetExpr;
import leola.ast.IfStmt;
import leola.ast.IntegerExpr;
import leola.ast.IsExpr;
import leola.ast.LongExpr;
import leola.ast.MapDeclExpr;
import leola.ast.NamedParameterExpr;
import leola.ast.NamespaceStmt;
import leola.ast.NewExpr;
import leola.ast.NullExpr;
import leola.ast.ParameterList;
import leola.ast.ProgramStmt;
import leola.ast.RealExpr;
import leola.ast.ReturnStmt;
import leola.ast.SetExpr;
import leola.ast.Stmt;
import leola.ast.StringExpr;
import leola.ast.SubscriptGetExpr;
import leola.ast.SubscriptSetExpr;
import leola.ast.SwitchStmt;
import leola.ast.ThrowStmt;
import leola.ast.TryStmt;
import leola.ast.UnaryExpr;
import leola.ast.VarDeclStmt;
import leola.ast.VarExpr;
import leola.ast.WhileStmt;
import leola.ast.YieldStmt;
import leola.frontend.Token;
import leola.frontend.tokens.TokenType;
import leola.vm.EvalException;
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
    
    @Override
    public void visit(SetExpr s) throws EvalException {
        asm.line(s.getLineNumber());
        
        
        if(s.getOperator().getType() != TokenType.EQUALS) {
            s.getObject().visit(this);            
            asm.getk(s.getIdentifier());
            s.getValue().visit(this);
            
            visitAssignmentOperator(s.getOperator());
        }
        else {
            s.getValue().visit(this);    
        }
        
        s.getObject().visit(this);
        asm.setk(s.getIdentifier());
    }

    /* (non-Javadoc)
     * @see leola.ast.ASTNodeVisitor#visit(leola.ast.SubscriptGetExpr)
     */
    @Override
    public void visit(SubscriptGetExpr s) throws EvalException {
        asm.line(s.getLineNumber());
        
        s.getObject().visit(this);
        s.getElementIndex().visit(this);
        asm.idx();        
    }

    /* (non-Javadoc)
     * @see leola.ast.ASTNodeVisitor#visit(leola.ast.SubscriptSetExpr)
     */
    @Override
    public void visit(SubscriptSetExpr s) throws EvalException {
        asm.line(s.getLineNumber());
        if(s.getOperator().getType() != TokenType.EQUALS) {
            s.getObject().visit(this);
            s.getElementIndex().visit(this);
            asm.idx();
            s.getValue().visit(this);
            
            visitAssignmentOperator(s.getOperator());
        }
        else {
            s.getValue().visit(this);    
        }
        
        s.getObject().visit(this);
        s.getElementIndex().visit(this);
        asm.sidx();
    }

    /* (non-Javadoc)
     * @see leola.ast.ASTNodeVisitor#visit(leola.ast.ArrayDeclExpr)
     */
    @Override
    public void visit(ArrayDeclExpr s) throws EvalException {
        asm.line(s.getLineNumber());
        
        List<Expr> exprs = s.getElements();
        for(Expr expr : exprs) {
            expr.visit(this);
        }
        
        asm.newarray(exprs.size());        
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
            key.appendFlag(ASTNode.MEMBER_PROPERTY);  /* Let VarExpr be converted to strings */
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
        
        VarExpr var = s.getVar();
        String varName = var.getVarName();
        
        s.getVar().visit(this);
        s.getValue().visit(this);
        
        visitAssignmentOperator(s.getOperator());        
        asm.store(varName);
    }

    private void visitAssignmentOperator(Token operator) {        
        switch(operator.getType()) {
            case EQUALS:              break;            
            case PLUS_EQ:  asm.add(); break;
            case MINUS_EQ: asm.sub(); break;
            case STAR_EQ:  asm.mul(); break;
            case SLASH_EQ: asm.div(); break;
            case MOD_EQ:   asm.mod(); break;
            case BSL_EQ:   asm.bsl(); break;
            case BSR_EQ:   asm.bsr(); break;
            case BOR_EQ:   asm.lor(); break;
            case BAND_EQ:  asm.land();break;
            case BXOR_EQ:  asm.xor(); break;
            default: 
                throw new EvalException("Invalid operator: '" + operator.getText() + "'");
        }
    }
    
    /**
     * Visits a Binary Expression
     * 
     * @param op
     * @throws EvalException
     */
    private void visitBinaryExpression(TokenType op) throws EvalException {
        switch(op) {
            case PLUS:  asm.add(); break;
            case MINUS: asm.sub(); break;
            case STAR:  asm.mul(); break;
            case SLASH: asm.div(); break;
            case MOD:   asm.mod(); break;
            
            // comparisons
            case LOGICAL_AND:     asm.and();  break;
            case LOGICAL_OR:      asm.or();   break;
            case REF_EQUALS:      asm.req();  break;
            case REF_NOT_EQUALS:  asm.rneq(); break;
            case D_EQUALS:        asm.eq();   break;
            case NOT_EQUALS:      asm.neq();  break;
            case GREATER_THAN:    asm.gt();   break;
            case GREATER_EQUALS:  asm.gte();  break;
            case LESS_THAN:       asm.lt();   break;
            case LESS_EQUALS:     asm.lte();  break;
            
            // bit ops
            case BITWISE_AND:     asm.land(); break;
            case BITWISE_OR:      asm.lor();  break;
            case BIT_SHIFT_LEFT:  asm.bsl();  break;
            case BIT_SHIFT_RIGHT: asm.bsr();  break;
            case BITWISE_XOR:     asm.xor();  break;
            
            default: 
                throw new EvalException("Unknown BinaryOperator: " + op);            
        }
        
    }
    
    /* (non-Javadoc)
     * @see leola.ast.ASTNodeVisitor#visit(leola.ast.BinaryExpr)
     */
    @Override
    public void visit(BinaryExpr s) throws EvalException {
        asm.line(s.getLineNumber());
        Token operator = s.getOp();
        switch(operator.getType()) {
            case LOGICAL_AND: {
                s.getLeft().visit(this);
                String escape = asm.ifeq();
                
                s.getRight().visit(this);
                String endif = asm.jmp();
                asm.label(escape);
                asm.loadfalse();
                asm.label(endif);
                break;
            }
            case LOGICAL_OR: {
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
                
                visitBinaryExpression(operator.getType());        
            }
        }                        
    }


    /* (non-Javadoc)
     * @see leola.ast.ASTNodeVisitor#visit(leola.ast.BooleanExpr)
     */
    @Override
    public void visit(BooleanExpr s) throws EvalException {
        asm.line(s.getLineNumber());
        
        if(s.getValue()) {
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
        
        if(!this.breakLabelStack.isEmpty()) {
            String label = this.breakLabelStack.peek();
            asm.brk(label);
        }
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
        if(parentClassNAme != null) {            
            asm.addAndloadconst(parentClassNAme);
        }
        else {
            asm.loadnull();
        }
        
        // interfaces
        List<String> interfaces = s.getInterfaceNames();
        if(interfaces != null) {
            for(String i : interfaces) {                
                asm.addAndloadconst(i);
            }
        }
        
        // TODO - Determine how this will take advantage of varargs
        ParameterList params = s.getClassParameters();
        if(params != null) {
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
        List<Expr> superParams = s.getParentClassParams();        
        if(superParams != null) {
            for(Expr e: superParams) {
                
                /* may pass values from sibling class to parent class */
                if(e instanceof VarExpr) {
                    e.appendFlag(ASTNode.MEMBER_PROPERTY);
                }
                
                e.visit(this);
            }
        }
        asm.addAndloadconst(superParams!=null?superParams.size():0);
        
        asm.addAndloadconst(asm.getBytecodeIndex());        
        asm.classdef(interfaces!=null?interfaces.size():0, params.size(), params.isVarargs());
        {            
            Stmt body = s.getClassBodyStmt();
            body.visit(this);
        }
        asm.end();                
        
        
    }

    /* (non-Javadoc)
     * @see leola.ast.ASTNodeVisitor#visit(leola.ast.BlockStmt)
     */
    @Override
    public void visit(BlockStmt s) throws EvalException {
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
        
        List<Stmt> nodes = s.getStatements();
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
     * @see leola.ast.ASTNodeVisitor#visit(leola.ast.ContinueStmt)
     */
    @Override
    public void visit(ContinueStmt s) throws EvalException {
        asm.line(s.getLineNumber());
        
        String label = this.continueLabelStack.peek();
        asm.cont(label);
    }

    /* (non-Javadoc)
     * @see leola.ast.ASTNodeVisitor#visit(leola.ast.DecoratorExpr)
     */
    @Override
    public void visit(DecoratorExpr s) throws EvalException {
        asm.line(s.getLineNumber());
        
        List<Expr> existingParams = s.getArguments();
        List<Expr> arguments = new ArrayList<>(existingParams);        
        arguments.add(s.getDecoratedExpr());
        
        FuncInvocationExpr functionInvokeExpr = new FuncInvocationExpr(s.getDecoratorName(), arguments);
        functionInvokeExpr.setLineNumber(s.getLineNumber());
        
        visit(functionInvokeExpr);
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
            for(Stmt n : s.getStatements()) {            
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
        
        s.getObject().visit(this);
        asm.addAndloadconst(s.getClassName());
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
            for(String name : parameters.getParameters()) {
                asm.addLocal(name);
            }
            
            s.getBody().visit(this);
        } 
        asm.end();         
    }
    
    /**
     * Checks to see if there are Named Parameters or any
     * Expandable variable arguments
     * 
     * @param arguments
     * @return the number of expanded argument index (if any, otherwise 0);
     */
    private int checkArguments(List<Expr> arguments) {
        int expandedArgsIndex = 0;
        if(arguments != null && !arguments.isEmpty()) {            
            boolean hasNamedParameters = false;
            
            /* check to see if there are any NamedParameters,
             * if so, we need to add some additional instructions
             * that effect performance
             */
            int index = 0;
            for(Expr param : arguments) {
                
                // Check if there are named parameters
                if(param instanceof NamedParameterExpr) {
                    hasNamedParameters = true;    
                    NamedParameterExpr nExpr = (NamedParameterExpr)param;
                    param = nExpr.getValueExpr();
                }
                
                // check if the parameters need an Array Expansion (*array)
                if(param instanceof UnaryExpr) {
                    UnaryExpr unaryExpr = (UnaryExpr)param;
                    if(unaryExpr.getOp().getType() == TokenType.STAR) {
                        expandedArgsIndex=index+1;
                    }
                }
                
                index++;
            }
            
            
            for(Expr param : arguments) {
                param.visit(this);
                
                /* mark the end of the parameter,
                 * so that we can properly index
                 * the named parameters
                 */
                if(hasNamedParameters) {
                    asm.paramend();
                }
            }            
        }
        
        return expandedArgsIndex;
    }
    
    /* (non-Javadoc)
     * @see leola.ast.ASTNodeVisitor#visit(leola.ast.FuncInvocationExpr)
     */
    @Override
    public void visit(FuncInvocationExpr s) throws EvalException {        
        asm.line(s.getLineNumber());
        
        List<Expr> arguments = s.getArguments();
        int nargs = arguments.size();
        int expandedArgsIndex = checkArguments(arguments);
        
        s.getCallee().visit(this);
            
        boolean isTailcall = false;
        if(!this.tailCallStack.isEmpty()) {
            Tailcall tc = this.tailCallStack.peek();
            if(tc.tailcallExpr == s) {
                asm.tailcall(nargs, expandedArgsIndex);
                isTailcall = true;
            }
        }
        
        if(!isTailcall) {
            asm.invoke(nargs, expandedArgsIndex);
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
        if(elseStmt != null) {
            elseStmt.visit(this);            
        }
        asm.label(endif);
    }

    /* (non-Javadoc)
     * @see leola.ast.ASTNodeVisitor#visit(leola.ast.GetExpr)
     */
    @Override
    public void visit(GetExpr s) throws EvalException {
        asm.line(s.getLineNumber());
        
        s.getObject().visit(this);
        asm.getk(s.getIdentifier());
    }

    /* (non-Javadoc)
     * @see leola.ast.ASTNodeVisitor#visit(leola.ast.NewExpr)
     */
    @Override
    public void visit(NewExpr s) throws EvalException {
        asm.line(s.getLineNumber());
        
        List<Expr> arguments = s.getArguments();
        int nargs = arguments.size();
        int expandedArgsIndex = checkArguments(arguments);
                
        String className = s.getClassName();        
        asm.addAndloadconst(className);
        
        asm.newobj(nargs, expandedArgsIndex);
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
            if(!isSingle) {
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
            if(!isSingle) {
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
            asm.endcatch();
            s.getCatchStmt().visit(this);
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
        
        asm.markLexicalScope();
        asm.addAndstorelocal(s.getIdentifier());
        
        Stmt stmt = s.getBody();
        stmt.visit(this);
        
        if( stmt instanceof Expr) {
            asm.oppop(); /* remove any pushed items */
        }
        asm.unmarkLexicalScope();
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
        
        TokenType operator = s.getOp().getType();
        switch(operator) {
            case BITWISE_NOT: asm.bnot(); break;
            case MINUS:       asm.neg();  break;
            case NOT:         asm.not();  break;
            case STAR:                    break;
            default:
                throw new EvalException("Unknown UnaryOperator: " + operator);
        }
    }

    
    /* (non-Javadoc)
     * @see leola.ast.ASTNodeVisitor#visit(leola.ast.VarDeclStmt)
     */
    @Override
    public void visit(VarDeclStmt s) throws EvalException {
        asm.line(s.getLineNumber());
        
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
        if ( checkConstantFirst && s.hasFlag(ASTNode.MEMBER_PROPERTY) ) {                    
            asm.addAndloadconst(ref);
        }
        else if ( ! asm.load(ref) ) {        
            /* check and see if this is a map lookup */
            LeoString member = LeoString.valueOf(ref);
            Constants constants = asm.getConstants();
            int index = constants.get(member);
            if ( index < 0) {
                
                /* inline string definition */
                if ( s.hasFlag(ASTNode.MEMBER_PROPERTY) || loadconst ) {                    
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
        loadmember(s, s.getVarName(), true);
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

