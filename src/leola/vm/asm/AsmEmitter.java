/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.vm.asm;

import static leola.vm.Opcodes.ADD;
import static leola.vm.Opcodes.AND;
import static leola.vm.Opcodes.BNOT;
import static leola.vm.Opcodes.BSL;
import static leola.vm.Opcodes.BSR;
import static leola.vm.Opcodes.CLASS_DEF;
import static leola.vm.Opcodes.FUNC_DEF;
import static leola.vm.Opcodes.DIV;
import static leola.vm.Opcodes.DUP;
import static leola.vm.Opcodes.END_BLOCK;
import static leola.vm.Opcodes.END_FINALLY;
import static leola.vm.Opcodes.END_ON;
import static leola.vm.Opcodes.EQ;
import static leola.vm.Opcodes.GEN_DEF;
import static leola.vm.Opcodes.GET;
import static leola.vm.Opcodes.GET_GLOBAL;
import static leola.vm.Opcodes.GET_NAMESPACE;
import static leola.vm.Opcodes.GT;
import static leola.vm.Opcodes.GTE;
import static leola.vm.Opcodes.IDX;
import static leola.vm.Opcodes.IFEQ;
import static leola.vm.Opcodes.INIT_FINALLY;
import static leola.vm.Opcodes.INIT_ON;
import static leola.vm.Opcodes.INVOKE;
import static leola.vm.Opcodes.IS_A;
import static leola.vm.Opcodes.JMP;
import static leola.vm.Opcodes.LAND;
import static leola.vm.Opcodes.LINE;
import static leola.vm.Opcodes.LOAD_CONST;
import static leola.vm.Opcodes.LOAD_FALSE;
import static leola.vm.Opcodes.LOAD_LOCAL;
import static leola.vm.Opcodes.LOAD_NAME;
import static leola.vm.Opcodes.LOAD_NULL;
import static leola.vm.Opcodes.LOAD_OUTER;
import static leola.vm.Opcodes.LOAD_TRUE;
import static leola.vm.Opcodes.LOR;
import static leola.vm.Opcodes.LT;
import static leola.vm.Opcodes.LTE;
import static leola.vm.Opcodes.MOD;
import static leola.vm.Opcodes.MOV;
import static leola.vm.Opcodes.MOVN;
import static leola.vm.Opcodes.MUL;
import static leola.vm.Opcodes.NEG;
import static leola.vm.Opcodes.NEQ;
import static leola.vm.Opcodes.NEW_OBJ;
import static leola.vm.Opcodes.NEW_ARRAY;
import static leola.vm.Opcodes.NEW_MAP;
import static leola.vm.Opcodes.NAMESPACE_DEF;
import static leola.vm.Opcodes.NOT;
import static leola.vm.Opcodes.OPCODE;
import static leola.vm.Opcodes.OPPOP;
import static leola.vm.Opcodes.OR;
import static leola.vm.Opcodes.PARAM_END;
import static leola.vm.Opcodes.POP;
import static leola.vm.Opcodes.REQ;
import static leola.vm.Opcodes.RET;
import static leola.vm.Opcodes.SET;
import static leola.vm.Opcodes.SIDX;
import static leola.vm.Opcodes.SET_ARG1;
import static leola.vm.Opcodes.SET_ARG2;
import static leola.vm.Opcodes.SET_ARGx;
import static leola.vm.Opcodes.SET_ARGsx;
import static leola.vm.Opcodes.SET_GLOBAL;
import static leola.vm.Opcodes.SHIFT;
import static leola.vm.Opcodes.STORE_LOCAL;
import static leola.vm.Opcodes.STORE_OUTER;
import static leola.vm.Opcodes.SUB;
import static leola.vm.Opcodes.SWAP;
import static leola.vm.Opcodes.TAIL_CALL;
import static leola.vm.Opcodes.THROW;
import static leola.vm.Opcodes.XOR;
import static leola.vm.Opcodes.YIELD;
import static leola.vm.Opcodes.xLOAD_LOCAL;
import static leola.vm.Opcodes.xLOAD_OUTER;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import leola.vm.Opcodes;
import leola.vm.asm.Scope.ScopeType;
import leola.vm.exceptions.LeolaRuntimeException;
import leola.vm.types.LeoDouble;
import leola.vm.types.LeoInteger;
import leola.vm.types.LeoLong;
import leola.vm.types.LeoObject;
import leola.vm.types.LeoString;
import leola.vm.util.ArrayUtil;

