/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.vm.types;

import java.lang.reflect.Method;
import java.util.Map;

import leola.vm.asm.Scope;
import leola.vm.lib.LeolaMethod;

/**
 * An object which contains code within it that can be referenced outside of the scope (namespaces, classes).
 * 
 * @author Tony
 *
 */
public abstract class LeoScopedObject extends LeoOuterObject {
		
	
	/**
	 * The scope
	 */
	private Scope scope;
		
	/**
	 * @param type
	 * @param scope
	 */
	public LeoScopedObject(LeoType type, Scope scope, int numberOfOuters) {
		super(type, numberOfOuters);
		setScope(scope);
	}
	
	/**
	 * @return the scope
	 */
	public Scope getScope() {
		return scope;
	}
	
	/**
	 * @param scope the scope to set
	 */
	public void setScope(Scope scope) {
		this.scope = scope;
	}
	
	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#isScopedObject()
	 */
	@Override
	public boolean isScopedObject() {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#eq(leola.vm.types.LeoObject)
	 */
	@Override
	public boolean $eq(LeoObject other) {
		if ( other == this) {
			return true;
		}
		
		if ( other instanceof LeoScopedObject ) {
			LeoScopedObject otherObj = (LeoScopedObject)other;			
			return otherObj.scope.equals(this.scope);
		}
		
		return false;
	}
	
	
	public LeoNativeFunction addMethod(Method method) {
		return addMethod(this, method);
	}
	
	public LeoNativeFunction addMethod(Object jObject, Method method) {
		return addMethod(jObject, method, (method.isAnnotationPresent(LeolaMethod.class)) ?
											method.getAnnotation(LeolaMethod.class).alias()
											: method.getName());
	}
	
	public LeoNativeFunction addMethod(Object jObject, Method method, String alias) {
		LeoNativeFunction fun = new LeoNativeFunction(method, jObject);
		this.scope.storeObject(alias, fun);
		
		return fun;
	}
	
	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#setObject(leola.vm.types.LeoObject, leola.vm.types.LeoObject)
	 */
	@Override
	public void setObject(LeoObject key, LeoObject value) {	
		addProperty(key.toLeoString(), value);
	}
	
	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#getObject(leola.vm.types.LeoObject)
	 */
	@Override
	public LeoObject getObject(LeoObject key) {	
		return getProperty(key);
	}
	
	public void put(String reference, LeoObject value) {
		this.addProperty(LeoString.valueOf(reference), value);
	}
	
	public boolean hasProperty(LeoObject member) {
		return this.scope.getObjectNoGlobal(member.toString()) != null;
	}
	
	/**
	 * Attempts to look up the data member with the supplied name.
	 * 
	 * @param member - the name of the property
	 * @return the property value if found, otherwise {@link LeoNull}
	 */
	public LeoObject getProperty(LeoObject member) {
		LeoObject result = this.scope.getObjectNoGlobal(member.toString());
		if(result == null) {
			result = LeoNull.LEONULL;
		}
		return result;
	}
	
		
	/**
	 * @return returns the property names, performance note, this is calculated
	 * new each time.
	 */
	public LeoArray getPropertyNames() {
		LeoMap map = this.scope.getRawObjects();
		return map.keys();
	}
	
	/**
	 * @return returns the property objects, performance note, this is calculated
	 * new each time, moreover any changes to the LeoArray are reflected on this scope. 
	 */
	public LeoArray getProperties() {
		return new LeoArray(this.scope.getScopedValues(), this.scope.getNumberOfObjects());
	}
	
	
	public void addProperty(LeoString member, LeoObject property) {
		this.scope.storeObject(member, property);
	}
	
	public void removeProperty(LeoString member) {
		this.scope.storeObject(member, LeoNull.LEONULL);
	}
	
	
	protected LeoObject lazyGetProperty(LeoObject member, Map<String, Method> methods) {
				
		String reference = member.toString();
		LeoObject result = scope.getObject(reference);
		if ( result == null ) {
			if ( methods.containsKey(reference) ) {
				result = addMethod(methods.get(reference));
			}
		}
		
		return result;
	}	
}

