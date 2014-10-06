/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.vm.asm;

import static leola.vm.Opcodes.END_BLOCK;
import static leola.vm.Opcodes.ADD;
import static leola.vm.Opcodes.AND;
import static leola.vm.Opcodes.BNOT;
import static leola.vm.Opcodes.BSL;
import static leola.vm.Opcodes.BSR;
import static leola.vm.Opcodes.CLASS_DEF;
import static leola.vm.Opcodes.DEF;
import static leola.vm.Opcodes.DIV;
import static leola.vm.Opcodes.DUP;
import static leola.vm.Opcodes.END_FINALLY;
import static leola.vm.Opcodes.END_ON;
import static leola.vm.Opcodes.EQ;
import static leola.vm.Opcodes.GEN;
import static leola.vm.Opcodes.GET;
import static leola.vm.Opcodes.GET_GLOBAL;
import static leola.vm.Opcodes.GET_NAMESPACE;
import static leola.vm.Opcodes.GT;
import static leola.vm.Opcodes.GTE;
import static leola.vm.Opcodes.IF;
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
import static leola.vm.Opcodes.NEW;
import static leola.vm.Opcodes.NEW_ARRAY;
import static leola.vm.Opcodes.NEW_MAP;
import static leola.vm.Opcodes.NEW_NAMESPACE;
import static leola.vm.Opcodes.NOT;
import static leola.vm.Opcodes.OPCODE;
import static leola.vm.Opcodes.OPPOP;
import static leola.vm.Opcodes.OR;
import static leola.vm.Opcodes.POP;
import static leola.vm.Opcodes.REQ;
import static leola.vm.Opcodes.RET;
import static leola.vm.Opcodes.SET;
import static leola.vm.Opcodes.SET_ARG1;
import static leola.vm.Opcodes.SET_ARG2;
import static leola.vm.Opcodes.SET_ARGx;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import leola.vm.asm.Scope.ScopeType;
import leola.vm.exceptions.LeolaRuntimeException;
import leola.vm.types.LeoDouble;
import leola.vm.types.LeoInteger;
import leola.vm.types.LeoLong;
import leola.vm.types.LeoObject;
import leola.vm.types.LeoString;
import leola.vm.util.ArrayUtil;

/**
 * The pseudo assembler language creator.  Easily build opcode codes via the assembler methods.
 * 
 * @author Tony
 *
 */
public class Asm {	
	private List<Integer> instructions;	
	private Map<String, Label> labels;
	private Stack<Asm> inner;
	private List<Asm> asms;

	private Stack<Integer> lexicalScopes;	
	
	private int labelIndex;
	
	private Symbols symbols;
	private Scope localScope;
	
	private DebugSymbols debugSymbols;
	
	private boolean uselocal;
	private boolean debug;
	
	private int currentLineNumber;
	private int numArgs;
	private boolean isVarargs;
	
	private Stack<Integer> blockSize;
	