/**
 * The pseudo assembler language creator.  Easily build/emit opcode codes via the assembler methods.
 * 
 * @author Tony
 *
 */
public class AsmEmitter {	
	private Instructions instructions;	
	private Labels labels;
	private Stack<AsmEmitter> inner;
	private List<AsmEmitter> asms;

	private Stack<Integer> lexicalScopes;	
		
	private Symbols symbols;
	private Scope localScope;
	
	private DebugSymbols debugSymbols;
	
	private boolean usesLocals;
	private boolean debug;
	
	private boolean hasParamIndexes;
	
	private int currentLineNumber;
	private int numArgs;
	private boolean isVarargs;
	
	private Stack<Integer> blockSize;
	private boolean hasBlocks;
	
	/**
	 * @param symbols the current scoped symbols
	 */
	public AsmEmitter(Symbols symbols) {
		this.symbols = symbols;
		this.usesLocals = false;
		this.isVarargs = false;
		this.hasBlocks = false;
		this.hasParamIndexes = false;
		
		this.currentLineNumber = -1;
		this.lexicalScopes = new Stack<Integer>();
		this.blockSize = new Stack<Integer>();
		this.debugSymbols = new DebugSymbols();
		
		this.setDebug(false);
	}

	/**
	 * @return true if the current scope stores variables on the stack
	 * or in the current environment
	 */
	public boolean usesLocals() {
		AsmEmitter peek = peek();
		return peek.usesLocals || !peek.lexicalScopes.isEmpty();
	}

	/**
	 * Mark the beginning of an inner scope
	 */
	public void markLexicalScope() {
		int index = getLocals().getIndex();
		peek().lexicalScopes.push(index);
		
		if(isDebug()) {
			peek().debugSymbols.startScope(getInstructionCount());
		}
	}
	
	/**
	 * Leave the scope
	 */
	public void unmarkLexicalScope() {
		if(peek().lexicalScopes.isEmpty()) {
			throw new LeolaRuntimeException("Illegal lexical scope");
		}
		
		/*
		 * This allows us for reusing the stack space
		 * for other local variables that will be in
		 * of scope by the time they get here
		 */
		int index = peek().lexicalScopes.pop();
		int currentIndex = getLocals().getIndex();
		if(currentIndex != index) {			
			getLocals().setIndex(index);				
		}
		
		if(isDebug()) {
			peek().debugSymbols.endScope(getInstructionCount());
		}
	}
	
	/**
	 * @return the debug
	 */
	public boolean isDebug() {
		return debug;
	}
	
	/**
	 * @param debug the debug to set
	 */
	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	
	private void incrementMaxstackSize(int delta) {
		peek().localScope.incrementMaxstacksize(delta);
	}
	
	private void incrementMaxstackSize() {
		peek().localScope.incrementMaxstacksize(1);
	}
	private void decrementMaxstackSize(int delta) {
		peek().localScope.incrementMaxstacksize(-delta);
	}
	
	private void decrementMaxstackSize() {
		peek().localScope.incrementMaxstacksize(-1);
	}
	
	
	/**
	 * @return the max stack size this bytecode chunk will require
	 */
	public int getMaxstacksize() {
		return peek().localScope.getMaxstacksize();
	}
   
	   
    /**
     * Starts a block of code, pushing a new {@link Scope}
     */
    public void start() {
        start(ScopeType.LOCAL_SCOPE);
    }
    
