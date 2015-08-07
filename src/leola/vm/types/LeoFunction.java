/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.vm.types;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

import leola.vm.Leola;
import leola.vm.VM;
import leola.vm.compiler.Bytecode;


/**
 * A {@link LeoFunction} is a function or better known as a Closure.
 *
 * @author Tony
 *
 */
public class LeoFunction extends LeoOuterObject {

    /**
     * The Runtime
     */
    private Leola runtime;
	
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
	 * @param runtime
	 * @param env
	 * @param bytecode
	 */
	public LeoFunction(Leola runtime, LeoObject env, Bytecode bytecode) {
		this(runtime, LeoType.FUNCTION, env, bytecode);				
	}
	
	
	/**
	 * @param runtime
	 * @param type
	 * @param env
	 * @param bytecode
	 */
	protected LeoFunction(Leola runtime, LeoType type, LeoObject env, Bytecode bytecode) {
		super(type, bytecode.numOuters);
		
		this.runtime = runtime;
		this.env = env;
		this.bytecode = bytecode;
		this.numberOfArgs = bytecode.numArgs;
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
	
	
	/* (non-Javadoc)
	 * @see leola.types.LeoObject#isFunction()
	 */
	@Override
	public boolean isFunction() {
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

	@Override
	public LeoObject call() {
		return this.runtime.getActiveVM().execute(env, this, this.bytecode);		
	}
	
	@Override
	public LeoObject call(LeoObject arg1) {
	    VM vm = this.runtime.getActiveVM();
		if(this.bytecode.hasVarargs()) {
			switch(this.bytecode.getVarargIndex()) {
				case 0: return vm.execute(env, this, this.bytecode, LeoArray.toLeoArray(arg1));
			}
		}
		return vm.execute(env, this, this.bytecode, arg1);		
	}
	
	@Override
	public LeoObject call(LeoObject arg1, LeoObject arg2) {
	    VM vm = this.runtime.getActiveVM();
		if(this.bytecode.hasVarargs()) {
			switch(this.bytecode.getVarargIndex()) {				
				case 0: return vm.execute(env, this, this.bytecode, LeoArray.toLeoArray(arg1, arg2));
				case 1: return vm.execute(env, this, this.bytecode, arg1, LeoArray.toLeoArray(arg2));				
			}				
		}		
		return vm.execute(env, this, this.bytecode, arg1, arg2);
	}
	
	@Override
	public LeoObject call(LeoObject arg1, LeoObject arg2, LeoObject arg3) {
	    VM vm = this.runtime.getActiveVM();
		if(this.bytecode.hasVarargs()) {
			switch(this.bytecode.getVarargIndex()) {				
				case 0: return vm.execute(env, this, this.bytecode, LeoArray.toLeoArray(arg1, arg2, arg3));
				case 1: return vm.execute(env, this, this.bytecode, arg1, LeoArray.toLeoArray(arg2, arg3));
				case 2: return vm.execute(env, this, this.bytecode, arg1, arg2, LeoArray.toLeoArray(arg3));				
			}				
		}
		return vm.execute(env, this, this.bytecode, arg1, arg2, arg3);
	}
	
	@Override
	public LeoObject call(LeoObject arg1, LeoObject arg2, LeoObject arg3, LeoObject arg4) {
	    VM vm = this.runtime.getActiveVM();
		if(this.bytecode.hasVarargs()) {
			switch(this.bytecode.getVarargIndex()) {				
				case 0: return vm.execute(env, this, this.bytecode, LeoArray.toLeoArray(arg1, arg2, arg3, arg4));
				case 1: return vm.execute(env, this, this.bytecode, arg1, LeoArray.toLeoArray(arg2, arg3, arg4));
				case 2: return vm.execute(env, this, this.bytecode, arg1, arg2, LeoArray.toLeoArray(arg3, arg4));
				case 3: return vm.execute(env, this, this.bytecode, arg1, arg2, arg3, LeoArray.toLeoArray(arg4));
			}				
		}
		return vm.execute(env, this, this.bytecode, arg1, arg2, arg3, arg4);
	}
	
	@Override
	public LeoObject call(LeoObject arg1, LeoObject arg2, LeoObject arg3, LeoObject arg4, LeoObject arg5) {
	    VM vm = this.runtime.getActiveVM();
		if(this.bytecode.hasVarargs()) {
			switch(this.bytecode.getVarargIndex()) {				
				case 0: return vm.execute(env, this, this.bytecode, LeoArray.toLeoArray(arg1, arg2, arg3, arg4, arg5));
				case 1: return vm.execute(env, this, this.bytecode, arg1, LeoArray.toLeoArray(arg2, arg3, arg4, arg5));
				case 2: return vm.execute(env, this, this.bytecode, arg1, arg2, LeoArray.toLeoArray(arg3, arg4, arg5));
				case 3: return vm.execute(env, this, this.bytecode, arg1, arg2, arg3, LeoArray.toLeoArray(arg4, arg5));
				case 4: return vm.execute(env, this, this.bytecode, arg1, arg2, arg3, arg4, LeoArray.toLeoArray(arg5));
			}				
		}
		return vm.execute(env, this, this.bytecode, arg1, arg2, arg3, arg4, arg5);
	}
	
	@Override
	public LeoObject call(LeoObject[] args) {
	    VM vm = this.runtime.getActiveVM();
		if(this.bytecode.hasVarargs()) {
			int index = this.bytecode.getVarargIndex();
			LeoArray varargs =  LeoArray.toLeoArray( Arrays.copyOfRange(args, index, args.length) );
			LeoObject[] newArgs = new LeoObject[index+1];
			System.arraycopy(args, 0, newArgs, 0, index);
			newArgs[index] = varargs;
			
			return vm.execute(env, this, this.bytecode, newArgs);					
		}
		
		return vm.execute(env, this, this.bytecode, args);		
	}

	/* (non-Javadoc)
	 * @see leola.types.LeoObject#eq(leola.types.LeoObject)
	 */
	@Override
	public boolean $eq(LeoObject other) {
		boolean isEquals = (other == this);

		if ( !isEquals && other != null ) {
			if ( other.isOfType(LeoType.FUNCTION) ) {
				LeoFunction function = other.as();
				isEquals = function.getNumberOfArgs() == this.numberOfArgs;
			}
		}
		return isEquals;
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
	public static LeoFunction read(Leola runtime, LeoObject env, DataInput in) throws IOException {
		Bytecode bytecode = Bytecode.read(env, in);
		int nouters = in.readInt();
		
		LeoObject[] outers = new LeoObject[nouters];
		for(int i =0; i < nouters; i++) {
			outers[i] = LeoObject.read(env, in);
		}
		
		LeoFunction function = new LeoFunction(runtime, env, bytecode);	
		return function;
	}
}

