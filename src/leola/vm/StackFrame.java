/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.vm;

import leola.vm.asm.Bytecode;
import leola.vm.asm.Scope;
import leola.vm.types.LeoFunction;
import leola.vm.types.LeoObject;

/**
 * The Stack frame contains first all functions Arguments, then any local variables.
 * 
 * There are three means of storage:
 * <pre>
 * 1) Stack storage - local variables + Arguments stored on the stack.  These only last for the duration
 * of the StackFrame (i.e., function call)
 * 
 * 2) Scope storage - which is bound to a {@link Scope}.  These are persistent changes, i.e., they 
 * will last after a StackFrame (i.e., function call).  The Scope storage, is primarily used for
 * classes in which the {@link Scope} is cloned during a class instantiation. 
 *  
 * 3) Outer storage - which is when a function references a local variable from an outer function.
 * These variables are referred to as "Outers" and can be added to a {@link LeoFunction}.  These are
 * different from a "Global" (aka Upvalue) in that they reference a Stack value versus a "Local".
 * </pre>
 * @author Tony
 *
 */
public class StackFrame {
	
	/**
	 * The local stack
	 */
	public final LeoObject[] stack;		
	public final Bytecode bytecode;
	public final int base;
	public final int nargs;
	public final int numlocals;

	public final LeoObject callee;

	private StackFrame(LeoObject[] stack
					, int top
					, LeoObject callee
					, Bytecode bytecode
					, int nargs) {		
		this.base = top;
		
		this.callee = callee;		
		this.bytecode = bytecode;		
		if ( this.callee != null && this.callee.isFunction()) {
			LeoFunction fun = this.callee.as();
			this.nargs = fun.getNumberOfArgs();
		}
		else {
			this.nargs = nargs;
		}
		
		this.numlocals = bytecode.numLocals;
		
		/*
		 * TODO: implement a smarter maxstacksize algorithm
		 */
		
//		int stacksize = this.nargs + this.numlocals + bytecode.maxstacksize; 		
//		stacksize += bytecode.numConstants;				
//		stacksize += bytecode.numOuters;		
//		
//		/* unsure the stack size */
//		if ( stacksize + top > stack.length ) {
//			stack = ArrayUtil.resize(stack, stacksize + top + 1);	
//		}		
		
		this.stack = stack;
	}

	/**
	 * @param callee
	 * @param bytecode
	 * @param arg1
	 */
	public StackFrame(LeoObject[] stack
					, int top
					, LeoObject callee
					, Bytecode bytecode
					, LeoObject arg1) {
		this(stack, top, callee, bytecode, 1);								
		this.stack[this.base + 0] = arg1;								
	}
	
	/**
	 * @param callee
	 * @param bytecode
	 * @param arg1
	 * @param arg2
	 */
	public StackFrame(LeoObject[] stack
					, int top
					,LeoObject callee
					, Bytecode bytecode
					, LeoObject arg1
					, LeoObject arg2) {
		this(stack, top, callee, bytecode, 2);								
		this.stack[this.base + 0] = arg1;
		this.stack[this.base + 1] = arg2;					
	}
	
	/**
	 * @param callee
	 * @param scope
	 * @param bytecode
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 */
	public StackFrame(LeoObject[] stack
					, int top
					,LeoObject callee
					, Bytecode bytecode
					, LeoObject arg1
					, LeoObject arg2
					, LeoObject arg3) {
		this(stack, top, callee, bytecode, 3);								
		this.stack[this.base + 0] = arg1;
		this.stack[this.base + 1] = arg2;
		this.stack[this.base + 2] = arg3;			
	}
	
	/**
	 * @param callee
	 * @param bytecode
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @param arg4
	 */
	public StackFrame(LeoObject[] stack
					, int top
					,LeoObject callee
					, Bytecode bytecode
					, LeoObject arg1
					, LeoObject arg2
					, LeoObject arg3
					, LeoObject arg4) {
		this(stack, top, callee, bytecode, 4);								
		this.stack[this.base + 0] = arg1;
		this.stack[this.base + 1] = arg2;
		this.stack[this.base + 2] = arg3;
		this.stack[this.base + 3] = arg4;			
	}

	/**
	 * @param callee
	 * @param bytecode
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @param arg4
	 * @param arg5
	 */
	public StackFrame(LeoObject[] stack
					, int top
					,LeoObject callee
					, Bytecode bytecode
					, LeoObject arg1
					, LeoObject arg2
					, LeoObject arg3
					, LeoObject arg4
					, LeoObject arg5) {
		this(stack, top, callee, bytecode, 5);								
		this.stack[this.base + 0] = arg1;
		this.stack[this.base + 1] = arg2;
		this.stack[this.base + 2] = arg3;
		this.stack[this.base + 3] = arg4;
		this.stack[this.base + 4] = arg5;			
	}
	
	/**
	 * @param callee
	 * @param bytecode
	 * @param args
	 */
	public StackFrame(LeoObject[] stack
					, int top
					,LeoObject callee
					, Bytecode bytecode
					, LeoObject[] args
					) {
		this(stack, top, callee, bytecode, args != null ? args.length : 0);
		if ( args != null ) {						
			System.arraycopy(args, 0, stack, this.base, args.length);
			
		}				
	}
	
}

