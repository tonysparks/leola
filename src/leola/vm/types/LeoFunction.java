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

import leola.vm.VM;
import leola.vm.asm.Bytecode;
import leola.vm.asm.Symbols;


/**
 * A {@link LeoFunction} is a function or better known as a Closure.
 *
 * @author Tony
 *
 */
public class LeoFunction extends LeoOuterObject {


	
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
	 * @param type
	 * @param numberOfArgs
	 * @param body
	 */
	public LeoFunction(LeoObject env, Bytecode bytecode) {
		this(LeoType.FUNCTION, env, bytecode);				
	}
	
	
	/**
	 * @param type
	 * @param env
	 * @param bytecode
	 */
	protected LeoFunction(LeoType type, LeoObject env, Bytecode bytecode) {
		super(type, bytecode.numOuters);
		
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
		if(this.bytecode.isVarargs) {
			switch(this.bytecode.getVarargIndex()) {
				case 0: return vm.execute(env, this, this.bytecode, LeoArray.toLeoArray(arg1));
			}
		}
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
		if(this.bytecode.isVarargs) {
			switch(this.bytecode.getVarargIndex()) {				
				case 0: return vm.execute(env, this, this.bytecode, LeoArray.toLeoArray(arg1, arg2));
				case 1: return vm.execute(env, this, this.bytecode, arg1, LeoArray.toLeoArray(arg2));				
			}				
		}		
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
		if(this.bytecode.isVarargs) {
			switch(this.bytecode.getVarargIndex()) {				
				case 0: return vm.execute(env, this, this.bytecode, LeoArray.toLeoArray(arg1, arg2, arg3));
				case 1: return vm.execute(env, this, this.bytecode, arg1, LeoArray.toLeoArray(arg2, arg3));
				case 2: return vm.execute(env, this, this.bytecode, arg1, arg2, LeoArray.toLeoArray(arg3));				
			}				
		}
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
		if(this.bytecode.isVarargs) {
			switch(this.bytecode.getVarargIndex()) {				
				case 0: return vm.execute(env, this, this.bytecode, LeoArray.toLeoArray(arg1, arg2, arg3, arg4));
				case 1: return vm.execute(env, this, this.bytecode, arg1, LeoArray.toLeoArray(arg2, arg3, arg4));
				case 2: return vm.execute(env, this, this.bytecode, arg1, arg2, LeoArray.toLeoArray(arg3, arg4));
				case 3: return vm.execute(env, this, this.bytecode, arg1, arg2, arg3, LeoArray.toLeoArray(arg4));
			}				
		}
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
		if(this.bytecode.isVarargs) {
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
	
	/**
	 * Invokes the function
	 * 
	 * @param vm
	 * @param args
	 * @return
	 */
	@Override
	public LeoObject call(VM vm, LeoObject[] args) {
		if(this.bytecode.isVarargs) {
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
	public static LeoFunction read(LeoObject env, Symbols symbols, DataInput in) throws IOException {
		Bytecode bytecode = Bytecode.read(env, symbols, in);
		int nouters = in.readInt();
		
		LeoObject[] outers = new LeoObject[nouters];
		for(int i =0; i < nouters; i++) {
			outers[i] = LeoObject.read(env, symbols, in);
		}
		
		LeoFunction function = new LeoFunction(env, bytecode);	
		return function;
	}
}