    /**
     * Starts an assembler scope
     * 
     * @param scopeType the type of scope
     */
    public void start(ScopeType scopeType) {        
        this.localScope = scopeType == ScopeType.GLOBAL_SCOPE ? 
                              this.symbols.getGlobalScope()
                            : this.symbols.pushScope(scopeType); 

        this.usesLocals = scopeType == ScopeType.LOCAL_SCOPE;
        
        this.instructions = new Instructions();     
        this.labels = new Labels();
                
        this.inner = new Stack<AsmEmitter>();
        this.asms = new ArrayList<AsmEmitter>();
        
        
        this.inner.push(this);
        incrementMaxstackSize(2);
    }

	
	/**
	 * Ends a block of code
	 */
	public AsmEmitter end() {	
		
		/* reconcile the labels */
		reconcileLabels();
		
		/* reconcile any Outers */		
		reconcileOuters(peek());
		
		this.symbols.popScope();
		
		AsmEmitter asm = this;
		if ( !this.inner.isEmpty() ) {
			asm = this.inner.pop();
		}
		
		return asm;
	}
	
	/**
	 * Reconciles the labels
	 */
	private void reconcileLabels() {
	    getLabels().reconcileLabels(peek());	    
	}
	
	/**
	 * Reconciles the outer variables
	 * 
	 * @param asm - this scoped Asm
	 */
    private void reconcileOuters(AsmEmitter asm) {
        Outers outers = asm.getOuters();

        for (int i = 0; i < outers.getNumberOfOuters(); i++) {
            OuterDesc outer = outers.get(i);            
            int outerUpIndex = outer.getUp();
            
            /*
             * If the outer is not in this scope (UP index is != 0),
             * then we must find the parent Scope to reconcile
             * it
             */
            if (outerUpIndex > 0) {
                
                Scope scope = asm.localScope;
                if (scope != null) {

                    /* find the asm from the parent scope */
                    scope = scope.getParent();
                    AsmEmitter outerAsm = findAsmByScope(scope);
                    if (outerAsm != null) {
                        int nup = outerUpIndex - 1;

                        /* if the outer is several parent scopes deep, we'll need to store
                         * an outer in this parent scope, so that we can chain this outer
                         * to finally become an xLOAD_LOCAL once it reaches the appropriate
                         * scope
                         */
                        if (nup > 0) {
                            int store = outerAsm.localScope.getOuters().store(new OuterDesc(outer.getIndex(), nup));
                            outerAsm.linstrx(xLOAD_OUTER, store);
                        }
                        else {
                            outerAsm.linstrx(xLOAD_LOCAL, outer.getIndex());
                        }
                    }
                }
            }
        }
    }
	
	/**
	 * @return the globals
	 */
	public Outers getOuters() {
		return this.symbols.peek().getOuters();
	}
	
	/**
	 * @return the constants
	 */
	public Constants getConstants() {
		return this.symbols.peek().getConstants();
	}
	
	/**
	 * @return the locals
	 */
	public Locals getLocals() {
		return this.symbols.peek().getLocals();
	}
	
	/**
	 * @return the current scope
	 */
	public Scope getScope() {
		return this.symbols.peek();
	}
	
	/**
     * Reserve space for the number of locals
	 * @param numberOfLocals the number of locals to reserve space for
	 */
	public void allocateLocals(int numberOfLocals) {
		getLocals().allocate(numberOfLocals);
	}
	

	/**
	 * Finds the associated Asm by Scope
	 * @param scope the scope to find the representative Asm
	 * @return the Asm if found, null if not found
	 */
	private AsmEmitter findAsmByScope(Scope scope) {
		for(int i = 0; i < this.inner.size(); i++ ) {
			AsmEmitter asm = this.inner.get(i);
			
			/* check by reference */
			if ( asm.localScope == scope ) {
				return asm;
			}
		}
		
		return null;
	}
	
	

	/**
	 * Stores the {@link LeoObject} in the constants table and
	 * emits a load instruction for it.
	 * 
	 * @param obj
	 */
	public void storeAndloadconst(LeoObject obj) {
		Constants constants = getConstants();
		int index = constants.store(obj);
		loadconst(index);
	}
	
