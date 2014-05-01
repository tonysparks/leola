/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.vm.types;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import leola.vm.VM;
import leola.vm.asm.Bytecode;
import leola.vm.asm.Symbols;


/**
 * A {@link LeoGenerator} is an object that behaves like a function, but
 * is "resumeable" from a yield statement.  That is, after successive invocations
 * it will pick up executing the function after the yield statement.
 * 
 * <p>
 * Example:
 * <pre>
 *   var talk = gen() {
 *     var i = 0
 *     yield "Hello: " + i
 *     i += 1
 *     yield "World: " + i
 *   }
 *   
 *   println( talk() ) // prints Hello: 0
 *   println( talk() ) // prints World: 1
 *   println( talk() ) // prints null, the function is done computing
 *   
 * </pre>
 *
 * @author Tony
 *
 */
public class LeoGenerator extends LeoOuterObject {

	/**
	 * Arguments
	 */
	private int numberOfArgs;

	/**
	 * Body
	 */
	private Bytecode bytecode;

	/**
	 * The environment it was created in
	 */
	private LeoObject env;
	
	/**
	 * The locals that need to be saved with this
	 * generator
	 */
	private LeoObject[] locals;
	
	/**
	 * @param type
	 * @param numberOfArgs
	 * @param body
	 */
	public LeoGenerator(LeoObject env, Bytecode bytecode) {
		super(LeoType.GENERATOR, bytecode.numOuters);
		
		this.env = env;
		this.bytecode = bytecode;
		this.numberOfArgs = bytecode.numArgs;
		this.locals = new LeoObject[bytecode.numLocals];
	}
	
	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#add(leola.vm.types.LeoObject)
	 */
	@Override
	public LeoObject $add(LeoObject other) {
		if (other.isString()) {
			return LeoString.valueOf(toString() + other.toString());
		}
		return super.$add(other);
	}
	
	/**
	 * @return the locals
	 */
	public LeoObject[] getLocals() {
		return locals;
	}
	
	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#isGenerator()
	 */
	@Override
	public boolean isGenerator() {
		return true;
	}
		
	/**
	 * @return the bytecode
	 */
	public Bytecode getBytecode() {
		return bytecode;
	}
	
	/**
	 * @return the numberOfArgs
	 */
	public int getNumberOfArgs() {
		return numberOfArgs;
	}

	/**
	 * Invokes the function
	 * 
	 * @param vm
	 * @return
	 */
	@Override
	public LeoObject call(VM vm) {
		return vm.execute(env, this, this.bytecode);		
	}
	
	/**
	 * Invokes the function
	 * 
	 * @param vm
	 * @param arg1
	 * @return
	 */
	@Override
	public LeoObject call(VM vm, LeoObject arg1) {
		return vm.execute(env, this, this.bytecode, arg1);		
	}
	
	/**
	 * Invokes the function
	 * 
	 * @param vm
	 * @param arg1
	 * @param arg2
	 * @return
	 */
	@Override
	public LeoObject call(VM vm, LeoObject arg1, LeoObject arg2) {
		return vm.execute(env, this, this.bytecode, arg1, arg2);
	}
	
	/**
	 * Invokes the function
	 * 
	 * @param vm
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @return
	 */
	@Override
	public LeoObject call(VM vm, LeoObject arg1, LeoObject arg2, LeoObject arg3) {
		return vm.execute(env, this, this.bytecode, arg1, arg2, arg3);
	}
	
	/**
	 * Invokes the function
	 * 
	 * @param vm
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @param arg4
	 * @return
	 */
	@Override
	public LeoObject call(VM vm, LeoObject arg1, LeoObject arg2, LeoObject arg3, LeoObject arg4) {
		return vm.execute(env, this, this.bytecode, arg1, arg2, arg3, arg4);
	}
	
	/**
	 * Invokes the function
	 * 
	 * @param vm
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @param arg4
	 * @param arg5
	 * @return
	 */
	@Override
	public LeoObject call(VM vm, LeoObject arg1, LeoObject arg2, LeoObject arg3, LeoObject arg4, LeoObject arg5) {
		return vm.execute(env, this, this.bytecode, arg1, arg2, arg3, arg4, arg5);
	}
	
	/**
	 * Invokes the function
	 * 
	 * @param vm
	 * @param args
	 * @return
	 */
	@Override
	public LeoObject call(VM vm, LeoObject[] args) {
		return vm.execute(env, this, this.bytecode, args);		
	}

	/* (non-Javadoc)
	 * @see leola.types.LeoObject#eq(leola.types.LeoObject)
	 */
	@Override
	public boolean $eq(LeoObject other) {
		return this == other;
	}


	/* (non-Javadoc)
	 * @see leola.types.LeoObject#gt(leola.types.LeoObject)
	 */
	@Override
	public boolean $gt(LeoObject other) {
		return false;
	}


	/* (non-Javadoc)
	 * @see leola.types.LeoObject#lt(leola.types.LeoObject)
	 */
	@Override
	public boolean $lt(LeoObject other) {
		return false;
	}

	/* (non-Javadoc)
	 * @see leola.types.LeoObject#getValue()
	 */
	@Override
	public Object getValue() {
		return this;
	}
	
	/* (non-Javadoc)
	 * @see leola.types.LeoObject#clone()
	 */
	@Override
	public LeoObject clone() {
		return this;
	}
	
	@Override
	public void write(DataOutput out) throws IOException {
//		out.write(this.getType().ordinal());
//		this.bytecode.write(out);
//		int nouters = this.outers!=null?this.outers.length:0;
//		if (nouters>0) {
////			for(int i =0; i < nouters; i++) {
////				LeoObject o = this.outers[i];
////				if ( o == null ) {
////					nouters = i;
////					break;
////				}			
////			}
//			
//			out.writeInt(nouters);
//			
////			for(int i =0; i < nouters; i++) {
////				LeoObject o = this.outers[i];						
////				o.write(out);
////			}
//		}
//		else {
//			out.writeInt(nouters);
//		}
	}
	
	/**
	 * Reads from the {@link DataInput} stream, constructing a {@link LeoObject}
	 * 
	 * @param in
	 * @return the {@link LeoObject}
	 * @throws IOException
	 */
	public static LeoGenerator read(LeoObject env, Symbols symbols, DataInput in) throws IOException {
		Bytecode bytecode = Bytecode.read(env, symbols, in);
		int nouters = in.readInt();
		
		LeoObject[] outers = new LeoObject[nouters];
		for(int i =0; i < nouters; i++) {
			outers[i] = LeoObject.read(env, symbols, in);
		}
		
		LeoGenerator function = new LeoGenerator(env, bytecode);	
		return function;
	}
}

