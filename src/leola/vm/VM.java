/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.vm;

import static leola.vm.Opcodes.ADD;
import static leola.vm.Opcodes.AND;
import static leola.vm.Opcodes.ARG1;
import static leola.vm.Opcodes.ARG2;
import static leola.vm.Opcodes.ARGx;
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
import static leola.vm.Opcodes.OPPOP;
import static leola.vm.Opcodes.OR;
import static leola.vm.Opcodes.POP;
import static leola.vm.Opcodes.REQ;
import static leola.vm.Opcodes.RET;
import static leola.vm.Opcodes.SET;
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
import static leola.vm.Opcodes.END_BLOCK;

import java.util.Stack;

import leola.vm.asm.Bytecode;
import leola.vm.asm.Outer;
import leola.vm.asm.Scope;
import leola.vm.asm.Scope.ScopeType;
import leola.vm.asm.Symbols;
import leola.vm.debug.DebugEvent;
import leola.vm.debug.DebugListener;
import leola.vm.exceptions.LeolaRuntimeException;
import leola.vm.types.LeoArray;
import leola.vm.types.LeoBoolean;
import leola.vm.types.LeoError;
import leola.vm.types.LeoFunction;
import leola.vm.types.LeoGenerator;
import leola.vm.types.LeoMap;
import leola.vm.types.LeoNamespace;
import leola.vm.types.LeoNull;
import leola.vm.types.LeoObject;
import leola.vm.types.LeoScopedObject;
import leola.vm.types.LeoString;
import leola.vm.util.ClassUtil;


/**
 * The Leola Virtual Machine executes the Leola bytecode.
 *
 * @author Tony
 *
 */
public class VM {

	/**
	 * Maximum stack size
	 */
	public static final int DEFAULT_MAX_STACKSIZE = 1024 * 1024;

	/**
	 * Runtime
	 */
	private Leola runtime;
	private Symbols symbols;

	/*thread stack
	 */
	private LeoObject[] stack;

	/* list of open outers, if this function goes out of scope (i.e., the stack) then the outers
	 * are closed (i.e., the value contained on the stack is transferred used instead of the indexed value
	 */
	private Outer[] openouters;
	private int top;

//	private int[] blockStack;
	
	/**
	 * @param runtime the {@link Leola} runtime
	 */
	public VM(Leola runtime) {
		this.runtime = runtime;
		this.symbols = runtime.getSymbols();

		int stackSize = runtime.getArgs().getStackSize();
		stackSize = (stackSize <= 0) ? DEFAULT_MAX_STACKSIZE : stackSize;

		this.stack = new LeoObject[stackSize];
		this.openouters = new Outer[stackSize];
		this.top = 0;		
	}


	/**
	 * Executes the {@link Bytecode}.
	 *
	 * @param code
	 * @return
	 * @throws LeolaRuntimeException
	 */
	public LeoObject execute(LeoObject env, LeoObject callee, Bytecode code) throws LeolaRuntimeException {		
		return execute(env, callee, code, (LeoObject[])null);
	}

	/**
	 * Executes the {@link Bytecode}.
	 *
	 * @param scope
	 * @param code
	 * @param args - arguments to the function
	 * @return
	 * @throws LeolaRuntimeException
	 */
	public LeoObject execute(LeoObject env, LeoObject callee, Bytecode code, LeoObject[] args) throws LeolaRuntimeException {
//		LeoObject[] stack = new LeoObject[code.maxstacksize];
		final int base = top;
		prepareStack(code);
		
		if ( args != null ) {
			System.arraycopy(args, 0, stack, base, args.length);
		}

		LeoObject result = executeStackframe(env, code, stack, callee, base );
		return result;
	}

	/**
	 * Executes the {@link Bytecode}.
	 *
	 * @param callee
	 * @param code
	 * @param arg1
	 * @return
	 * @throws LeolaRuntimeException
	 */
	public LeoObject execute(LeoObject env, LeoObject callee, Bytecode code, LeoObject arg1) throws LeolaRuntimeException {
//		LeoObject[] stack = new LeoObject[code.maxstacksize];
		final int base = top;
		prepareStack(code);
		
		stack[base + 0] = arg1;		
		
		LeoObject result = executeStackframe(env, code, stack, callee, base );
		return result;
	}