	/**
     * Stores the constant 'str' into the constants table and
     * emits a load instruction for it.
     * 
     * @param str
     */
    public void storeAndloadconst(String str) {
        storeAndloadconst(LeoString.valueOf(str));
    }
	
    
    /**
     * Stores the integer in the constants table and
     * emits a load instruction for it.
     * 
     * @param obj
     */
	public void storeAndloadconst(int i) {
		storeAndloadconst(LeoInteger.valueOf(i));
	}
	
	
	/**
     * Stores the double in the constants table and
     * emits a load instruction for it.
     * 
     * @param obj
     */
	public void storeAndloadconst(double i) {
		storeAndloadconst(LeoDouble.valueOf(i));
	}
	
	
	/**
     * Stores the long in the constants table and
     * emits a load instruction for it.
     * 
     * @param obj
     */
	public void storeAndloadconst(long i) {
		storeAndloadconst(new LeoLong(i));
	}
		
	/**
	 * Loads the variable, either as a local, scoped or an {@link Outer}
	 * 
	 * @param ref
	 * @return true if loaded, false otherwise
	 */
	public boolean load(String ref) {
		boolean success = true;			
		int	i = getLocals().get(ref);
		if ( i > -1) {
			loadlocal(i);
		}		
		else {					
			
			OuterDesc upvalue = this.symbols.find(ref);
			if ( upvalue == null ) {				
				success = false;
			}
			else {
				
				Outers outers = getOuters();
				
				int store = outers.store(upvalue);
				loadouter(store);
			}	
		}		
				
		return success;
	}
	
	/**
	 * Adds the symbol to the {@link Locals}.
	 * 
	 * @param reference
	 * @return the index it is stored in the locals table
	 */
	public int addLocal(String reference) {
		if(isDebug()) {
			peek().debugSymbols.store(reference, getInstructionCount());
		}
		
		Locals locals = getLocals();
		return locals.store(reference);
	}
	
	/**
	 * Emits a store instruction.
	 * 
	 * Stores a variable, either as a local, global or an {@link Outer}
	 * 
	 * @param ref the reference name of the variable
	 */
	public void store(String ref) {
		int index = getLocals().get(ref);
		
		/* this is a global */
		if ( index == -1 ) {						
			OuterDesc upvalue = this.symbols.find(ref);
			if ( upvalue == null ) {
				setglobal(ref);
			}
			else {
				Outers outers = getOuters();
				index = outers.store(upvalue);
				storeouter(index);
			}
		}
		/* Otherwise this is a local */
		else {
			storelocal(index);
		}			
	}
	
	/**
	 * @return the number of instructions
	 */
	public int getInstructionCount() {
		return getInstructions().getCount();
	}
	
	/**
	 * @return the labels
	 */
	public Labels getLabels() {
		return peek().labels;
	}
	
	/**
	 * @return the instructions
	 */
	public Instructions getInstructions() {
		return peek().instructions;
	}
	
	
	/**
	 * @return the current scoped Asm
	 */
	public AsmEmitter peek() {
		return this.inner.peek();
	}
	
	/**
	 * @return the current instruction
	 */
	private int peekInstr() {
		Instructions instrs = peek().instructions;
		return instrs.peekLast();
	}
	
	/**
	 * Replaces the last instruction with the supplied instruction
	 * @param instr
	 */
	private void setInstr(int instr) {
	    Instructions instrs = peek().instructions;
	    instrs.setLast(instr);
	}
	
	
	/**
	 * Adds an instruction to this (local) scoped Asm
	 * 
	 * @param opcode
	 * @param argx
	 */
	private void linstrx(int opcode, int argx) {
		this.instructions.add(SET_ARGx(opcode, argx));
	}
	
	/**
	 * Outputs an instruction with no arguments
	 * 
	 * @param opcode
	 */
	private void instr(int opcode) {
		this.inner.peek().instructions.add(opcode);
	}
	
	/**
	 * Outputs an instruction with 1 (x) argument
	 * 
	 * @param opcode
	 * @param argx (x size argument -- see {@link Opcodes}).
	 */
	private void instrx(int opcode, int argx) {
		instr(SET_ARGx(opcode, argx));
	}
	
