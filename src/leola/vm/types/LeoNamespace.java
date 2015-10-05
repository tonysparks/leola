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

import leola.vm.Leola;
import leola.vm.Scope;
import leola.vm.compiler.Bytecode;
import leola.vm.compiler.Outer;
import leola.vm.lib.LeolaMethod;
import leola.vm.util.ClassUtil;


/**
 * Defines a namespace.
 *
 * @author Tony
 *
 */
public class LeoNamespace extends LeoScopedObject {

	/**
	 * The namespaces name
	 */
	private LeoObject name;

	/**
	 * @param scope
	 * @param name
	 */
	public LeoNamespace(Scope scope, LeoObject name) {
		this(null, null, scope, name);
	}

	/**
	 * @param runtime
	 * @param code
	 * @param scope
	 * @param name
	 */
	public LeoNamespace(Leola runtime, Bytecode code, Scope scope, LeoObject name) {
		super(LeoType.NAMESPACE, scope, (code !=null ) ? code.numOuters : 0);

		this.name = name;

		addProperty(LeoString.valueOf("this"), this);

		// NOTE, we can't execute the bytecode here because
		// the outers will not be set at this point
//		if ( code != null && runtime != null ) {
//			runtime.execute(this, code);
//		}
	}

	
	/**
	 * Stores the Java Object into this {@link LeoNamespace}
	 * 
	 * @param jObject
	 */
	public void store(Object jObject) {
		Scope scope = getScope();

		Class<?> nClass = jObject.getClass();
		List<Method> methods = ClassUtil.getAllDeclaredMethods(nClass);
		for(Method m: methods) {
			LeoNativeFunction func = new LeoNativeFunction(m, jObject);
			if(m.isAnnotationPresent(LeolaMethod.class)) {
				scope.storeObject(m.getAnnotation(LeolaMethod.class).alias(), func);
			}
			else {
				scope.storeObject(m.getName(), func);
			}
		}
	}

	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#toString()
	 */
	@Override
	public String toString() {
		return String.format("[ %s @ %s ]", this.getType(), this.name);
	}

	/**
	 * @return the name
	 */
	public LeoObject getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#isNamespace()
	 */
	@Override
	public boolean isNamespace() {
		return true;
	}

	/**
	 * Overrides the previous outers
	 * @param outers
	 */
	public void setOuters(Outer[] outers) {
		this.outers = outers;
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
	 * @see leola.types.LeoObject#eq(leola.types.LeoObject)
	 */
	@Override
	public boolean $eq(LeoObject other) {
		boolean isEquals = (other == this);

		if ( !isEquals && other != null ) {
			if ( other.isOfType(LeoType.NAMESPACE)) {
				LeoNamespace ns = other.as();
				isEquals = this.name.equals(ns.name);
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

	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#write(java.io.DataOutput)
	 */
	@Override
	public void write(DataOutput out) throws IOException {
	}
}