	/**
	 * Executes the {@link Bytecode}.
	 *
	 * @param callee
	 * @param code
	 * @param arg1
	 * @param arg2
	 * @return
	 * @throws LeolaRuntimeException
	 */
	public LeoObject execute(LeoObject env, LeoObject callee, Bytecode code, LeoObject arg1, LeoObject arg2) throws LeolaRuntimeException {
		//LeoObject[] stack = new LeoObject[code.maxstacksize];
		final int base = top;
		prepareStack(code);
		
		stack[base + 0] = arg1;
		stack[base + 1] = arg2;

		LeoObject result = executeStackframe(env, code, stack, callee, base );
		return result;
	}

	/**
	 * Executes the {@link Bytecode}.
	 *
	 * @param callee
	 * @param code
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @return
	 * @throws LeolaRuntimeException
	 */
	public LeoObject execute(LeoObject env, LeoObject callee, Bytecode code, LeoObject arg1, LeoObject arg2, LeoObject arg3) throws LeolaRuntimeException {
		//LeoObject[] stack = new LeoObject[code.maxstacksize];
		final int base = top;
		prepareStack(code);
		
		stack[base + 0] = arg1;
		stack[base + 1] = arg2;
		stack[base + 2] = arg3;

		LeoObject result = executeStackframe(env, code, stack, callee, base );
		return result;
	}

	/**
	 * Executes the {@link Bytecode}.
	 *
	 * @param callee
	 * @param code
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @param arg4
	 * @return
	 * @throws LeolaRuntimeException
	 */
	public LeoObject execute(LeoObject env, LeoObject callee, Bytecode code, LeoObject arg1, LeoObject arg2, LeoObject arg3, LeoObject arg4) throws LeolaRuntimeException {
		//LeoObject[] stack = new LeoObject[code.maxstacksize];
		final int base = top;
		prepareStack(code);
		
		stack[base + 0] = arg1;
		stack[base + 1] = arg2;
		stack[base + 2] = arg3;
		stack[base + 3] = arg4;

		LeoObject result = executeStackframe(env, code, stack, callee, base );
		return result;
	}

	/**
	 * Executes the {@link Bytecode}.
	 *
	 * @param callee
	 * @param code
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @param arg4
	 * @param arg5
	 * @return
	 * @throws LeolaRuntimeException
	 */
	public LeoObject execute(LeoObject env, LeoObject callee, Bytecode code, LeoObject arg1, LeoObject arg2, LeoObject arg3, LeoObject arg4, LeoObject arg5) throws LeolaRuntimeException {

		//LeoObject[] stack = new LeoObject[code.maxstacksize];
		final int base = top;
		prepareStack(code);		
		
		stack[base + 0] = arg1;
		stack[base + 1] = arg2;
		stack[base + 2] = arg3;
		stack[base + 3] = arg4;
		stack[base + 4] = arg5;

		LeoObject result = executeStackframe(env, code, stack, callee, base );
		return result;
	}
	
	/**
	 * Prepares the stack by assigning NULL to all of the bytecode's
	 * arguments.
	 * 
	 * @param code
	 */
	private void prepareStack(Bytecode code) {
		final int base = top;
		for(int i = 0; i < code.numArgs; i++) {
			stack[base + i] = LeoNull.LEONULL;
		}
	}