	/**
     * Outputs an instruction with 1 (x-signed) argument
     * 
     * @param opcode
     * @param argsx (x signed size argument -- see {@link Opcodes}).
     */
    private void instrsx(int opcode, int argsx) {
        instr(SET_ARGsx(opcode, argsx));
    }
	
	/**
	 * Outputs an instruction with 1 argument
	 * 
	 * @param opcode
	 * @param arg1
	 */
	private void instr1(int opcode, int arg1) {
		instr(SET_ARG1(opcode, arg1));
	}
	
	/**
	 * Outputs an instruction with 2 arguments
	 * 
	 * @param opcode
	 * @param arg1
	 * @param arg2
	 */
	private void instr2(int opcode, int arg1, int arg2) {		
		instr(SET_ARG2(SET_ARG1(opcode, arg1), arg2));
	}
	
	/**
	 * Marks a Label, so it can be eventually calculated for a jmp delta
	 * 
	 * @param opcode
	 * @param label
	 */
	private void markLabel(int opcode, String label) {		
		instr(opcode); // will eventually be replaced
		getLabels().markLabel(this, label, opcode);				
	}
	
	/**
	 * Creates a label with the supplied name
	 * 
	 * @param name
	 */
	public void label(String name) {		
		getLabels().setLabel(this, name);
	}
	
	
	/**
	 * Creates a label with a sequenced name.
	 * 
	 * @return the label name
	 */
	public String label() {
		String labelName = nextLabelName();
		label(labelName);
		
		return labelName;
	}
	
	
	/**
	 * Generates the next sequenced labeled name
	 * 
	 * @return the labeled name
	 */
	public String nextLabelName() {
	    return peek().labels.nextLabelName();
	}
	
	
	
	/*================================================================================
	 * The Assembler
	 *================================================================================*/
	 
	
	
	/**
	 * Emits the LINE opcode.  Only enabled if the debug flags are set.  This marks
	 * the line numbers in the Leola script with the associated byte code.
	 *  
	 * @param line
	 */
	public void line(int line) {
		if ( this.isDebug() ) {
			if ( line != this.currentLineNumber && line != 0 
				&& (getInstructionCount() > 0 && OPCODE(peekInstr()) != LINE )
				) {
				this.currentLineNumber = line;
				
				instrx(LINE, line);
			}
		}
	}
	
	public void loadconst(int index) {
		instrx(LOAD_CONST, index);	
		
		incrementMaxstackSize();
	}
	public void loadlocal(int index) {		
		instrx(LOAD_LOCAL, index);
		incrementMaxstackSize();
	}

	public void loadouter(int index) {
		instrx(LOAD_OUTER, index);
		incrementMaxstackSize();
	}
	
	public void loadname(int index) {
	    instrx(LOAD_NAME, index);
        incrementMaxstackSize();
	}
	
	public void paramend() {
        instr(PARAM_END);
        incrementMaxstackSize();
        
        peek().hasParamIndexes = true;
    }
	
	
	public void storelocal(int index) {		
		instrx(STORE_LOCAL, index);
		decrementMaxstackSize();
	}
	
	public void storeouter(int index) {
		instrx(STORE_OUTER, index);
		decrementMaxstackSize();
	}
		
	public void loadnull() {
		instr(LOAD_NULL);
		incrementMaxstackSize();
	}
	public void loadtrue() {
		instr(LOAD_TRUE);
		incrementMaxstackSize();
	}
	public void loadfalse() {
		instr(LOAD_FALSE);
		incrementMaxstackSize();
	}
	
