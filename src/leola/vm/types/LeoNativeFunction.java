/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.vm.types;

import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import leola.vm.exceptions.LeolaRuntimeException;
import leola.vm.util.ClassUtil;


/**
 * A {@link LeoNativeFunction} is a function or better known as a Closure.
 *
 * @author Tony
 *
 */
public class LeoNativeFunction extends LeoObject {

	/**
	 * Arguments
	 */
	private final int numberOfArgs;

	private final Class<?> clss;
	private final Object instance;
	private final LeoObject methodName;

	private final List<Method> overloads;
	
	/**
	 * @param overloads
	 * @param instance
	 */
	public LeoNativeFunction(List<Method> overloads, Object instance) {
		super(LeoType.NATIVE_FUNCTION);
		
		if(overloads.isEmpty()) {
		    LeoObject.throwNativeMethodError("No native Java methods defined");
		}
		
		Method base = overloads.get(0);
		
		this.clss = base.getDeclaringClass();
		this.methodName = LeoString.valueOf(base.getName());
		
		this.instance = instance;
		this.overloads = overloads;
		if(overloads.size() > 1) {
		    this.numberOfArgs = -1;
		}
		else {
		    this.numberOfArgs = base.getParameterTypes().length;
		}
	}
	    
	/**
	 * @param method
	 * @param instance
	 */
    public LeoNativeFunction(Method method, Object instance) {
        this(Arrays.asList(method), instance);      
    }
	
	/**
	 * @return the clss
	 */
	public Class<?> getOwnerClass() {
		return clss;
	}
	
	/**
	 * @return the instance
	 */
	public Object getInstance() {
		return instance;
	}
	
	/**
	 * @return the methodName
	 */
	public LeoObject getMethodName() {
		return methodName;
	}
	

	/* (non-Javadoc)
	 * @see leola.types.LeoObject#isFunction()
	 */
	@Override
	public boolean isFunction() {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see leola.types.LeoObject#isNativeFunction()
	 */
	@Override
	public boolean isNativeFunction() {
		return true;
	}
			
	/**
	 * @return the numberOfArgs
	 */
	public int getNumberOfArgs() {
		return numberOfArgs;
	}
	
	@Override
	public LeoObject call() {
		return nativeCall();
	}
	
	@Override
	public LeoObject call(LeoObject arg1) {	
		return nativeCall(arg1);
	}
	
	@Override
	public LeoObject call(LeoObject arg1, LeoObject arg2) {	
		return nativeCall(arg1, arg2);
	}
	
	@Override
	public LeoObject call(LeoObject arg1, LeoObject arg2, LeoObject arg3) {	
		return nativeCall(arg1, arg2, arg3);
	}
	
	@Override
	public LeoObject call(LeoObject arg1, LeoObject arg2,
			LeoObject arg3, LeoObject arg4) {	
		return nativeCall(arg1, arg2, arg3, arg4);
	}
	
	@Override
	public LeoObject call(LeoObject arg1, LeoObject arg2,
			LeoObject arg3, LeoObject arg4, LeoObject arg5) {	
		return nativeCall(arg1, arg2, arg3, arg4, arg5);
	}
	
	@Override
	public LeoObject call(LeoObject[] args) {	
		return nativeCall(args);
	}
	
	/**
	 * Invokes the native function using Java reflection.
	 * 
	 * @param args
	 * @return the result of the function invocation
	 */	
	public LeoObject nativeCall(LeoObject... args) {
		Object result = null;
		try {		    
		    result = ClassUtil.invokeMethod(this.overloads, this.instance, args);			
		} 
		catch(LeolaRuntimeException e) {
			//throw e;
		    return ((LeolaRuntimeException)e).getLeoError();
		}
		catch (Exception e) {
			//LeoObject.rethrow(e);
		    return new LeoError(e);
		}
		
		return LeoObject.valueOf(result);
	}

	/* (non-Javadoc)
	 * @see leola.types.LeoObject#eq(leola.types.LeoObject)
	 */
	@Override
	public boolean $eq(LeoObject other) {
		boolean isEquals = (other == this);

		if ( !isEquals && other != null ) {
			if ( other.isOfType(LeoType.FUNCTION) ) {
				LeoNativeFunction function = other.as();
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
		out.write(this.getType().ordinal());
		out.writeBytes(this.methodName.toString());
		out.writeBytes(this.clss.getName());
		out.writeInt(this.numberOfArgs);
	}
}

