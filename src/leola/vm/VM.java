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
import leola.vm.lib.LeolaLibrary;
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
 * The {@link VM} (i.e., the Virtual Machine) is responsible for executing the Leola bytecode.  The current implementation uses a <code>stack</code>
 * in order to operate.  The stack is shared amongst the call stack.  An important implementation detail is that if you are using a shared {@link Leola} 
 * runtime instance, in order to be thread-safe, you must configure it to use {@link ThreadLocal}'s (via {@link Args#allowThreadLocal()}).  By enabling the
 * use of {@link ThreadLocal}, a {@link VM} instance will be created per thread.  Please note, this does not fully guarantee thread-safety as the {@link Scope}
 * containers are still shared amongst threads, i.e., assigning scoped variables is not atomic.  
 * 
 * <p>
 * 
 * <h2>The Stack</h2>
 * 
 * The VM attempts to optimize the allotted stack space by growing it on demand.  Upon initial startup, the VM will default to the {@link VM#DEFAULT_STACKSIZE} 
 * size (this may be altered by {@link Args#getStackSize()}).  The stack size will grow if required (by executing a {@link Bytecode#maxstacksize} which is more 
 * than what is currently available); the stack size does have a max capacity of growth which defaults to <code>({@link #DEFAULT_STACKSIZE} * {@link #DEFAULT_STACKSIZE})</code> 
 * - (this can be altered by {@link Args#getMaxStackSize()}).
 * 
 * <p>
 * 
 * The shared stack is purely a performance enhancement; it took me a while to convince myself this was the appropriate approach.  The alternative would be 
 * to create a new stack per {@link VM#execute(LeoObject, LeoObject, Bytecode)}.  In doing profiling, this created an immense amount of garbage (however speed 
 * was relatively the same).
 * 
 * <p>
 * <h2>Exception Handling</h2>
 * The VM shouldn't throw any {@link Exception}s (unless there truly is an exceptional error in the VM), but instead will wrap any {@link Exception}s into a 
 * {@link LeoError} which will be used as the result of executing a {@link VM#execute(LeoObject, LeoObject, Bytecode)}.  This approach keeps the execution 
 * functions 'predictable'.  What does this mean?  It means it allows me to write the Java code without having to worry about exceptions being thrown from
 * the client code.  I can treat the {@link LeoObject#call()} as a discrete unit of work, and if the Leola code contains an exception, it is a Leola exception
 * and <b>not</b> an interpreter/Java exception.  
 * <p>
 * Given the above, there are plenty of scenarios were I <b>do</b> want to listen for exceptions from the Leola function call.  The common case for this
 * scenario is in the standard library or {@link LeolaLibrary}'s.  The API's will want to know when an exception occurs, and handle them accordingly.  In these
 * scenarios the API author should use the helper methods {@link LeoObject#xcall()}, which will throw a {@link LeolaRuntimeException} is the result of the 
 * call is a {@link LeoError}.
 * 
 * <p>
 * <h2>Object lifecyle</h2>
 * As any sane implementation on the JVM, I take advantage of the JVM's garbage collector.  However, the JVM has to know when an object should be collected.
 * Since the Leola {@link VM} contains a shared stack, we must be diligent in ensuring the stack only contains 'alive' objects.  After each 
 * {@link VM#execute(LeoObject, LeoObject, Bytecode)} call, the VM's stack values are <code>popped</code> off for that function.  It does this by when an 
 * execution is started it marks the current <code>top</code> of the stack, once the execution is terminated, it will <code>pop</code> off of the stack until
 * the marked <code>top</code>.
 * 
 * <p>
 * Since Leola supports closures, we have to be a little more advanced in our stack management and object life cycle.  
 *
 * <p>
 *
 * @author Tony
 *
 */
public class VM {
    
	/**
	 * Maximum stack size
	 */
	public static final int DEFAULT_STACKSIZE = 1024;

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

		this.maxStackSize = Math.max(runtime.getArgs().getMaxStackSize(), stackSize*stackSize);
		
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

		LeoObject result = executeStackframe(env, code, callee, base );
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
		
		LeoObject result = executeStackframe(env, code, callee, base );
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

		LeoObject result = executeStackframe(env, code, callee, base );
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

		LeoObject result = executeStackframe(env, code, callee, base );
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

		LeoObject result = executeStackframe(env, code, callee, base );
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

		LeoObject result = executeStackframe(env, code, callee, base );
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
	private LeoObject executeStackframe(LeoObject env, Bytecode code, LeoObject callee, int base) throws LeolaRuntimeException {
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
							isReturnedSafely = true; 
							
							pc = len;  /* Break out of the bytecode */
							if ( top>topStack) {
								result = stack[--top];
							}
							break;
						}
						case YIELD: {
							yield = true; /* lets not expire the generator */
							isReturnedSafely = true; 
							
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
							LeoObject fun = stack[--top];
							
							int nargs = ARG1(i);
							
	                         
                            /* determine if there are named parameters to resolve */
                            if(paramIndex > 0 ) {
                                nargs = resolveNamedParameters(params, stack, top-nargs, fun, nargs);
                                

                                /* ready this for any other method calls */
                                params.clear();
                                paramIndex = 0;
                            }   
							
							if(code.hasVarargs()) {
							    int numberOfArguments = (nargs - code.numArgs) + 1;
    							int expandedArgs = ARG2(i);
    							
    							/* If we don't have an expanded array request, wrap up the arguments
    							 * into a new array
    							 */
    							if(expandedArgs<1) {
        							LeoArray arguments = new LeoArray(readArrayFromStack(numberOfArguments, stack));    							
        							stack[top++] = arguments;
        							nargs = code.numArgs;
    							}
							}							
							
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
						    LeoObject fun = stack[--top];
						    int nargs = ARG1(i);
						    
						    
                            /* determine if there are named parameters to resolve */
                            if(paramIndex > 0 && !fun.isNativeFunction() ) {
                                nargs = resolveNamedParameters(params, stack, top-nargs, fun, nargs);
                                

                                /* ready this for any other method calls */
                                params.clear();
                                paramIndex = 0;
                            } 
						    
						    /* If we have an expanded array argument,
						     * go ahead and expand it
						     */
	                        int argIndex = ARG2(i);
	                        if(argIndex>0) {
	                            if(!fun.isNativeFunction()) {    	                            
    	                            if(!fun.hasVarargs()) {
    	                                error(fun + " does not accept variable arguments.");
    	                            }
    	                            int expectedNumberOfArguments = fun.getNumberOfArgs();
    	                            if(nargs < expectedNumberOfArguments) {
    	                                error("Invalid number of parameters '" + nargs + "' before the variable arguments expansion '" + expectedNumberOfArguments + "'.");
    	                            }
	                            }
	                            
                                nargs += expandArrayArgument() - 1;
	                        }
                           
							
							LeoObject c = null;
	
							switch(nargs) {
								case 0: {
									c = fun.xcall();
									break;
								}
								case 1: {
									LeoObject arg1 = stack[--top];
									c = fun.xcall(arg1);
									break;
								}
								case 2: {
									LeoObject arg2 = stack[--top];
									LeoObject arg1 = stack[--top];
									c = fun.xcall(arg1, arg2);
									break;
								}
								case 3: {
									LeoObject arg3 = stack[--top];
									LeoObject arg2 = stack[--top];
									LeoObject arg1 = stack[--top];
									c = fun.xcall(arg1, arg2, arg3);
									break;
								}
								case 4: {
									LeoObject arg4 = stack[--top];
									LeoObject arg3 = stack[--top];
									LeoObject arg2 = stack[--top];
									LeoObject arg1 = stack[--top];
									c = fun.xcall(arg1, arg2, arg3, arg4);
									break;
								}
								case 5: {
									LeoObject arg5 = stack[--top];
									LeoObject arg4 = stack[--top];
									LeoObject arg3 = stack[--top];
									LeoObject arg2 = stack[--top];
									LeoObject arg1 = stack[--top];
									c = fun.xcall(arg1, arg2, arg3, arg4, arg5);
									break;
								}
								default: {
									LeoObject[] args = readArrayFromStack(nargs, stack);
									c = fun.xcall(args);
								}
							}
	
							stack[top++] = c;	
							continue;
						}
						case NEW_OBJ:	{
	
							LeoObject className = stack[--top];
	
							int nargs = ARG1(i);
							
							
							LeoObject instance = null;
	
							ClassDefinitions defs = scope.lookupClassDefinitions(className);
							if ( defs == null ) {
	
								if(runtime.isSandboxed()) {															
								    error("Unable to instantiate native Java classes ('"+ className +"') in Sandboxed mode.");
								}
							
								LeoObject[] args = readArrayFromStack(nargs, stack);
								instance = ClassUtil.newNativeInstance(className.toString(), args);
							}
							else {
							    LeoObject resolvedClassName = scope.getClassName(className);
                                ClassDefinition definition = defs.getDefinition(resolvedClassName);
                                LeoObject[] args = new LeoObject[definition.getNumberOfParameters()];
                                args = readArrayFromStack(args, nargs, stack);
                                for(int j=nargs; j < args.length; j++) {
                                    args[j]=LeoObject.NULL;
                                }
                                
							    if(paramIndex > 0) {				                       
	                                resolveNamedParameters(params, args, 0, definition.getParameterNames(), nargs);
	                                
	
	                                /* ready this for any other method calls */
	                                params.clear();
	                                paramIndex = 0;
	                            }				
							    
							    if(definition.hasVarargs()) {
	                                int numberOfArguments = (nargs - definition.getNumberOfParameters()) + 1;
	                                int startVarArgIndex = nargs-numberOfArguments;
	                                int expandedArgs = ARG2(i);
	                                
	                                /* If we don't have an expanded array request, wrap up the arguments
	                                 * into a new array
	                                 */
	                                if(expandedArgs<1) {
	                                    LeoArray arguments = new LeoArray(numberOfArguments);
	                                    for(int k=startVarArgIndex; k<args.length;k++) {
	                                        arguments.add(args[k]);
	                                    }
	                                    args[startVarArgIndex] = arguments;
	                                }
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
							if (assignOuters(outers, calleeouters, openouters, namespacecode.numOuters, base, pc, code)) {
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
							if (assignOuters(outers, calleeouters, openouters, bytecode.numOuters, base, pc, code)) {
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
							if (assignOuters(outers, calleeouters, openouters, bytecode.numOuters, base, pc, code)) {
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
							if( assignOuters(outers, calleeouters, openouters, body.numOuters, base, pc, code)) {
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
//							isReturnedSafely = false; 
//							
//							LeoObject str = stack[--top];							
//							errorThrown = buildStackTrace(code, errorThrown, str, lineNumber);
//	
//							stack[top++] = errorThrown;
//							
//							pc = len; /* exit out of this function */
						    
						    LeoObject str = stack[--top];         						    		   
						    throw new LeolaRuntimeException(new LeoError(str));
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
     * @param args
     * @param nargs
     * @param stack
     * @return the array of {@link LeoObject}'s, or null if nargs <= 0
     */
    private LeoObject[] readArrayFromStack(LeoObject[] args, int nargs, LeoObject[] stack) {                           
        for(int j = nargs - 1; j >= 0; j--) {
            args[j] = stack[--top];
        }                
        return args;
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
            return readArrayFromStack(args, nargs, stack);
        }
        
        return args;
	}
	
	/**
	 * Expands the first function argument (which ends up really be the last argument in Leola code).
	 * This will also ensure that the stack is appropriately resized if necessary.
	 * 
	 * @param doExpansion - if the expansion should actually be done
	 * @return the number of arguments that were expanded
	 */
	private int expandArrayArgument() {	    
        LeoObject l = stack[--top];
        if(!l.isArray()) {
            error(l + " is not an array");
        }
        
        LeoArray array = l.as();
        int size = array.size();
                
        
        growStackIfRequired(stack, top, size+1);
        
        
        for(int index = 0; index < size; index++) {
            stack[top++] = array.get(index);    
        }              
        
        return size;
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
		if( (message instanceof LeolaRuntimeException) ) {
		    LeoError error = ((LeolaRuntimeException)message).getLeoError();
		
		    if(error.getLineNumber()<0) {                         
	            error.setSourceFile(code.getSourceFileName());
	            error.setLineNumber(lineNumber);	            
	        }
		    else if(errorThrown.isError()) {
	            LeoError parentError = errorThrown.as();
	            parentError.addStack(error);
	            error = parentError;
	        }
		    else {
		        LeoError cause = new LeoError();
		        cause.setSourceFile(code.getSourceFileName());
                cause.setLineNumber(lineNumber);
                error.addStack(cause);
                
		    }
		    
		    errorThrown = error;
		}
		else {
	    
    	    LeoError error = new LeoError(message.toString());
    	    
            if(error.getLineNumber()<0) {	                        
        	    error.setSourceFile(code.getSourceFileName());
        	    error.setLineNumber(lineNumber);
            }
    	    
    		if(errorThrown.isError()) {
    			LeoError parentError = errorThrown.as();
    			parentError.addStack(error);
    		}
    		else {
    			errorThrown = error;
    		}
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
	 * @return the number of arguments to be expected
	 */
	private int resolveNamedParameters(List<LeoObject> params, LeoObject[] args, int argTop, LeoObject fun, int nargs) {	    
        /* assume this is a function */
        LeoFunction f = fun.as();
        Bytecode bc = f.getBytecode();
        
        resolveNamedParameters(params, args, argTop, bc.paramNames, nargs);
        
        /* If we received less number of parameters than expected,
         * adjust the top of the stack, because we are accounting for
         * them now
         */
        int expectedNumberOfParameters = bc.paramNames.length;
        if(nargs < expectedNumberOfParameters) {
            top += expectedNumberOfParameters-nargs;
            nargs = expectedNumberOfParameters;
        }
        
        return nargs;
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
        int expectedNumberOfArgs = paramNames.length;
//        int tmpTop = 0;//top;
        int tmpTop = top;
        
        
//        LeoObject[] tmp = new LeoObject[expectedNumberOfArgs];//stack
        LeoObject[] tmp = stack;
        
        /* Clone the supplied arguments into a temporary
         * variable (we use the execution stack for performance reasons,
         * this helps us avoid an allocation)
         */
        for(int stackIndex = 0; stackIndex < expectedNumberOfArgs; stackIndex++) {            
            if(stackIndex < nargs) {
                tmp[tmpTop + stackIndex] = args[topArgs + stackIndex];
            }
            else {
                tmp[tmpTop + stackIndex] = LeoObject.NULL;
            }
            
            args[topArgs+stackIndex] = null;
        }
                
        int[] otherIndexes = new int[expectedNumberOfArgs];
        
        /* iterate through the parameter names and adjust the stack
         * so that the names match the position the function expects them
         */
        for(int stackIndex = 0; stackIndex < params.size(); stackIndex++) {
            LeoObject paramName = params.get(stackIndex);
            if(paramName != null) {             
                
                /* Find the appropriate argument position
                 * index for the named parameter
                 */
                int paramIndex = 0;
                for(; paramIndex < paramNames.length; paramIndex++) {
                    if(paramNames[paramIndex].$eq(paramName)) {
                        break;
                    }
                }
                
                if(paramIndex>=paramNames.length) {
                    error("Invalid parameter name '" + paramName + "'");
                }
                
                otherIndexes[stackIndex] = paramIndex + 1;
            }   
            else {
                otherIndexes[stackIndex] = -(stackIndex+1);
            }
        }            

        /* Assign the named parameters to the correct position
         */
        for(int i = 0; i < expectedNumberOfArgs; i++) {         
            if(otherIndexes[i]>0) {
                args[topArgs+ (otherIndexes[i]-1) ] = tmp[tmpTop + i];
            }            
        }
        
        /* Account for arguments that do not have a name
         * in front of them.  In these cases, we 'fill-in-the-blanks'
         * in order from left to right.
         */
        int lastUnusedIndex = 0;
        for(int i = 0; i < expectedNumberOfArgs; i++) {
            if(args[topArgs+i] == null) {
                for(; lastUnusedIndex < otherIndexes.length;) {
                    if(otherIndexes[lastUnusedIndex]<0) {
                        args[ topArgs+i ] = tmp[tmpTop + lastUnusedIndex++ ];
                        break;
                    }      
                    lastUnusedIndex++;
                }
            }
        }
        
        /* Whatever is left over, just assign
         * them to NULL
         */
        for(int i = 0; i < expectedNumberOfArgs; i++) {
            if(args[topArgs+i]==null)
                args[topArgs+i] = LeoObject.NULL;
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
	private boolean assignOuters(Outer[] outers, Outer[] calleeouters, Outer[] openouters, 
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