	/**
	 * Executes the {@link Bytecode}
	 *
	 * @param code
	 * @param frame
	 * @throws LeolaRuntimeException
	 */
	private LeoObject executeStackframe(LeoObject env, Bytecode code, LeoObject[] stack, LeoObject callee, int base) throws LeolaRuntimeException {
//		if(code.maxstacksize > stack.length-base) {
//			throw new LeolaRuntimeException("VM stack overflow.");
//		}

		LeoObject result = LeoNull.LEONULL;

		final int[] instr = code.instr;
		final int len = code.len;
		int pc = code.pc;


		final LeoObject[] constants = code.constants;
		final Bytecode[] inner = code.inner;
		
		final Outer[] calleeouters;
		final LeoObject[] genLocals;
		
		if(callee != null) {
			calleeouters = callee.getOuters();
			genLocals = callee.getLocals();
			
			/* if this is a generator, let us copy its local variables onto
			 * the stack
			 */
			if(genLocals != null) {
				System.arraycopy(genLocals, code.numArgs, stack, base+code.numArgs, code.numLocals-code.numArgs);
			}
		}
		else {
			calleeouters = null;
			genLocals = null;
		}
		

		boolean closeOuters = false;
		boolean yield = false;

		Scope scope = null;
		LeoScopedObject scopedObj = null;
		if ( env instanceof LeoScopedObject) {
			scopedObj = (LeoScopedObject)env;
			scope = scopedObj.getScope();
		}


		if(scope==null) {
			LeoNamespace global = runtime.getGlobalNamespace();

			scope=global.getScope();
			scopedObj=global;
		}
		

		Stack<Integer> blockStack = new Stack<Integer>();
		
		final int topStack = base + code.numLocals;
		top = topStack;

		int lineNumber = -1;
		do {			
			try {										
				while( pc < len ) {
					int i = instr[pc++];
					int opcode =  i & 255; //OPCODE(i);
	
					switch(opcode) {
						/* Debug */
						case LINE: {
							lineNumber = ARGx(i);
							DebugListener listener = this.runtime.getDebugListener();
							if(listener != null ) {
								LeoObject[] stackSnapshot = new LeoObject[top-base];
								System.arraycopy(stack, base, stackSnapshot, 0, stackSnapshot.length);
	
								LeoObject[] localsSnapshot = new LeoObject[topStack-base];
								System.arraycopy(stack, base, localsSnapshot, 0, localsSnapshot.length);
	
								listener.onLineNumber(new DebugEvent(stack, base, topStack, top, pc
																   , lineNumber, scope, calleeouters, code));
							}
							continue;
						}
	
						/* Store operations */
						case LOAD_CONST: {
							int iname = ARGx(i);
							stack[top++] = constants[iname];
							continue;
						}
						case LOAD_LOCAL: {
							int iname = ARGx(i);
							stack[top++] = stack[base + iname];
							continue;
						}
						case LOAD_OUTER: {
							int iname = ARGx(i);
							stack[top++] = calleeouters[iname].getValue();
							continue;
						}
						case LOAD_NULL: {
							stack[top++] = LeoNull.LEONULL;
							continue;
						}
						case LOAD_TRUE: {
							stack[top++] = LeoBoolean.LEOTRUE;
							continue;
						}
						case LOAD_FALSE: {
							stack[top++] = LeoBoolean.LEOFALSE;
							continue;
						}
						case STORE_LOCAL: {
							int iname = ARGx(i);
							stack[base + iname] = stack[--top];
							continue;
						}
						case STORE_OUTER: {
							int iname = ARGx(i);
							calleeouters[iname].setValue(stack[--top]);
							continue;
						}
	
						/* stack operators */
						case SHIFT:	{
							int n = ARGx(i);
	
							LeoObject t = stack[top-1];
							for(int j = 1; j < n; j++) {
								stack[top-j] = stack[top-j-1];
							}
							stack[top-n] = t;
	
							continue;
						}
						case POP:	{
							stack[top--] = null;
							continue;
						}
						case OPPOP:	{
							if (top>topStack) {
								stack[top--] = null;
							}
							continue;
						}
						case DUP: {
							LeoObject obj = stack[top-1];
							stack[top++] = obj;
							continue;
						}
						case RET:	{
							pc = len;  /* Break out of the bytecode */
							if ( top>topStack) {
								result = stack[--top];
							}
							break;
						}
						case YIELD: {
							yield = true; /* lets not expire the generator */
							
							/* copy what was stored on the stack, back to the
							 * generators local copy
							 */
							System.arraycopy(stack, base+code.numArgs, genLocals, code.numArgs, code.numLocals-code.numArgs);
							
							code.pc = pc;						
							pc = len;
							
							if ( top>topStack) {
								result = stack[--top];
							}
							break;
						}
						case MOV: {
							LeoObject t = stack[top-2];
							stack[top-2] = stack[top-1];
							stack[top-1] = t;
							continue;
						}
						case SWAP: {
							int n = ARGx(i);
							for(int j = 1; j <= n; j++) {
								LeoObject t = stack[top-j];
								stack[top-j] 	= stack[top-n-j];
								stack[top-n-j] 	= t;
							}
							continue;
						}
						case MOVN: {
							int n = ARGx(i) + 1;
	
							LeoObject t = stack[top-n];
							for(int j = n-1; j > 0; j--) {
								stack[top-j-1] 	= stack[top-j];
							}
	
							stack[top-1] = t;
	
							continue;
						}
						case JMP:	{
							int pos = ARGx(i);
							pc += pos;
							continue;
						}
						case TAIL_CALL: {
							pc = 0;	/* return to the beginning of the function call, with the
									   stack persevered */
	
							int nargs = ARG1(i);
							--top; /* pop the function object */
							/*LeoObject obj = stack[--top];*/
							switch(nargs) {
								case 0: {
									break;
								}
								case 1: {
									LeoObject arg1 = stack[--top];
									stack[base + 0] = arg1;
									break;
								}
								case 2: {
									LeoObject arg2 = stack[--top];
									LeoObject arg1 = stack[--top];
									stack[base + 0] = arg1;
									stack[base + 1] = arg2;
									break;
								}
								case 3: {
									LeoObject arg3 = stack[--top];
									LeoObject arg2 = stack[--top];
									LeoObject arg1 = stack[--top];
									stack[base + 0] = arg1;
									stack[base + 1] = arg2;
									stack[base + 2] = arg3;
									break;
								}
								case 4: {
									LeoObject arg4 = stack[--top];
									LeoObject arg3 = stack[--top];
									LeoObject arg2 = stack[--top];
									LeoObject arg1 = stack[--top];
									stack[base + 0] = arg1;
									stack[base + 1] = arg2;
									stack[base + 2] = arg3;
									stack[base + 3] = arg4;
									break;
								}
								case 5: {
									LeoObject arg5 = stack[--top];
									LeoObject arg4 = stack[--top];
									LeoObject arg3 = stack[--top];
									LeoObject arg2 = stack[--top];
									LeoObject arg1 = stack[--top];
									stack[base + 0] = arg1;
									stack[base + 1] = arg2;
									stack[base + 2] = arg3;
									stack[base + 3] = arg4;
									stack[base + 4] = arg5;
									break;
								}
								default: {
									LeoObject args[] = new LeoObject[nargs];
									for(int j = nargs - 1; j >= 0; j--) {
										args[j] = stack[--top];
									}
									System.arraycopy(args, 0, stack, base, nargs);
								}
							}
	
	
							continue;
						}
						case INVOKE:	{
							int nargs = ARG1(i);
							LeoObject fun = stack[--top];
	
							LeoObject c = null;
	
							switch(nargs) {
								case 0: {
									c = fun.call(this);
									break;
								}
								case 1: {
									LeoObject arg1 = stack[--top];
									c = fun.call(this, arg1);
									break;
								}
								case 2: {
									LeoObject arg2 = stack[--top];
									LeoObject arg1 = stack[--top];
									c = fun.call(this, arg1, arg2);
									break;
								}
								case 3: {
									LeoObject arg3 = stack[--top];
									LeoObject arg2 = stack[--top];
									LeoObject arg1 = stack[--top];
									c = fun.call(this, arg1, arg2, arg3);
									break;
								}
								case 4: {
									LeoObject arg4 = stack[--top];
									LeoObject arg3 = stack[--top];
									LeoObject arg2 = stack[--top];
									LeoObject arg1 = stack[--top];
									c = fun.call(this, arg1, arg2, arg3, arg4);
									break;
								}
								case 5: {
									LeoObject arg5 = stack[--top];
									LeoObject arg4 = stack[--top];
									LeoObject arg3 = stack[--top];
									LeoObject arg2 = stack[--top];
									LeoObject arg1 = stack[--top];
									c = fun.call(this, arg1, arg2, arg3, arg4, arg5);
									break;
								}
								default: {
									LeoObject args[] = new LeoObject[nargs];
									for(int j = nargs - 1; j >= 0; j--) {
										args[j] = stack[--top];
									}
									c = fun.call(this, args);
								}
							}
	
							/* if this has an ON block afterwards, it catches the
							 * exception (if there is one)
							 */
							if( !(c instanceof LeoError) || ARG2(i) > 0 ) {
								stack[top++] = c;
							}
							else {
								result = c; 	/* throw this error */
								pc = len; 		/* exit out of this function */
								LeoError error = c.as();
								if(error.getLineNumber() < 1) {
									error.setLineNumber(lineNumber);
									error.setSourceFile(code.getSourceFile());
								}
								else {
									error.addStack(new LeoError("", lineNumber));
								}
							}
	
							continue;
						}
						case NEW:	{
	
							LeoObject className = stack[--top];
	
							int nargs = ARGx(i);
							LeoObject[] args = null;
							if ( nargs > 0 ) {
								args = new LeoObject[nargs];
								for(int j = nargs - 1; j >= 0; j--) {
									args[j] = stack[--top];
								}
							}
	
							LeoObject instance = null;
	
							ClassDefinitions defs = symbols.lookupClassDefinitions(className);
							if ( defs == null ) {
	
								if(!runtime.isSandboxed()) {															
									instance = ClassUtil.newNativeInstance(className.toString(), args);
								}
								else {
									throw new LeolaRuntimeException("Unable to instantiate native Java classes in Sandboxed mode: " + className.toString());
								}
							}
							else {
								instance = defs.newInstance(runtime, LeoString.valueOf(symbols.getClassName(className.toString())), args);
							}
	
							stack[top++] = instance;
							continue;
						}
						case NEW_ARRAY:	{
							int initialSize = ARGx(i);
							LeoArray array = new LeoArray(initialSize);
	
							for(int j = initialSize; j > 0; j--) {
								array.add(stack[top-j]);
							}
							top -= initialSize;
	
							stack[top++] = array;
							continue;
						}
						case NEW_MAP:	{
							int initialSize = ARGx(i);
	
							LeoMap map = new LeoMap(initialSize);
							for(int j = 0; j < initialSize; j++) {
								LeoObject value = stack[--top];
								LeoObject key = stack[--top];
	
								map.put(key, value);
							}
	
							stack[top++] = map;
							continue;
						}
						case NEW_NAMESPACE: {
							int innerIndex = ARGx(i);
							Bytecode namespacecode = inner[innerIndex];
	
							String name = stack[--top].toString();
							NamespaceDefinitions ndefs = scope.getNamespaceDefinitions();
							LeoNamespace ns = ndefs.getNamespace(name);
							if(ns==null) {
								ns = new LeoNamespace(this.runtime, namespacecode, new Scope(this.symbols, scope, ScopeType.OBJECT_SCOPE), name);
								ndefs.storeNamespace(name, ns);
							}
							else {
								if(namespacecode.numOuters>0) {
									ns.setOuters(new Outer[namespacecode.numOuters]);
								}
							}
	
							closeOuters = outers(ns.getOuters(), calleeouters, openouters, stack, namespacecode.numOuters, base, pc, code, scope, lineNumber, top, topStack)
											|| closeOuters;
							pc += namespacecode.numOuters;
	
							this.runtime.execute(ns, namespacecode);
	
							stack[top++] = ns;
							continue;
						}
						case GEN: {
							int innerIndex = ARGx(i);
							Bytecode bytecode = inner[innerIndex];
							LeoGenerator fun = new LeoGenerator(scopedObj, bytecode.clone());
	
							Outer[] outers = fun.getOuters();
							closeOuters = outers(outers, calleeouters, openouters, stack, bytecode.numOuters, base, pc, code, scope, lineNumber, top, topStack)
											|| closeOuters;
							pc += bytecode.numOuters;
	
							stack[top++] = fun;
							continue;
						}
						case DEF: {
							int innerIndex = ARGx(i);
							Bytecode bytecode = inner[innerIndex];
							LeoFunction fun = new LeoFunction(scopedObj, bytecode);
	
							Outer[] outers = fun.getOuters();
							closeOuters = outers(outers, calleeouters, openouters, stack, bytecode.numOuters, base, pc, code, scope, lineNumber, top, topStack)
											|| closeOuters;
							pc += bytecode.numOuters;
	
							stack[top++] = fun;
							continue;
						}
						case CLASS_DEF: {
	
							LeoObject bytecodeIndex = stack[--top];
							Bytecode body = inner[bytecodeIndex.asInt()];
	
							LeoObject[] superParams = null;
							int numSuperParams = stack[--top].asInt();
							if( numSuperParams> 0 ) {
								superParams = new LeoObject[numSuperParams];
								for(int j = numSuperParams-1; j >= 0; j--) {
									superParams[j] = stack[--top];
								}
							}
	
	
							/* Defines the class signature */
							int nparams = stack[--top].asInt();
							LeoString[] paramNames = null;
							if ( nparams > 0 ) {
								paramNames = new LeoString[nparams];
								for(int j = nparams-1; j >= 0; j--) {
									paramNames[j] = stack[--top].toLeoString();
								}
							}
	
	
							LeoString[] interfaces = null;
							int numberOfInterfaces = ARGx(i);
							if ( numberOfInterfaces > 0 ) {
								interfaces = new LeoString[numberOfInterfaces];
								for(int j = 0; j < numberOfInterfaces; j++) {
									interfaces[j] = stack[--top].toLeoString();
								}
							}
	
							LeoObject superClassname = stack[--top];
							LeoString className = stack[--top].toLeoString();
	
	
							ClassDefinition superClassDefinition = null;
	
							if ( ! superClassname.$eq(LeoNull.LEONULL) ) {
								ClassDefinitions defs = symbols.lookupClassDefinitions(superClassname);
								superClassDefinition = defs.getDefinition(superClassname.toLeoString());
							}
	
							ClassDefinition classDefinition = new ClassDefinition(className
																			    , superClassDefinition
																			    , scope
																			    , interfaces
																			    , paramNames
																			    , superParams
																			    , body);
	
							ClassDefinitions defs = scope.getClassDefinitions();
							defs.storeClass(className, classDefinition);
	
							closeOuters = outers(classDefinition.getOuters(), calleeouters, openouters, stack, body.numOuters, base, pc, code, scope, lineNumber, top, topStack)
											|| closeOuters;
							pc += body.numOuters;
							continue;
						}
						case IS_A: {
							LeoObject obj = stack[--top];
							LeoObject type = stack[--top];
	
							stack[top++] = LeoBoolean.get(obj.isOfType(type.toString()));
	
							continue;
						}
						case IF:	{
							LeoObject cond = stack[--top];
							if ( ! LeoObject.isTrue(cond) ) {
								int pos = ARGx(i);
								pc += pos;
							}
							continue;
						}
						case THROW: {
							LeoObject str = stack[--top];
							result = new LeoError(str);
	
							stack[top++] = result;
							
							pc = len; /* exit out of this function */
							continue;
						}
	
						/* object access */
						case GET: {
							LeoObject index = stack[--top];
							LeoObject obj = stack[--top];
	
							LeoObject value = obj.getObject(index);
							stack[top++] = value;
	
							continue;
						}
						case SET: {
	
							LeoObject index = stack[--top];
							LeoObject obj = stack[--top];
							LeoObject value = stack[--top];
	
							obj.setObject(index, value);
							stack[top++] = obj; /* make this an expression */
							continue;
						}
						case GET_GLOBAL: {
							int iname = ARGx(i);
							LeoObject member = scope.getObject(constants[iname].toLeoString());
							stack[top++] = member;
	
							continue;
						}
						case SET_GLOBAL: {
							int iname = ARGx(i);
							scopedObj.addProperty(constants[iname].toLeoString(), stack[--top]);
	
							continue;
						}
						case GET_NAMESPACE: {
							int iname = ARGx(i);
							LeoObject member = scope.getNamespace(constants[iname].toLeoString());
							stack[top++] = member;
	
							continue;
						}
						case INIT_FINALLY: {
							blockStack.add(ARGx(i));
							continue;
						}
						case INIT_ON: {
							blockStack.add(ARGx(i));
							continue;
						}
						case END_ON: {
							blockStack.pop();
							continue;
						}
						case END_FINALLY: {
							//blockStack.pop();
							
							// TODO - rethrow Exception in Finally block
//							LeoObject error = stack[top-1];
//							if(error.isError()) {
//								pc = len;
//							}
							if(result.isError()) {
								pc = len;
							}
							
							continue;
						}
						case END_BLOCK: {
							blockStack.pop();
							continue;
						}
	
						/* arithmetic operators */
						case ADD:	{
							LeoObject r = stack[--top];
							LeoObject l = stack[--top];
							LeoObject c = l.$add(r);
							stack[top++] = c;
							continue;
						}
						case SUB:	{
							LeoObject r = stack[--top];
							LeoObject l = stack[--top];
							LeoObject c = l.$sub(r);
							stack[top++] = c;
							continue;
						}
						case MUL:	{
							LeoObject r = stack[--top];
							LeoObject l = stack[--top];
							LeoObject c = l.$mul(r);
							stack[top++] = c;
							continue;
						}
						case DIV:	{
							LeoObject r = stack[--top];
							LeoObject l = stack[--top];
							LeoObject c = l.$div(r);
							stack[top++] = c;
							continue;
						}
						case MOD:	{
							LeoObject r = stack[--top];
							LeoObject l = stack[--top];
							LeoObject c = l.$mod(r);
							stack[top++] = c;
							continue;
						}
						case NEG:	{
							LeoObject l = stack[--top];
							LeoObject c = l.$neg();
							stack[top++] = c;
							continue;
						}
						case BSL:	{
							LeoObject r = stack[--top];
							LeoObject l = stack[--top];
							LeoObject c = l.$bsl(r);
							stack[top++] = c;
							continue;
						}
						case BSR:	{
							LeoObject r = stack[--top];
							LeoObject l = stack[--top];
							LeoObject c = l.$bsr(r);
							stack[top++] = c;
							continue;
						}
						case BNOT:	{
							LeoObject l = stack[--top];
							LeoObject c = l.$bnot();
							stack[top++] = c;
							continue;
						}
						case XOR:	{
							LeoObject r = stack[--top];
							LeoObject l = stack[--top];
							LeoObject c = l.$xor(r);
							stack[top++] = c;
							continue;
						}
						case LOR:	{
							LeoObject r = stack[--top];
							LeoObject l = stack[--top];
							LeoObject c = l.$bor(r);
							stack[top++] = c;
							continue;
						}
						case LAND:	{
							LeoObject r = stack[--top];
							LeoObject l = stack[--top];
							LeoObject c = l.$band(r);
							stack[top++] = c;
							continue;
						}
	
						case OR:	{
							LeoObject r = stack[--top];
							LeoObject l = stack[--top];
							LeoObject c = LeoBoolean.get(l.isTrue() || r.isTrue());
							stack[top++] = c;
							continue;
						}
						case AND:	{
							LeoObject r = stack[--top];
							LeoObject l = stack[--top];
							LeoObject c = LeoBoolean.get(l.isTrue() && r.isTrue());
							stack[top++] = c;
							continue;
						}
						case NOT:	{
							LeoObject l = stack[--top];
							LeoObject c = LeoBoolean.get(!l.isTrue());
							stack[top++] = c;
							continue;
						}
	
						case REQ:	{
							LeoObject r = stack[--top];
							LeoObject l = stack[--top];
							LeoObject c = LeoBoolean.get(l.$req(r));
							stack[top++] = c;
							continue;
						}
						case EQ:	{
							LeoObject r = stack[--top];
							LeoObject l = stack[--top];
							LeoObject c = LeoBoolean.get(l.$eq(r));
							stack[top++] = c;
							continue;
						}
						case NEQ:	{
							LeoObject r = stack[--top];
							LeoObject l = stack[--top];
							LeoObject c = LeoBoolean.get(l.$neq(r));
							stack[top++] = c;
							continue;
						}
						case GT:	{
							LeoObject r = stack[--top];
							LeoObject l = stack[--top];
							LeoObject c = LeoBoolean.get(l.$gt(r));
							stack[top++] = c;
							continue;
						}
						case GTE:	{
							LeoObject r = stack[--top];
							LeoObject l = stack[--top];
							LeoObject c = LeoBoolean.get(l.$gte(r));
							stack[top++] = c;
							continue;
						}
						case LT:	{
							LeoObject r = stack[--top];
							LeoObject l = stack[--top];
							LeoObject c = LeoBoolean.get(l.$lt(r));
							stack[top++] = c;
							continue;
						}
						case LTE:	{
							LeoObject r = stack[--top];
							LeoObject l = stack[--top];
							LeoObject c = LeoBoolean.get(l.$lte(r));
							stack[top++] = c;
							continue;
						}
						default: {
							error(lineNumber, "Unknown bytecode: " + Integer.toHexString(i) + " Opcode: " + opcode);
						}
					}
				}
			}
			catch(LeolaRuntimeException e) {
				runtimeError(lineNumber, e);
			}
			catch(Exception e) {						
				error(lineNumber, e.toString());
			}
			finally {
				
				if(!blockStack.isEmpty()) {
					pc = blockStack.peek();
				}
				else {
				
					/* close the outers for this function call */
					//if ( closeOuters /*|| true*/ ) {
						for(int j=base;j<openouters.length && j<base+code.maxstacksize;j++) {
							if(openouters[j]!=null) {
								openouters[j].close();
								openouters[j] = null;
							}
		
							stack[j] = null;
						}
					//}
		
					top = base;
					
					/* expire this generator if we hit the end of the function */
					if(!yield && callee != null && callee.isGenerator()) {
						if(pc == len) {
							code.pc = pc;
						}
					}		
				}
			}
		} while(!blockStack.isEmpty());

		return result;
	}