	public void shift(int index) {
		instrx(SHIFT, index);
	}
	
	
	public void pop() {
		instr(POP);
		decrementMaxstackSize();
	}
	public void oppop() {
		boolean dupOptimization = false;
		
		/* Check to see if there was an unused expression;
		 * if there was, that means there is a DUP instruction
		 * that we can ignore, along with this OPPOP
		 */
		Instructions instructions = peek().instructions;
		int numberOfInstrs = instructions.getCount();
		if(numberOfInstrs > 1) {
		    
			/* We go back two instructions because the expression
			 * will DUP the expression value before it does a STORE
			 */
			int instr = instructions.get(numberOfInstrs-2);						
			if(OPCODE(instr) == DUP) {
				instructions.remove(numberOfInstrs-2);
				dupOptimization = true;
			}
		}
		
		if(!dupOptimization) {
			instr(OPPOP);
		}
		
	}
	public void dup() {
		instr(DUP);
		incrementMaxstackSize();
	}
	
	public void on() {
		int instr = peekInstr();
		if ( OPCODE(instr) == INVOKE ) {
			setInstr(SET_ARG2(instr, 1));			
		}
	}
	
	public void yield() {
		instr(YIELD);
	}
	
	public void ret() {
		instr(RET);
	}
	
	public void mov() {
		instr(MOV);
	}
	public void movn(int n) {
		if ( n != 0 ) {
			instrx(MOVN, n);
		}
	}
	public void swap(int amount) {
		if ( amount > 0 ) {
			instrx(SWAP, amount);
		}
	}
	
	public void tailcall(int numberOfParameters) {
		instrx(TAIL_CALL, numberOfParameters);
	}
	
	public void jmp(String label) {
		markLabel(JMP, label);
	}
	public String jmp() {
		String label = nextLabelName();
		jmp(label);
		return label;
	}
	
	public void jmp(int offset) {
	    if(offset != 0) {
	        instrsx(JMP, offset);
	    }
	}
	
	public void brk(String label) {
		jmp(label);
	}
	
	public void cont(String label) {
		jmp(label);
	}
	
	public void newobj(int nargs) {
		instrx(NEW_OBJ, nargs);
		incrementMaxstackSize(nargs);
	}
	
	public void newarray(int initialSize) {
		instrx(NEW_ARRAY, initialSize);
		incrementMaxstackSize(initialSize);
	}
	public void newmap(int initialSize) {
		instrx(NEW_MAP, initialSize);
		incrementMaxstackSize(initialSize);
	}
	
	public void idx() {
	    instr(IDX);
        decrementMaxstackSize();
	}
	public void sidx() {
        instr(SIDX);
        decrementMaxstackSize();
    }
	
	public void get() {
		instr(GET);
		decrementMaxstackSize();
	}
	public void set() {
		instr(SET);
		decrementMaxstackSize();
	}
	
	public void getnamespace(String stringconst) {
		int index = getConstants().store(stringconst);
		getnamespace(index);
	}
	public void getnamespace(int constindex) {
		instrx(GET_NAMESPACE, constindex);
		incrementMaxstackSize();
	}
	
	public void getglobal(String stringconst) {
		int index = getConstants().store(stringconst);
		getglobal(index);
	}
	
	public void setglobal(String stringconst) {
		int index = getConstants().store(stringconst);
		setglobal(index);
	}
	
	public void getglobal(int constindex) {
		instrx(GET_GLOBAL, constindex);
		incrementMaxstackSize();
	}
	
	public void setglobal(int constindex) {
		instrx(SET_GLOBAL, constindex);
		decrementMaxstackSize();
	}
	
   public void namespacedef() {
        instrx(NAMESPACE_DEF, getBytecodeIndex());
        incrementMaxstackSize();
        
        AsmEmitter asm = new AsmEmitter(this.symbols);
        asm.setDebug(this.isDebug());
        asm.start(ScopeType.OBJECT_SCOPE);      
                
        peek().asms.add(asm);
        this.inner.push(asm);
    }
	
	public void classdef(int numberOfInterfaces) {
		instrx(CLASS_DEF, numberOfInterfaces);
		incrementMaxstackSize(numberOfInterfaces);		
		
		AsmEmitter asm = new AsmEmitter(this.symbols);		
		asm.setDebug(this.isDebug());
		asm.start(ScopeType.OBJECT_SCOPE);
				
		peek().asms.add(asm);
		this.inner.push(asm);
	}
	
