/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.vm;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import leola.vm.lib.LeolaMethod;
import leola.vm.types.LeoMap;
import leola.vm.types.LeoNamespace;
import leola.vm.types.LeoNativeFunction;
import leola.vm.types.LeoObject;
import leola.vm.types.LeoString;
import leola.vm.util.ArrayUtil;
import leola.vm.util.ClassUtil;

/**
 * A {@link Scope} represents a lexical scope of variables
 *
 * @author Tony
 *
 */
public class Scope {

	private static final AtomicLong id = new AtomicLong();
	private String sid;

	private Symbols symbols;
	private Scope parent;

	private ClassDefinitions classDefinitions;
	private NamespaceDefinitions namespaceDefinitions;
	
	private LeoMap values;

	private Scope(Scope scope) {
		this.symbols = scope.symbols;
		this.parent = scope.parent;

		this.classDefinitions = scope.classDefinitions;
		this.namespaceDefinitions = scope.namespaceDefinitions;

		this.values = (LeoMap)scope.values.clone();

		this.sid = "C" + scope.sid;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.sid + " parent: " + ((this.getParent()!=null)?this.getParent():"-");
	}


	/**
	 * @param symbols
	 * @param parent
	 * @param scopeType
	 */
	public Scope(Symbols symbols, Scope parent) {
		this.symbols = symbols;
		this.parent = parent;

		this.sid = id.incrementAndGet() + "";
	}

	/**
	 * @return the symbols
	 */
	public Symbols getSymbols() {
		return symbols;
	}


	/**
	 * @param parent the parent to set
	 */
	public void setParent(Scope parent) {
		this.parent = parent;

	}


	/**
	 * @return true if there are {@link ClassDefinition}s in this {@link Scope}
	 */
	public boolean hasClassDefinitions() {
		return this.classDefinitions != null && this.classDefinitions.hasDefinitions();
	}

	/**
	 * @return the classDefinitions
	 */
	public ClassDefinitions getClassDefinitions() {
		if ( this.classDefinitions == null ) {
			this.classDefinitions = new ClassDefinitions();
		}
		return classDefinitions;
	}

	public boolean hasNamespaceDefinitions() {
		return this.namespaceDefinitions != null && this.namespaceDefinitions.hasDefinitions();
	}

	/**
	 * @return the namespaceDefinitions
	 */
	public NamespaceDefinitions getNamespaceDefinitions() {
		if(this.namespaceDefinitions == null) {
			this.namespaceDefinitions = new NamespaceDefinitions();
		}

		return namespaceDefinitions;
	}


	/**
	 * @return the underlying raw values of the {@link Scope}
	 */
	public LeoObject[] getScopedValues() {
		return (this.values != null) ? this.values.vals().getRawArray() : ArrayUtil.EMPTY_LEOOBJECTS;
	}

	/**
	 * Recursively attempts to retrieve the value associated with the reference.  If it
	 * isn't found in this scope, it will ask its parent scope.
	 * @param reference
	 * @return
	 */
	public LeoObject getObject(LeoString reference) {
		LeoObject value = (this.values != null) ? this.values.getWithJNull(reference) : null;
		if ( value == null && parent != null) {
			value = parent.getObject(reference);
		}

		return value;
	}

	public LeoObject getObjectNoGlobal(LeoString reference) {
		LeoObject value = (this.values != null) ? this.values.getWithJNull(reference) : null;
		if ( value == null && parent != null && !parent.isGlobalScope()) {
			value = parent.getObjectNoGlobal(reference);
		}

		return value;
	}

	/**
	 * Retrieves a {@link LeoNamespace}
	 * @param reference
	 * @return
	 */
	public LeoNamespace getNamespace(LeoString reference) {
//		LeoObject value = (this.values != null) ? this.values.getWithJNull(reference) : null;
//		if ( value == null && parent != null ) {
//			value = parent.getNamespace(reference);
//		}
//		else if ( (value != null && !value.isNamespace()) && parent != null) {
//			value = parent.getNamespace(reference);
//		}
//
//		return value!=null ? (LeoNamespace)value : null;

		LeoNamespace value = (hasNamespaceDefinitions()) ? this.namespaceDefinitions.getNamespace(reference.getString()) : null;
		if(value == null) {
			value = parent.getNamespace(reference);
		}
		return value;
	}

