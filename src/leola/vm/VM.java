/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.vm;

import static leola.vm.Opcodes.ADD;
import static leola.vm.Opcodes.AND;
import static leola.vm.Opcodes.ARG1;
import static leola.vm.Opcodes.ARGsx;
import static leola.vm.Opcodes.ARGx;
import static leola.vm.Opcodes.BNOT;
import static leola.vm.Opcodes.BSL;
import static leola.vm.Opcodes.BSR;
import static leola.vm.Opcodes.CLASS_DEF;
import static leola.vm.Opcodes.DIV;
import static leola.vm.Opcodes.DUP;
import static leola.vm.Opcodes.END_BLOCK;
import static leola.vm.Opcodes.EQ;
import static leola.vm.Opcodes.FUNC_DEF;
import static leola.vm.Opcodes.GEN_DEF;
import static leola.vm.Opcodes.GET;
import static leola.vm.Opcodes.GETK;
import static leola.vm.Opcodes.GET_GLOBAL;
import static leola.vm.Opcodes.GET_NAMESPACE;
import static leola.vm.Opcodes.GT;
import static leola.vm.Opcodes.GTE;
import static leola.vm.Opcodes.IDX;
import static leola.vm.Opcodes.IFEQ;
import static leola.vm.Opcodes.INIT_BLOCK;
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
import static leola.vm.Opcodes.MUL;
import static leola.vm.Opcodes.NAMESPACE_DEF;
import static leola.vm.Opcodes.NEG;
import static leola.vm.Opcodes.NEQ;
import static leola.vm.Opcodes.NEW_ARRAY;
import static leola.vm.Opcodes.NEW_MAP;
import static leola.vm.Opcodes.NEW_OBJ;
import static leola.vm.Opcodes.NOT;
import static leola.vm.Opcodes.OPPOP;
import static leola.vm.Opcodes.OR;
import static leola.vm.Opcodes.PARAM_END;
import static leola.vm.Opcodes.POP;
import static leola.vm.Opcodes.REQ;
import static leola.vm.Opcodes.RET;
import static leola.vm.Opcodes.ROTL;
import static leola.vm.Opcodes.ROTR;
import static leola.vm.Opcodes.SET;
import static leola.vm.Opcodes.SETK;
import static leola.vm.Opcodes.SET_GLOBAL;
import static leola.vm.Opcodes.SIDX;
import static leola.vm.Opcodes.STORE_LOCAL;
import static leola.vm.Opcodes.STORE_OUTER;
import static leola.vm.Opcodes.SUB;
import static leola.vm.Opcodes.SWAP;
import static leola.vm.Opcodes.SWAPN;
import static leola.vm.Opcodes.TAIL_CALL;
import static leola.vm.Opcodes.THROW;
import static leola.vm.Opcodes.XOR;
import static leola.vm.Opcodes.YIELD;
import static leola.vm.Opcodes.xLOAD_LOCAL;
import static leola.vm.Opcodes.xLOAD_OUTER;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import leola.vm.compiler.Bytecode;
import leola.vm.compiler.Outer;
import leola.vm.compiler.Outer.StackValue;
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
	public static final int DEFAULT_STACKSIZE = 1024;//1024 * 1024;

	/**
	 * Runtime
	 */
	private Leola runtime;

	/*thread stack
	 */
	private LeoObject[] stack;

	/* list of open outers, if this function goes out of scope (i.e., the stack) then the outers
	 * are closed (i.e., the value contained on the stack is transferred used instead of the indexed value
	 */
	private Outer[] openouters;
	private int top;

	/**
	 * The maximum stack size
	 */
	private final int maxStackSize;
	
	/**
	 * The stack value accounts for closures requesting a value off
	 * of the stack and when the are finally 'closed' over.
	 * 
	 * We can't just use the VM.stack variable when closing over
	 * the Outer because the VM.stack variable may be replaced
	 * when the stack grows.
	 */
	private StackValue vmStackValue = new StackValue() {	    
	    @Override
	    public LeoObject getStackValue(int index) {
	        return stack[index];
	    }
	    
	    @Override
	    public void setStackValue(int index, LeoObject value) {	     
	        stack[index] = value;
	    }
	};
	
	/**
	 * @param runtime the {@link Leola} runtime
	 */
	public VM(Leola runtime) {
		this.runtime = runtime;
		
		int stackSize = runtime.getArgs().getStackSize();
		stackSize = (stackSize <= 0) ? DEFAULT_STACKSIZE : stackSize;

		this.maxStackSize = Math.max(runtime.getArgs().getMaxStackSize(), stackSize);
		
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
		
		growStackIfRequired(stack, base, code.maxstacksize);
		
		for(int i = 0; i < code.numArgs; i++) {
			stack[base + i] = LeoNull.LEONULL;
		}
	}

	/**
	 * Checks to see if we should grow the stack
	 * 
	 * @param stack
	 * @param base
	 * @param neededSize
	 * @return the new stack (if no growth was required, the supplied stack is returned).
	 */
	private void growStackIfRequired(LeoObject[] stack, int base, int neededSize) {
	    final int requiredStackSize = base + neededSize;
	    if ( requiredStackSize > this.maxStackSize) {
	        error("Stack overflow, required stack size over maxStackSize '" + this.maxStackSize + "'");
	    }
	    
	    if( requiredStackSize > stack.length) {
	        final int newStackSize = Math.min( stack.length + ((requiredStackSize-stack.length) << 1), this.maxStackSize);
	        LeoObject[] newStack = new LeoObject[newStackSize];
	        System.arraycopy(stack, 0, newStack, 0, base);
	        this.stack = newStack;
	        
	        Outer[] newOuters = new Outer[newStack.length];
	        System.arraycopy(openouters, 0, newOuters, 0, base);
	        this.openouters = newOuters;
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
		LeoObject result = LeoNull.LEONULL;
		LeoObject errorThrown = LeoNull.LEONULL;

		final int[] instr = code.instr;
		final int len = code.len;
		int pc = code.pc;


		final LeoObject[] constants = code.constants;
		final Bytecode[] inner = code.inner;
		
		final Outer[] calleeouters;
		final LeoObject[] genLocals;
		
		
		/* if there is some object calling this function
		 * this means there might be outer scoped variables
		 * that we can access within this byte code
		 */
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
		boolean isReturnedSafely = true;
		
		Scope scope = null;
		
		/* check and see if this is a scoped object,
		 * if so use the scope
		 */
		LeoScopedObject scopedObj = null;
		if ( env instanceof LeoScopedObject) {
			scopedObj = (LeoScopedObject)env;
			scope = scopedObj.getScope();
		}

		/* use the global scope if this object doesn't contain
		 * any scope
		 */
		if(scope==null) {
			LeoNamespace global = runtime.getGlobalNamespace();

			scope=global.getScope();
			scopedObj=global;
		}
		
		
		/* named parameters 
		 */
		List<LeoObject> params = null;
		int paramIndex = 0;
		
		if(code.hasParamIndexes()) {
		    params = new ArrayList<LeoObject>();
		}
		
		
		
		/* exception handling, keeps track of the catch program 
		 * counter */
		Stack<Integer> blockStack = null;
		if(code.hasBlocks()) {
		    blockStack = new Stack<Integer>();
		}
		
		
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
						case LOAD_NAME: {
						    int iname = ARGx(i);
						    
						    LeoObject name = constants[iname];
						    params.add(paramIndex, name);						    						    			   
						    continue;
						}
						case PARAM_END: {
						    paramIndex++;
						    
						    if(params.size() < paramIndex) {
						        params.add(null);
						    }
						    break;
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
							isReturnedSafely = true; /* do not clear out result in an ON block */
							
							pc = len;  /* Break out of the bytecode */
							if ( top>topStack) {
								result = stack[--top];
							}
							break;
						}
						case YIELD: {
							yield = true; /* lets not expire the generator */
							isReturnedSafely = true; /* do not clear out result in an ON block */
							
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
						case SWAP: {
							LeoObject t = stack[top-2];
							stack[top-2] = stack[top-1];
							stack[top-1] = t;
							continue;
						}
						case SWAPN: {
							int n = ARGx(i);
							for(int j = 1; j <= n; j++) {
								LeoObject t = stack[top-j];
								stack[top-j] 	= stack[top-n-j];
								stack[top-n-j] 	= t;
							}
							continue;
						}
						case ROTL: {
							int n = ARGx(i) + 1;
	
							LeoObject t = stack[top-n];
							for(int j = n-1; j > 0; j--) {
								stack[top-j-1] 	= stack[top-j];
							}
	
							stack[top-1] = t;
	
							continue;
						}
                        case ROTR:  {
                            int n = ARGx(i);
    
                            LeoObject t = stack[top-1];
                            for(int j = 1; j < n; j++) {
                                stack[top-j] = stack[top-j-1];
                            }
                            stack[top-n] = t;
    
                            continue;
                        }						
						case JMP:	{
							int pos = ARGsx(i);
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
								    LeoObject[] args = readArrayFromStack(nargs, stack);
									System.arraycopy(args, 0, stack, base, nargs);
								}
							}
	
	
							continue;
						}
						case INVOKE:	{
							int nargs = ARG1(i);
							LeoObject fun = stack[--top];
	
							/* determine if there are named parameters to resolve */
							if(paramIndex > 0 && !fun.isNativeFunction() ) {
							    resolveNamedParameters(params, stack, top, fun, nargs);
							    

						        /* ready this for any other method calls */
							    params.clear();
							    paramIndex = 0;
							}                            
							
							LeoObject c = null;
	
							switch(nargs) {
								case 0: {
									c = fun.call();
									break;
								}
								case 1: {
									LeoObject arg1 = stack[--top];
									c = fun.call(arg1);
									break;
								}
								case 2: {
									LeoObject arg2 = stack[--top];
									LeoObject arg1 = stack[--top];
									c = fun.call(arg1, arg2);
									break;
								}
								case 3: {
									LeoObject arg3 = stack[--top];
									LeoObject arg2 = stack[--top];
									LeoObject arg1 = stack[--top];
									c = fun.call(arg1, arg2, arg3);
									break;
								}
								case 4: {
									LeoObject arg4 = stack[--top];
									LeoObject arg3 = stack[--top];
									LeoObject arg2 = stack[--top];
									LeoObject arg1 = stack[--top];
									c = fun.call(arg1, arg2, arg3, arg4);
									break;
								}
								case 5: {
									LeoObject arg5 = stack[--top];
									LeoObject arg4 = stack[--top];
									LeoObject arg3 = stack[--top];
									LeoObject arg2 = stack[--top];
									LeoObject arg1 = stack[--top];
									c = fun.call(arg1, arg2, arg3, arg4, arg5);
									break;
								}
								default: {
									LeoObject[] args = readArrayFromStack(nargs, stack);
									c = fun.call(args);
								}
							}
	
							stack[top++] = c;
							
							if( c.isError() )  {
							    isReturnedSafely = false;
								errorThrown = c;
								result = c; 	/* throw this error */ 
								pc = len; 		/* exit out of this function */ 
								
								LeoError error = c.as();
								if(error.getLineNumber() < 1) {
									error.setLineNumber(lineNumber);
									error.setSourceFile(code.getSourceFile());
								}
								else {
									error.addStack(new LeoError(lineNumber));
								}
							}
	
							continue;
						}
						case NEW_OBJ:	{
	
							LeoObject className = stack[--top];
	
							int nargs = ARGx(i);
							LeoObject[] args = readArrayFromStack(nargs, stack);
							
							LeoObject instance = null;
	
							ClassDefinitions defs = scope.lookupClassDefinitions(className);
							if ( defs == null ) {
	
								if(!runtime.isSandboxed()) {															
									instance = ClassUtil.newNativeInstance(className.toString(), args);
								}
								else {
									error("Unable to instantiate native Java classes ('"+ className +"') in Sandboxed mode.");
								}
							}
							else {
							    LeoObject resolvedClassName = scope.getClassName(className);
                                ClassDefinition definition = defs.getDefinition(resolvedClassName);
							    
							    if(paramIndex > 0) {				                       
	                                resolveNamedParameters(params, args, nargs, definition.getParameterNames(), nargs);
	                                
	
	                                /* ready this for any other method calls */
	                                params.clear();
	                                paramIndex = 0;
	                            }						    
							    
								instance = defs.newInstance(runtime, definition, args);
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
						case NAMESPACE_DEF: {
							int innerIndex = ARGx(i);
							Bytecode namespacecode = inner[innerIndex];
	
							LeoObject name = stack[--top];
							NamespaceDefinitions ndefs = scope.getNamespaceDefinitions();
							LeoNamespace ns = ndefs.getNamespace(name);
							if(ns==null) {
								ns = new LeoNamespace(this.runtime, namespacecode, new Scope(scope), name);
								ndefs.storeNamespace(name, ns);
							}
							else {
								if(namespacecode.numOuters>0) {
									ns.setOuters(new Outer[namespacecode.numOuters]);
								}
							}
							
							Outer[] outers = ns.getOuters();
							if (outers(outers, calleeouters, openouters, namespacecode.numOuters, base, pc, code)) {
							    closeOuters = true;
							}
							pc += namespacecode.numOuters;
	
							this.runtime.execute(ns, namespacecode);
	
							stack[top++] = ns;
							continue;
						}
						case GEN_DEF: {
							int innerIndex = ARGx(i);
							Bytecode bytecode = inner[innerIndex];
							LeoGenerator fun = new LeoGenerator(this.runtime, scopedObj, bytecode.clone());
	
							Outer[] outers = fun.getOuters();
							if (outers(outers, calleeouters, openouters, bytecode.numOuters, base, pc, code)) {
                                closeOuters = true;
                            }
							pc += bytecode.numOuters;
	
							stack[top++] = fun;
							continue;
						}
						case FUNC_DEF: {
							int innerIndex = ARGx(i);
							Bytecode bytecode = inner[innerIndex];
							LeoFunction fun = new LeoFunction(this.runtime, scopedObj, bytecode);
	
							Outer[] outers = fun.getOuters();							
							if (outers(outers, calleeouters, openouters, bytecode.numOuters, base, pc, code)) {
                                closeOuters = true;
                            }
							pc += bytecode.numOuters;
	
							stack[top++] = fun;
							continue;
						}
						case CLASS_DEF: {
	
							LeoObject bytecodeIndex = stack[--top];
							Bytecode body = inner[bytecodeIndex.asInt()];
							
							int numSuperParams = stack[--top].asInt();
							LeoObject[] superParams = readArrayFromStack(numSuperParams, stack);
	
							int nparams = stack[--top].asInt();
							LeoObject[] paramNames = readArrayFromStack(nparams, stack);
								
							int numberOfInterfaces = ARGx(i);
							LeoObject[] interfaces = readArrayFromStack(numberOfInterfaces, stack);
								
							LeoObject superClassname = stack[--top];
							LeoObject className = stack[--top];
	
	
							ClassDefinition superClassDefinition = null;
	
							if ( ! superClassname.$eq(LeoNull.LEONULL) ) {
								ClassDefinitions defs = scope.lookupClassDefinitions(superClassname);
								superClassDefinition = defs.getDefinition(superClassname);
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
	
							Outer[] outers = classDefinition.getOuters();
							if( outers(outers, calleeouters, openouters, body.numOuters, base, pc, code)) {
                                closeOuters = true;
                            }
							pc += body.numOuters;
							continue;
						}
						case IS_A: {
							LeoObject obj = stack[--top];
							LeoObject type = stack[--top];
	
							stack[top++] = LeoBoolean.valueOf(obj.isOfType(type.toString()));
	
							continue;
						}
						case IFEQ:	{
							LeoObject cond = stack[--top];
							if ( ! LeoObject.isTrue(cond) ) {
								int pos = ARGsx(i);
								pc += pos;
							}
							continue;
						}
						case THROW: {
							/* we are not safely returning */
							isReturnedSafely = false; 
							
							LeoObject str = stack[--top];							
							errorThrown = buildStackTrace(code, errorThrown, str, lineNumber);
	
							stack[top++] = errorThrown;
							
							pc = len; /* exit out of this function */
							continue;
						}
	
						case IDX: {
                            LeoObject index = stack[--top];
                            LeoObject obj = stack[--top];
    
                            LeoObject value = obj.$index(index);
                            stack[top++] = value;						    
						    continue;
						}
						case SIDX: {
	                      
                            LeoObject index = stack[--top];
                            LeoObject obj = stack[--top];
                            LeoObject value = stack[--top];
    
                            obj.$sindex(index, value);
                            stack[top++] = obj; /* make this an expression */
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
						case GETK: {
						    int iname = ARGx(i);
						    LeoObject obj = stack[--top];
						    
                            LeoObject value = obj.getObject(constants[iname]);
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
						case SETK: {
						    int iname = ARGx(i);
                            LeoObject obj = stack[--top];
                            LeoObject value = stack[--top];
    
                            obj.setObject(constants[iname], value);
                            stack[top++] = obj; /* make this an expression */						    
						    continue;
						}
						case GET_GLOBAL: {
							int iname = ARGx(i);
							LeoObject member = scope.getObject(constants[iname]);
							if(member==null) {
							    scopedObj.throwAttributeError(constants[iname]);
							}
							
							stack[top++] = member;
	
							continue;
						}
						case SET_GLOBAL: {
							int iname = ARGx(i);
							scopedObj.addProperty(constants[iname], stack[--top]);
	
							continue;
						}
						case GET_NAMESPACE: {
							int iname = ARGx(i);
							LeoNamespace ns = scope.getNamespace(constants[iname]);
							stack[top++] = ns;
							
							continue;
						}						
						case INIT_BLOCK: {
						    /*
						     * Denote that we are entering a TRY
						     * block that may contain a CATCH and/or FINALLY
						     * blocks
						     */
							blockStack.add(ARGsx(i));
							continue;
						}						
						case END_BLOCK: {						    							
							int endType = ARG1(i);
							switch(endType) {
    							
    							// if this is a start of a catch block
    							// we can clear out the error states
    							case 1: /* End of a Catch */ {
    							    /*
    							     * try
    							     *   ..
    							     * catch e
    							     *   .. // since we are catching
    							     *      // the exception, we need
    							     *      // to clear out the error flags
    							     *      // and denote we are safe to 
    							     *      // return
    							     */
    							    
    							    result = LeoNull.LEONULL;
    							    errorThrown = LeoNull.LEONULL;
    							    isReturnedSafely = true;
    							    break;
    							}
    							case 2: /* End of a Finally */ {
    
    	                            /* if the result is an 
    	                             * error, we need to bubble up the 
    	                             * error.  This happens for:
    	                             * 
    	                             * try 
    	                             *   ..
    	                             * finally
    	                             *   .. // the error is still present and
    	                             *      // must be bubbled up
    	                             */
    	                            if(errorThrown.isError()) {
    	                                pc = len;
    	                            }    
    	                            break;
    							}
    							default: {
    							    /*
    							     * We have reached the end of an
    							     * INIT_BLOCK, which means
    							     * we have passed either a 
    							     * CATCH or FINALLY blocks
    							     */
    							    blockStack.pop();    
    							}
							}
							
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
							LeoObject c = LeoBoolean.valueOf(l.isTrue() || r.isTrue());
							stack[top++] = c;
							continue;
						}
						case AND:	{
							LeoObject r = stack[--top];
							LeoObject l = stack[--top];
							LeoObject c = LeoBoolean.valueOf(l.isTrue() && r.isTrue());
							stack[top++] = c;
							continue;
						}
						case NOT:	{
							LeoObject l = stack[--top];
							LeoObject c = LeoBoolean.valueOf(!l.isTrue());
							stack[top++] = c;
							continue;
						}
	
						case REQ:	{
							LeoObject r = stack[--top];
							LeoObject l = stack[--top];
							LeoObject c = LeoBoolean.valueOf(l.$req(r));
							stack[top++] = c;
							continue;
						}
						case EQ:	{
							LeoObject r = stack[--top];
							LeoObject l = stack[--top];
							LeoObject c = LeoBoolean.valueOf(l.$eq(r));
							stack[top++] = c;
							continue;
						}
						case NEQ:	{
							LeoObject r = stack[--top];
							LeoObject l = stack[--top];
							LeoObject c = LeoBoolean.valueOf(l.$neq(r));
							stack[top++] = c;
							continue;
						}
						case GT:	{
							LeoObject r = stack[--top];
							LeoObject l = stack[--top];
							LeoObject c = LeoBoolean.valueOf(l.$gt(r));
							stack[top++] = c;
							continue;
						}
						case GTE:	{
							LeoObject r = stack[--top];
							LeoObject l = stack[--top];
							LeoObject c = LeoBoolean.valueOf(l.$gte(r));
							stack[top++] = c;
							continue;
						}
						case LT:	{
							LeoObject r = stack[--top];
							LeoObject l = stack[--top];
							LeoObject c = LeoBoolean.valueOf(l.$lt(r));
							stack[top++] = c;
							continue;
						}
						case LTE:	{
							LeoObject r = stack[--top];
							LeoObject l = stack[--top];
							LeoObject c = LeoBoolean.valueOf(l.$lte(r));
							stack[top++] = c;
							continue;
						}
						default: {
							error("Unknown opcode '" + opcode + "' found for the Bytecode '" + Integer.toHexString(i) + "'");
						}
					}
				}
			}
			catch(Throwable e) {						

				/* clear out result in an ON block */
				isReturnedSafely = false; 
				
				/**
				 * Return the error
				 */
				errorThrown = buildStackTrace(code, errorThrown, e, lineNumber);								
				
				stack[top++] = errorThrown;
				pc = len; 		/* exit out of this function */
			}
			finally {
				
				if(blockStack != null && !blockStack.isEmpty()) {
					pc = blockStack.peek();
				}
				else {
				    final int stackSize = Math.min(stack.length, base+code.maxstacksize);
					/* close the outers for this function call */
					if (closeOuters) {
						for(int j=base;j<stackSize;j++) {
							if(openouters[j]!=null) {
								openouters[j].close();
								openouters[j] = null;
							}
		
							stack[j] = null;
						}
					}
					else {
					    for(int j=base;j<stackSize;j++) {
					        stack[j] = null;
					    }			    
					}
		
					top = base;
					
					/* expire this generator if we hit the end of the function */
					if(!yield && callee != null && callee.isGenerator()) {
						if(pc == len) {
							code.pc = pc;
						}
					}		
				}
			}
		} while(blockStack != null && !blockStack.isEmpty());

		return isReturnedSafely ? 
				 result : errorThrown;
	}

	
	/**
	 * Reads an array of values from the stack.
	 * 
	 * @param nargs
	 * @param stack
	 * @return the array of {@link LeoObject}'s, or null if nargs <= 0
	 */
	private LeoObject[] readArrayFromStack(int nargs, LeoObject[] stack) {
	    LeoObject[] args = null;
        if ( nargs > 0 ) {
            args = new LeoObject[nargs];
            for(int j = nargs - 1; j >= 0; j--) {
                args[j] = stack[--top];
            }
        }
        
        return args;
	}
	
	/**
	 * Builds the stack trace based off of the current stack trace and error message.
	 * 
	 * @param code the current bytecode being executed
	 * @param errorThrown the current error thrown (if any)
	 * @param message the Error message and/or Exception
	 * @return the error thrown 
	 */
	private LeoObject buildStackTrace(Bytecode code, LeoObject errorThrown, Object message, int lineNumber) {
		
	    LeoError error = (message instanceof LeolaRuntimeException) ?
	                        ((LeolaRuntimeException)message).getLeoError()
	                            : new LeoError(message.toString());
	    
	    error.setSourceFile(code.getSourceFile());
	    error.setLineNumber(lineNumber);
	    
		if(errorThrown.isError()) {
			LeoError parentError = errorThrown.as();
			parentError.addStack(error);
		}
		else {
			errorThrown = error;
		}
	    
		
		return errorThrown;
	}
	
	/**
	 * Handles an error in the execution.
	 *
	 * @param errorMsg
	 */
	private void error(String errorMsg) {
		if(errorMsg==null) {
			errorMsg = "";
		}
		
		throw new LeolaRuntimeException("ExecutionError: " + errorMsg);
	}

	
	/**
	 * Resolve the named parameters
	 * 
	 * @param params
	 * @param fun
	 * @param nargs
	 */
	private void resolveNamedParameters(List<LeoObject> params, LeoObject[] args, int argTop, LeoObject fun, int nargs) {	    
        /* assume this is a function */
        LeoFunction f = fun.as();
        Bytecode bc = f.getBytecode();
        
        resolveNamedParameters(params, args, argTop, bc.paramNames, nargs);                                                                                                         
	}
    
    /**
     * Resolve the named parameters
     * 
     * @param params
     * @param stack
     * @param paramNames
     * @param nargs
     */
    private void resolveNamedParameters(List<LeoObject> params, LeoObject[] args, int topArgs, LeoObject[] paramNames, int nargs) {                   
        /* store stack arguments in a temporary location */
        int tmpTop = top;
        LeoObject[] tmp = stack;//new LeoObject[nargs];
        for(int stackIndex = 0; stackIndex < nargs; stackIndex++) {
            tmp[tmpTop + stackIndex] = args[topArgs - stackIndex - 1];
        }
        
                                                
        /* iterate through the parameter names and adjust the stack
         * so that the names match the position the function expects them
         */
        for(int stackIndex = 0; stackIndex < params.size(); stackIndex++) {
            LeoObject paramName = params.get(stackIndex);
            if(paramName != null) {             
                int paramIndex = 0;
                for(; paramIndex < paramNames.length; paramIndex++) {
                    if(paramNames[paramIndex].$eq(paramName)) {
                        break;
                    }
                }
                
                args[topArgs - (nargs - paramIndex)] = tmp[tmpTop + (nargs - stackIndex - 1)];
            }
        }                                                                                                          
    }
	
	/**
	 * Close over the outer variables for closures.
	 * 
	 * @param outers
	 * @param calleeouters
	 * @param openouters
	 * @param numOuters
	 * @param base
	 * @param pc
	 * @param code
	 * @return true if there where Outers created that should be closed over once we leave the function
	 * scope
	 */
	private boolean outers(Outer[] outers, Outer[] calleeouters, Outer[] openouters, 
	                    int numOuters, 
	                    int base, 
	                    int pc, 
	                    Bytecode code) {
	    
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
								(openouters[bindex] = new Outer(vmStackValue, bindex));
					closeOuters = true;
					break;
				}
				default: {
					error("Outer opcode '" + opCode +"' is invalid");
				}
			}
		}

		return closeOuters;
	}
}