	/**
	 * Handles an error in the execution.
	 *
	 * @param errorMsg
	 */
	private void error(int lineNumber, String errorMsg) {
		if(errorMsg==null) {
			errorMsg = "";
		}

		if ( lineNumber > -1) {
			throw new LeolaRuntimeException("Error on line: " + lineNumber + "\n>>>" + errorMsg);
		}

		throw new LeolaRuntimeException(errorMsg);
	}

	private void runtimeError(int lineNumber, LeolaRuntimeException error) {
		if ( lineNumber > -1) {
			throw new LeolaRuntimeException("Error on line: " + lineNumber + "\n\t>>>" + error.getMessage());
		}

		throw new LeolaRuntimeException("\n\t>>>" + error.getMessage());
	}

	private boolean outers(Outer[] outers, Outer[] calleeouters, Outer[] openouters, LeoObject[] stack, int numOuters, int base, int pc, Bytecode code, Scope scope, int lineNumber, int top, int topStack) {
		boolean closeOuters = false;
		for(int j = 0; j < numOuters; j++) {
			int i = code.instr[pc++];

			int opCode = i & 255;
			int index = ARGx(i);

			switch(opCode) {
				case xLOAD_OUTER: {
					outers[j] = calleeouters[index];
					break;
				}
				case xLOAD_LOCAL: {
					int bindex = base + index;
					outers[j] = openouters[bindex] != null ?
								openouters[bindex] :
								(openouters[bindex] = new Outer(stack, bindex));
					closeOuters = true;
					break;
				}
//				case xLOAD_SCOPE: {
//					outers[j] = new Outer(scope.getScopedValues(), index);
//					break;
//				}
				default: {
					error(lineNumber, "Invalid Opcode for Outer: " + opCode);
				}
			}
		}

		return closeOuters;
	}
}