	public void gendef(int numberOfParameters, boolean isVarargs) {
		instrx(GEN_DEF, getBytecodeIndex());		
		incrementMaxstackSize(numberOfParameters);
		
		AsmEmitter asm = new AsmEmitter(this.symbols);
		asm.setDebug(this.isDebug());
		
		asm.numArgs = numberOfParameters;
		asm.isVarargs = isVarargs;
		asm.start(ScopeType.LOCAL_SCOPE);
				
		peek().asms.add(asm);
		this.inner.push(asm);
	}
	
	public void funcdef(int numberOfParameters, boolean isVarargs) {
		instrx(FUNC_DEF, getBytecodeIndex());		
		incrementMaxstackSize(numberOfParameters);
		
		AsmEmitter asm = new AsmEmitter(this.symbols);
		asm.setDebug(this.isDebug());
		
		asm.numArgs = numberOfParameters;	
		asm.isVarargs = isVarargs;
		asm.start(ScopeType.LOCAL_SCOPE);
				
		peek().asms.add(asm);
		this.inner.push(asm);
	}
	
	public int getBytecodeIndex() {
		return peek().asms.size();
	}
	
	public void isa() {
		instr(IS_A);
		decrementMaxstackSize();
	}
	
	public void ifeq(String label) {
		markLabel(IFEQ, label);
		decrementMaxstackSize();
	}
	public void ifeq(int offset) {
		instrsx(IFEQ, offset);
		decrementMaxstackSize();
	}
	
	public String ifeq() {
		String labelName = nextLabelName();
		ifeq(labelName);
		
		return labelName;
	}
	
	public void invoke(int numberOfArgs) {
		instr1(INVOKE, numberOfArgs);				
		decrementMaxstackSize(numberOfArgs);
	}
	public void invoke(int numberOfArgs, boolean onClause) {
		instr2(INVOKE, numberOfArgs, onClause ? 1 : 0);				
		decrementMaxstackSize(numberOfArgs);
	}

	public void throw_() {
		instr(THROW);
		incrementMaxstackSize();
	}
	
	public void initfinally() {
	    peek().hasBlocks = true;
		peek().blockSize.add(getInstructionCount());
		// this will be populated with the correct offset
		instrsx(INIT_FINALLY, 0); 
		
	}
	public void initfinally(int offset) {
	    peek().hasBlocks = true;
	    instrsx(INIT_FINALLY, offset);
	}
	public void initfinally(String label) {
        peek().hasBlocks = true;        
        markLabel(INIT_FINALLY, label);
    }
	
	public void initon() {
	    peek().hasBlocks = true;
	    peek().blockSize.add(getInstructionCount());
		// this will be populated with the correct offset
		instrsx(INIT_ON, 0); 		
	}
	
	public void initon(int offset) {
	    peek().hasBlocks = true;
        instrsx(INIT_ON, offset); 
	}
	public void initon(String label) {
        peek().hasBlocks = true;        
        markLabel(INIT_ON, label);
    }
	
	public void endfinally() {				
		instr(END_FINALLY);
	}
	public void markendfinally() {
		int startPC = peek().blockSize.pop();
		getInstructions().set(startPC, SET_ARGsx(INIT_FINALLY, getInstructionCount()));
		instr(END_BLOCK);
	}
	
	public void markendon() {
		int startPC = peek().blockSize.pop();
		getInstructions().set(startPC, SET_ARGsx(INIT_ON, getInstructionCount()));
		instr(END_BLOCK);
	}
	
	public void endon() {			
		instr(END_ON);
	}
	
	public void endblock() {
		instr(END_BLOCK);
	}
	
	
		
