/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.vm.types;

import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

import leola.vm.VM;
import leola.vm.exceptions.LeolaRuntimeException;
import leola.vm.util.ClassUtil;
import leola.vm.util.Pair;


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
	private int numberOfArgs;

	private Class<?> clss;
	private Object instance;
	private LeoObject methodName;

	private Method method;

	private List<Method> overloads;
	
	
	/**
	 * @param clss
	 * @param instance
	 * @param methodName
	 * @param numberOfArgs
	 */
	public LeoNativeFunction(Class<?> clss, Object instance, String methodName, int numberOfArgs) {
	    this(clss, instance, LeoString.valueOf(methodName), numberOfArgs);
	}
	
	/**
	 * @param clss
	 * @param instance
	 * @param methodName
	 * @param numberOfArgs
	 */
	public LeoNativeFunction(Class<?> clss, Object instance, LeoObject methodName, int numberOfArgs) {
		super(LeoType.NATIVE_FUNCTION);
		
		this.clss = clss;
		this.instance = instance;
		this.methodName = methodName;
		this.numberOfArgs = numberOfArgs;		
	}
	
	public LeoNativeFunction(Method method, Object instance) {
		this(method.getDeclaringClass(), instance, method.getName(), method.getParameterTypes().length);
		
		setMethod(method);
	}
	
	public LeoNativeFunction(List<Method> overloads, Object instance) {
		this(overloads.get(0).getDeclaringClass(), instance, overloads.get(0).getName(), -1);
		
		this.overloads = overloads;
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

	/**
	 * @param method the method to set
	 */
	public void setMethod(Method method) {
		this.method = method;
		this.method.setAccessible(true);
	}
	
	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#call(leola.vm.VM)
	 */
	@Override
	public LeoObject call(VM vm) {
		return call();
	}
	
	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#call(leola.vm.VM, leola.vm.types.LeoObject)
	 */
	@Override
	public LeoObject call(VM vm, LeoObject arg1) {	
		return call(arg1);
	}
	
	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#call(leola.vm.VM, leola.vm.types.LeoObject, leola.vm.types.LeoObject)
	 */
	@Override
	public LeoObject call(VM vm, LeoObject arg1, LeoObject arg2) {	
		return call(arg1, arg2);
	}
	
	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#call(leola.vm.VM, leola.vm.types.LeoObject, leola.vm.types.LeoObject, leola.vm.types.LeoObject)
	 */
	@Override
	public LeoObject call(VM vm, LeoObject arg1, LeoObject arg2, LeoObject arg3) {	
		return call(arg1, arg2, arg3);
	}
	
	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#call(leola.vm.VM, leola.vm.types.LeoObject, leola.vm.types.LeoObject, leola.vm.types.LeoObject, leola.vm.types.LeoObject)
	 */
	@Override
	public LeoObject call(VM vm, LeoObject arg1, LeoObject arg2,
			LeoObject arg3, LeoObject arg4) {	
		return call(arg1, arg2, arg3, arg4);
	}
	
	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#call(leola.vm.VM, leola.vm.types.LeoObject, leola.vm.types.LeoObject, leola.vm.types.LeoObject, leola.vm.types.LeoObject, leola.vm.types.LeoObject)
	 */
	@Override
	public LeoObject call(VM vm, LeoObject arg1, LeoObject arg2,
			LeoObject arg3, LeoObject arg4, LeoObject arg5) {	
		return call(arg1, arg2, arg3, arg4, arg5);
	}
	
	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#call(leola.vm.VM, leola.vm.types.LeoObject[])
	 */
	@Override
	public LeoObject call(VM vm, LeoObject[] args) {	
		return call(args);
	}
	
	/**
	 * Invokes the function
	 * 
	 * @param vm
	 * @param args
	 * @return
	 */	
	public LeoObject call(LeoObject... args) {
		Object result = null;
		try {
			if ( this.overloads != null ) {
				for(Method m : this.overloads) {
					if(args!=null) {
						if ( m.getParameterTypes().length == args.length ) {
							result = ClassUtil.invokeMethod(m, this.instance, args);
							break;
						}
					}
					else {
						if ( m.getParameterTypes().length == 0 ) {
							result = ClassUtil.invokeMethod(m, this.instance, args);
							break;
						}
					}
				}
			}
			else if ( this.method == null ) {						
				Pair<Method, Object> pairResult = ClassUtil.invokeMethod(this.clss, this.methodName.toString(), this.instance, args);
								
				setMethod(pairResult.getFirst());
				result = pairResult.getSecond();
				
			}
			else {			
				result = ClassUtil.invokeMethod(this.method, this.instance, args);			
			}
		} 
		catch(LeolaRuntimeException e) {
			return e.getLeoError();
		}
		catch (Exception e) {
			return new LeoError(e.getMessage()); 
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