	/**
	 */
	public Asm(Symbols symbols) {
		this.symbols = symbols;
		this.uselocal = false;
		this.isVarargs = false;
		
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
	public boolean useLocals() {
		Asm peek = peek();
		return peek.uselocal || !peek.lexicalScopes.isEmpty();
	}

	/**
	 * Mark the beginning of an inner scope
	 */
	public void markLexicalScope() {
		int index = getLocals().getIndex();
		peek().lexicalScopes.push(index);
		
		if(isDebug()) {
			peek().debugSymbols.startScope(getInstructionSize());
		}
	}
	
	/**
	 * Leave the scope
	 */
	public void unmarkLexicalScope() {
		if(peek().lexicalScopes.isEmpty()) {
			throw new LeolaRuntimeException("Illegal lexical scope");
		}
		int index = peek().lexicalScopes.pop();
		int currentIndex = getLocals().getIndex();
		if(currentIndex != index) {			
			getLocals().setIndex(index);				
		}
		
		if(isDebug()) {
			peek().debugSymbols.endScope(getInstructionSize());
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
	
	
	public void start(ScopeType scopeType) {		
		this.localScope = scopeType == ScopeType.GLOBAL_SCOPE ? 
							  this.symbols.getGlobalScope()
							: this.symbols.pushScope(scopeType); 

		this.uselocal = scopeType == ScopeType.LOCAL_SCOPE;
		
		this.instructions = new ArrayList<Integer>();		
		this.labels = new HashMap<String, Label>();
		
		this.labelIndex = 0;
		
		this.inner = new Stack<Asm>();
		this.asms = new ArrayList<Asm>();
		
		
		this.inner.push(this);
		incrementMaxstackSize(2);
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
	 * Ends a block of code
	 */
	public Asm end() {	
		
		/* reconcile the labels */
		for(Label label : getLabels().values()) {
			for(long l : label.getDeltas()) {
				int instrIndex = (int)(l >> 32);
				int opcode = (int)((l << 32) >> 32);
				int delta = label.getLabelInstructionIndex() - instrIndex - 1;
				int instr = SET_ARGx(opcode,  delta);	
				
				getInstructions().set(instrIndex, instr);
			}
		}
		
		/* reconcile any Outers */
		Asm thisAsm = peek();
		Outers outers = thisAsm.getOuters();
				
		for(int i = 0; i < outers.getNumberOfOuters(); i++) {
			OuterDesc outer = outers.get(i);
			if ( outer.getUp() > 0 ) {
				int s = 0;
				Scope scope = thisAsm.localScope;
				if(scope!=null && s < outer.getUp()) {
					
					scope = scope.getParent();
					Asm asm = findAsmByScope(scope);
					if(asm!=null) {												
						int nup = outer.getUp()-1;
						
						if(nup>0) {
							int store = asm.localScope.getOuters().store(new OuterDesc(outer.getIndex(), nup));
							asm.linstrx(xLOAD_OUTER, store);
						}
						else {
							asm.linstrx(xLOAD_LOCAL, outer.getIndex());
						}
					}
					
					s++;
				}				
			}
		}
		
		this.symbols.popScope();
		
		Asm asm = this;
		if ( !this.inner.isEmpty() ) {
			asm = this.inner.pop();
		}
		
		return asm;
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
	
	public void allocateLocals(int numberOfLocals) {
		getLocals().allocate(numberOfLocals);
	}
	
//	private Asm getParent() {
//		return this.inner.size() > 1 ? this.inner.elementAt(this.inner.size()-2) : null;
//	}
	
	private Asm findAsmByScope(Scope scope) {
		for(int i = 0; i < this.inner.size(); i++ ) {
			Asm asm = this.inner.get(i);
			if ( asm.localScope == scope ) {
				return asm;
			}
		}
		
		return null;
	}
	
	public void storeAndloadconst(String reference) {
		storeAndloadconst(LeoString.valueOf(reference));
	}
	
	public void storeAndloadconst(LeoObject obj) {
		Constants constants = getConstants();
		int index = constants.store(obj);
		loadconst(index);
	}
	
	public void storeAndloadconst(int i) {
		storeAndloadconst(LeoInteger.valueOf(i));
	}
	
	public void storeAndloadconst(double i) {
		storeAndloadconst(LeoDouble.valueOf(i));
	}
	
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
	 * @return
	 */
	public int addLocal(String reference) {
		if(isDebug()) {
			peek().debugSymbols.store(reference, getInstructionSize());
		}
		
		Locals locals = getLocals();
		return locals.store(reference);
	}
	
	/**
	 * Stores a variable, either as a local, scoped or an {@link Outer}
	 * 
	 * @param ref
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
	public int getInstructionSize() {
		return getInstructions().size();
	}
	
	/**
	 * @return the labels
	 */
	public Map<String, Label> getLabels() {
		return this.inner.peek().labels;
	}
	
	/**
	 * @return the instructions
	 */
	public List<Integer> getInstructions() {
//		return peek().inner.peek().instructions;
		return peek().instructions;
	}
	
	public Asm peek() {
		return this.inner.peek();
	}
	
	/**
	 * @return the current instruction
	 */
	private int peekInstr() {
		List<Integer> instrs = peek().instructions;
		return instrs.get(instrs.size()-1);
	}
	
	private void setInstr(int instr) {
		List<Integer> instrs = peek().instructions;
		instrs.set(instrs.size()-1, instr);
	}
	
	private void linstrx(int opcode, int argx) {
		this.instructions.add(SET_ARGx(opcode, argx));
	}
	
	/**
	 * Outputs an instruction with no arguments
	 * @param opcode
	 */
	private void instr(int opcode) {
		this.inner.peek().instructions.add(opcode);
	}
	
	/**
	 * Outputs an instruction with 1 argument
	 * @param opcode
	 * @param arg1
	 */
	private void instrx(int opcode, int arg1) {
		instr(SET_ARGx(opcode, arg1));
	}
	
	private void instr1(int opcode, int arg1) {
		instr(SET_ARG1(opcode, arg1));
	}
	
	/**
	 * Outputs an instruction with 2 arguments
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
		if ( ! getLabels().containsKey(label) ) {
			getLabels().put(label, new Label(this));
		}
		
		instr(opcode); // will eventually be replaced
		
		Label l = getLabels().get(label);
		l.mark(opcode);
		
	}
	
	/**
	 * Creates a label
	 * 
	 * @param name
	 */
	public void label(String name) {
		if ( ! getLabels().containsKey(name) ) {
			getLabels().put(name, new Label(this));
		}
		Label l = getLabels().get(name);
		l.set();		
	}
	public String label() {
		String labelName = nextLabelName();
		label(labelName);
		
		return labelName;
	}
	
	public String nextLabelName() {
		String labelName = ":" + this.labelIndex++;
		return labelName;
	}
	
	/*================================================================================
	 * The Assembler
	 *================================================================================*/
	 
	
	public void line(int line) {
		if ( this.isDebug() ) {
			if ( line != this.currentLineNumber && line != 0 
				&& (getInstructionSize() > 0 && OPCODE(peekInstr()) != LINE )
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
		List<Integer> instructions = peek().instructions;
		int numberOfInstrs = instructions.size();
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
		String label = ":" + this.labelIndex++;
		jmp(label);
		return label;
	}
	
	public void jmp(int offset) {
		instrx(JMP, offset);
	}
	
	public void brk(String label) {
		jmp(label);
	}
	
	public void cont(String label) {
		jmp(label);
	}
	
	public void newobj(int nargs) {
		instrx(NEW, nargs);
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
		
	public void newnamespace() {
		instrx(NEW_NAMESPACE, getBytecodeIndex());
		incrementMaxstackSize();
		
		Asm asm = new Asm(this.symbols);
		asm.setDebug(this.isDebug());
		asm.start(ScopeType.OBJECT_SCOPE);		
				
		peek().asms.add(asm);
		this.inner.push(asm);
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
	
	public void classdef(int numberOfInterfaces) {
		instrx(CLASS_DEF, numberOfInterfaces);
		incrementMaxstackSize(numberOfInterfaces);		
		
		Asm asm = new Asm(this.symbols);		
		asm.setDebug(this.isDebug());
		asm.start(ScopeType.OBJECT_SCOPE);
				
		peek().asms.add(asm);
		this.inner.push(asm);
	}
	
	public void gen(int numberOfParameters, boolean isVarargs) {
		instrx(GEN, getBytecodeIndex());		
		incrementMaxstackSize(numberOfParameters);
		
		Asm asm = new Asm(this.symbols);
		asm.setDebug(this.isDebug());
		
		asm.numArgs = numberOfParameters;
		asm.isVarargs = isVarargs;
		asm.start(ScopeType.LOCAL_SCOPE);
				
		peek().asms.add(asm);
		this.inner.push(asm);
	}
	
	public void def(int numberOfParameters, boolean isVarargs) {
		instrx(DEF, getBytecodeIndex());		
		incrementMaxstackSize(numberOfParameters);
		
		Asm asm = new Asm(this.symbols);
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
		markLabel(IF, label);
		decrementMaxstackSize();
	}
	public void ifeq(int offset) {
		instrx(IF, offset);
		decrementMaxstackSize();
	}
	
	public String ifeq() {
		String labelName = ":" + this.labelIndex++;
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
		this.blockSize.add(getInstructionSize());
		// this will be populated with the correct offset
		instrx(INIT_FINALLY, 0); 
		
	}
	public void initon() {
		this.blockSize.add(getInstructionSize());
		// this will be populated with the correct offset
		instrx(INIT_ON, 0); 		
	}
	
	public void endfinally() {				
		instr(END_FINALLY);
	}
	public void markendfinally() {
		int startPC = this.blockSize.pop();
		getInstructions().set(startPC, SET_ARGx(INIT_FINALLY, getInstructionSize()));
		instr(END_BLOCK);
	}
	
	public void markendon() {
		int startPC = this.blockSize.pop();
		getInstructions().set(startPC, SET_ARGx(INIT_ON, getInstructionSize()));
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

		int [] code = toArray(this.instructions);
		Bytecode bytecode = new Bytecode(code);
		
		//BytecodeOptimizer.optimize(bytecode);
		
		bytecode.numInners = this.asms.size();
		bytecode.inner = new Bytecode[bytecode.numInners];
				
		bytecode.numArgs = this.numArgs;
		bytecode.isVarargs = this.isVarargs;
				
		if(this.localScope.hasOuters()) {
			Outers outers = this.localScope.getOuters();
			bytecode.numOuters = outers.getNumberOfOuters();
		}
		
		if(this.localScope.hasLocals()) {
			Locals locals = this.localScope.getLocals();
			bytecode.numLocals = locals.getNumberOfLocals();
		}
		
		/* we only care about this for classes */
		bytecode.debug =  (byte) (this.isDebug() ? 1 : 0);
		if ( bytecode.debug > 0 ) {
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

	/**
	 * To primitive array
	 * @param instr
	 * @return
	 */
	private int[] toArray(List<Integer> instr) {
		final int len = instr.size();
		final int[] code = new int[len];
		for(int i = 0; i < len; i++) {
			code[i] = instr.get(i);
		}
		return code;
	}
}