	/* arithmetic operators */
	public void add() {
		instr(ADD);
		decrementMaxstackSize();
	}
	public void sub() {
		instr(SUB);
		decrementMaxstackSize();
	}
	public void mul() {
		instr(MUL);
		decrementMaxstackSize();
	}
	public void div() {
		instr(DIV);
		decrementMaxstackSize();
	}
	public void mod() {
		instr(MOD);
		decrementMaxstackSize();
	}
	public void neg() {
		instr(NEG);
	}
	
//	BSL = 80,
	public void bsl() {
		instr(BSL);
		decrementMaxstackSize();
	}
//	BSR = 81,
	public void bsr() {
		instr(BSR);
		decrementMaxstackSize();
	}
//	BNOT = 82,
	public void bnot() {
		instr(BNOT);
		decrementMaxstackSize();
	}
//	XOR = 83,
	public void xor() {
		instr(XOR);
		decrementMaxstackSize();
	}
//	LOR = 84,
	public void lor() {
		instr(LOR);
		decrementMaxstackSize();
	}
//	LAND = 85,
	public void land() {
		instr(LAND);
		decrementMaxstackSize();
	}
	
	public void or() {
		instr(OR);
		decrementMaxstackSize();
	}
	public void and() {
		instr(AND);
		decrementMaxstackSize();
	}
	public void not() {
		instr(NOT);
	}
	
	public void req() {
		instr(REQ);
		decrementMaxstackSize();
	}
	
	public void eq() {
		instr(EQ);
		decrementMaxstackSize();
	}
	public void neq() {
		instr(NEQ);
		decrementMaxstackSize();
	}
	public void gt() {
		instr(GT);
		decrementMaxstackSize();
	}
	public void gte() {
		instr(GTE);
		decrementMaxstackSize();
	}
	public void lt() {
		instr(LT);
		decrementMaxstackSize();
	}
	public void lte() {
		instr(LTE);
		decrementMaxstackSize();
	}

	
	/**
	 * Compiles the assembler into bytecode.
	 * 
	 * @return
	 * @throws LeolaRuntimeException
	 */
	public Bytecode compile() throws LeolaRuntimeException {

		int [] code = this.instructions.truncate();
		Bytecode bytecode = new Bytecode(code);
		
		//BytecodeOptimizer.optimize(bytecode);
		
		bytecode.numInners = this.asms.size();
		bytecode.inner = new Bytecode[bytecode.numInners];
				
		bytecode.numArgs = this.numArgs;
		if(this.isVarargs) {
		    bytecode.setVarargs();
		}
		
		if(this.hasBlocks) {
		    bytecode.setBlocks();
		}		
		
		if(this.hasParamIndexes) {
		    bytecode.setParamIndexes();
		}
		
		if(this.localScope.hasOuters()) {
			Outers outers = this.localScope.getOuters();
			bytecode.numOuters = outers.getNumberOfOuters();
		}
		
		
		bytecode.paramNames = new LeoString[this.numArgs];
		if(this.localScope.hasLocals()) {
			Locals locals = this.localScope.getLocals();
			bytecode.numLocals = locals.getNumberOfLocals();
			
			if(bytecode.numArgs > 0) {
	            for(int i = 0; i < bytecode.numArgs; i++) {
	                String ref = locals.getReference(i);
	                if(ref != null) {
	                    bytecode.paramNames[i] = LeoString.valueOf(ref);
	                }
	            }
	        }
		}
		
		/* we only care about this for classes */
		if ( isDebug() ) {
		    bytecode.setDebug();
			bytecode.debugSymbols = this.debugSymbols;
		}
										
		if ( this.localScope.hasConstants() ) {
			Constants constants = this.localScope.getConstants();
			bytecode.constants = constants.compile();
			bytecode.numConstants = constants.getNumberOfConstants();
		}
		else {
			bytecode.numConstants = 0;
			bytecode.constants = ArrayUtil.EMPTY_LEOOBJECTS;
		}
		
		int stacksize = bytecode.numArgs + bytecode.numLocals + this.localScope.getMaxstacksize(); 		
		stacksize += bytecode.numConstants;				
		stacksize += bytecode.numOuters;		
				
		bytecode.maxstacksize = stacksize;		
		
		for(int i = 0; i < bytecode.inner.length; i++) {
			bytecode.inner[i] = this.asms.get(i).compile();
		}
		
		// removes all of the compiler specific data
		// from the scope
		this.localScope.compiled();
		
		return bytecode;		
	}
}