	/**
	 * Recursively attempts to retrieve the value associated with the reference.  If it
	 * isn't found in this scope, it will ask its parent scope.
	 * @see Scope#getObject(LeoString)
	 * @param reference
	 * @return
	 */
	public LeoObject getObject(String reference){
		return getObject(LeoString.valueOf(reference));
	}

	public LeoObject getObjectNoGlobal(String reference) {
		return getObjectNoGlobal(LeoString.valueOf(reference));
	}

	/**
	 * Stores an object in this scope and only this scope. This does
	 * not traverse the parent scopes to see if a value is already
	 * held.
	 * 
	 * @param reference
	 * @param value
	 * @return the previously held value, if any
	 */
	public LeoObject putObject(LeoString reference, LeoObject value) {
		if(this.values==null) {
			this.values = new LeoMap();
		}

		return this.values.put(reference, value);		
	}
		
	/**
	 * Stores an object in this scope, it first checks to see if any parent
	 * values contain the supplied reference, if it does it will override the existing
	 * value.  This is to account for class data members.
	 * 
	 * @param reference
	 * @param value
	 * @return the previously held value, if any
	 */
	public LeoObject storeObject(LeoString reference, LeoObject newValue) {
		
		Scope current = this;		
		while (current != null) {
			
			// if the value is the the current scope, break out 
			if (current.values != null && current.values.getWithJNull(reference) != null ) {
				break;
			}
			
			// else check the parent 
			if(current.parent != null && !current.parent.isGlobalScope()) {
				current = current.parent;
			}
			else {
				current = this;
				break;
			}
		}
		
		return current.putObject(reference, newValue);
	}

	public LeoObject storeObject(String reference, LeoObject value) {
		return storeObject(LeoString.valueOf(reference), value);
	}

	/**
	 * Removes an object from this {@link Scope}
	 *
	 * @param reference
	 * @return
	 */
	public LeoObject removeObject(LeoString reference) {
		return (this.values!=null) ? this.values.remove(reference) : null;
	}

	public LeoObject removeObject(String reference) {
		return removeObject(LeoString.valueOf(reference));
	}

	/**
	 * @return the number of {@link LeoObject}s in this {@link Scope}
	 */
	public int getNumberOfObjects() {
		return (this.values != null) ? this.values.size() : 0;
	}

	public LeoMap getRawObjects() {
		return this.values;
	}

	/**
	 * @return true if this scope is the global scope
	 */
	public boolean isGlobalScope() {
		return this == this.symbols.getGlobalScope();
	}

	/**
	 * @return the parent
	 */
	public Scope getParent() {
		return parent;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	public Scope clone() {
		Scope clone = new Scope(this);
		return clone;
	}

	/**
	 * Loads the objects methods into the supplied {@link Scope}
	 * @param scope
	 * @param jObject
	 */
	public void loadNatives(Object jObject) {
		Class<?> nClass = jObject.getClass();
		List<Method> methods = ClassUtil.getAllDeclaredMethods(nClass);
		for(Method m: methods) {
			LeoNativeFunction func = new LeoNativeFunction(nClass, jObject, m.getName(), m.getParameterTypes().length);
			if(m.isAnnotationPresent(LeolaMethod.class)) {
				storeObject(m.getAnnotation(LeolaMethod.class).alias(), func);
			}
			else {
				storeObject(m.getName(), func);
			}
		}
	}

	/**
	 * Loads the static methods of the native class into the supplied {@link Scope}
	 *
	 * @param scope
	 * @param aClass
	 */
	public void loadStatics(Class<?> aClass) {
		List<Method> methods = ClassUtil.getAllDeclaredMethods(aClass);
		for(Method m: methods) {
			LeoNativeFunction func = new LeoNativeFunction(aClass, null, m.getName(), m.getParameterTypes().length);
			boolean isStatic = (m.getModifiers() & Modifier.STATIC) != 0;

			if ( isStatic ) {
				if(m.isAnnotationPresent(LeolaMethod.class)) {
					storeObject(m.getAnnotation(LeolaMethod.class).alias(), func);
				}
				else {
					storeObject(m.getName(), func);
				}
			}
		}
	}
}
