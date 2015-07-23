/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.vm.types;

import java.lang.reflect.Method;
import java.util.Map;

import leola.vm.NamespaceDefinitions;
import leola.vm.Scope;
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
		this.scope = scope;
	}
	
	/**
	 * @return the scope
	 */
	public Scope getScope() {
		return scope;
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
	
	/**
     * @return the {@link NamespaceDefinitions} within this scope
     */
    public NamespaceDefinitions getNamespaceDefinitions() {
        return getScope().getNamespaceDefinitions();
    }
	
   
    /**
     * Adds the Java method into this {@link LeoScopedObject}
     * 
     * @param method
     * @return the {@link LeoNativeFunction} that represents the Java method
     */
	public LeoNativeFunction addMethod(Method method) {
		return addMethod(this, method);
	}
	
	/**
	 * Adds the Java method into this {@link LeoScopedObject}, if the {@link LeolaMethod} annotation is present on the supplied
	 * {@link Method}, the {@link LeolaMethod#alias()} will be used as the method alias name.
	 * 
	 * @param jObject
	 * @param method
	 * @return the {@link LeoNativeFunction} that represents the Java method
	 */
	public LeoNativeFunction addMethod(Object jObject, Method method) {
		return addMethod(jObject, method, (method.isAnnotationPresent(LeolaMethod.class)) ?
											method.getAnnotation(LeolaMethod.class).alias()
											: method.getName());
	}
	
	/**
	 * Adds a native Java method into this {@link LeoScopedObject}
	 * 
	 * @param jObject the Java instance
	 * @param method the Java method
	 * @param alias an alias name for the Java method
	 * @return the {@link LeoNativeFunction} that represents the Java method
	 */
	public LeoNativeFunction addMethod(Object jObject, Method method, String alias) {
		LeoNativeFunction fun = new LeoNativeFunction(method, jObject);
		this.scope.storeObject(alias, fun);
		
		return fun;
	}
	
	/** 
	 * @see LeoScopedObject#addProperty(LeoString, LeoObject)
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
	
	/**
	 * Places the data member in this scope.  This is different than
	 * {@link LeoScopedObject#setObject(LeoObject, LeoObject)} in that the latter
	 * from will scan the parent scopes to determine if it should override a data member,
	 * as this function will not, it will always place it in this {@link Scope}
	 * 
	 * @param reference
	 * @param value
	 */
	public void putObject(String reference, LeoObject value) {
	    putObject(LeoString.valueOf(reference), value); 
	}
	
	
	/**
     * Places the data member in this scope.  This is different than
     * {@link LeoScopedObject#setObject(LeoObject, LeoObject)} in that the latter
     * from will scan the parent scopes to determine if it should override a data member,
     * as this function will not, it will always place it in this {@link Scope}
     * 
     * @param reference
     * @param value
     */
	public void putObject(LeoObject reference, LeoObject value) {
	    this.scope.putObject(reference, value);
	}
	
	
	/**
	 * Determines if a data member exists in this {@link Scope} or parent scopes
	 * 
	 * @param member
	 * @return true if found within the hierarchy of this {@link Scope}.  This will always
	 * exclude the global {@link Scope}
	 */
	public boolean hasProperty(LeoObject member) {
		return this.scope.getObjectNoGlobal(member) != null;
	}
	
	/**
	 * Attempts to look up the data member with the supplied name.
	 * 
	 * @param member - the name of the property
	 * @return the property value if found, otherwise {@link LeoNull}
	 */
	public LeoObject getProperty(LeoObject member) {
		LeoObject result = this.scope.getObjectNoGlobal(member);
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
	
	/**
	 * Adds a data member to this {@link LeoScopedObject}
	 * 
	 * @param member
	 * @param property
	 */
	public void addProperty(LeoObject member, LeoObject property) {
		this.scope.storeObject(member, property);
	}
	
	
	/**
	 * Removes the data member from this {@link LeoScopedObject}
	 * @param member
	 */
	public void removeProperty(LeoObject member) {
		this.scope.removeObject(member);
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

