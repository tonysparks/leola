/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.vm;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import leola.vm.exceptions.LeolaRuntimeException;
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
	
	/**
	 * The parent scope
	 */
	private Scope parent;

	
	/**
	 * Any class definitions in this 
	 * scope
	 */
	private ClassDefinitions classDefinitions;
	
	/**
	 * Any namespace definitions in this 
	 * scope
	 */
	private NamespaceDefinitions namespaceDefinitions;
	
	
	/**
	 * The values stored in this scope
	 */
	private LeoMap values;

	/**
	 * @param symbols
	 * @param parent
	 * @param scopeType
	 */
	public Scope(Scope parent) {
		this.parent = parent;
	}

	
	/**
	 * This will clear out (i.e., remove) all data elements from this {@link Scope}.
	 * 
	 * <p>
	 * This includes:
	 * <ul>
	 *     <li>The {@link ClassDefinitions}</li>
	 *     <li>The {@link NamespaceDefinitions}</li>
	 *     <li>Invokes {@link Scope#clear()} on the <code>parent</code> scope of this Scope</li>
	 *     <li>Any bound variables within this Scope</li>
	 * </ul>
	 * As the name implies, this does remove all allocated objects which will become garbage and 
	 * there before be collected by the JVM GC.  Use this method with caution.
	 */
	public void clear() {
	    if(hasClassDefinitions()) {
	        this.classDefinitions.clearDefinitions();	        
	    }
	    
	    if(hasNamespaceDefinitions()) {
	        this.namespaceDefinitions.clearDefinitions();
	    }
	    
	    if(hasParent()) {
	        this.parent.clear();
	    }
	    	    
	    if(hasObjects()) {
	        /* Create a new Set here so that we don't get caught in an infinite recursive loop if
	         * the scopes are self referencing
	         */
	        Set<Map.Entry<LeoObject, LeoObject>> entrySet = new HashSet<Map.Entry<LeoObject,LeoObject>>(this.values.entrySet());
	        this.values.clear();
	        
	        for(Map.Entry<LeoObject, LeoObject> entry : entrySet) {
	            Scope keyScope = entry.getKey().getScope();
	            if(keyScope != null) {
	                keyScope.clear();
	            }
	            
	            Scope valueScope = entry.getValue().getScope();
	            if(valueScope != null) {
	                valueScope.clear();
	            }
	        }
	    }
	}
	
	/**
	 * @return true if this {@link Scope} has a parent {@link Scope} defined.
	 */
	public boolean hasParent() {
	    return this.parent != null;
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

	/**
	 * @return if this scope has {@link NamespaceDefinitions}
	 */
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
	 * 
	 * @param reference
	 * @return the value if found, otherwise null
	 */
	public LeoObject getObject(LeoObject reference) {
		LeoObject value = (this.values != null) ? this.values.getWithJNull(reference) : null;
		if ( value == null && parent != null) {
			value = parent.getObject(reference);
		}

		return value;
	}

    /**
     * Recursively attempts to retrieve the value associated with the reference.  If it
     * isn't found in this scope, it will ask its parent scope.
     * 
     * @see Scope#getObject(LeoString)
     * @param reference
     * @return the LeoObject that is linked to the reference, if not found null is returned
     */
    public LeoObject getObject(String reference){
        return getObject(LeoString.valueOf(reference));
    }
    
	/**
	 * Searches scopes and parent scopes up and until the global scope
	 * 
	 * @param reference
	 * @return the value if found, otherwise null;
	 */
	public LeoObject getObjectNoGlobal(LeoObject reference) {
		LeoObject value = (this.values != null) ? this.values.getWithJNull(reference) : null;
		if ( value == null && parent != null && !parent.isGlobalScope()) {
			value = parent.getObjectNoGlobal(reference);
		}

		return value;
	}

	/**
     * Searches scopes and parent scopes up and until the global scope
     * 
     * @param reference
     * @return the value if found, otherwise null;
     */
	public LeoObject getObjectNoGlobal(String reference) {
	    return getObjectNoGlobal(LeoString.valueOf(reference));
	}
	
	/**
	 * Retrieves a {@link LeoNamespace} by its name
	 * 
	 * @param reference
	 * @return the {@link LeoNamespace} if found, otherwise null
	 */
	public LeoNamespace getNamespace(LeoObject reference) {
		LeoNamespace value = (hasNamespaceDefinitions()) ? this.namespaceDefinitions.getNamespace(reference) : null;
		if(value == null && parent != null) {
			value = parent.getNamespace(reference);
		}
		return value;
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
	public LeoObject putObject(LeoObject reference, LeoObject value) {
		if(this.values==null) {
			this.values = new LeoMap();
		}

		return this.values.put(reference, value);		
	}
	
	public LeoObject putObject(String reference, LeoObject value) {
	    return putObject(LeoString.valueOf(reference), value);
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
	public LeoObject storeObject(LeoObject reference, LeoObject newValue) {
		
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
	 * @return the {@link LeoObject} previously held by the reference (or null if no value was held
	 * by this reference).
	 */
	public LeoObject removeObject(LeoObject reference) {
		return (this.values!=null) ? this.values.remove(reference) : null;
	}

	
	/**
     * Removes an object from this {@link Scope}
     *
     * @param reference
     * @return the {@link LeoObject} previously held by the reference (or null if no value was held
     * by this reference).
     */
	public LeoObject removeObject(String reference) {
		return removeObject(LeoString.valueOf(reference));
	}

	/**
	 * @return true if and only if there are stored objects in this {@link Scope}
	 */
	public boolean hasObjects() {
	    return (this.values != null) && !this.values.isEmpty();
	}
	
	/**
	 * @return the number of {@link LeoObject}s in this {@link Scope}
	 */
	public int getNumberOfObjects() {
		return (this.values != null) ? this.values.size() : 0;
	}

	
	/**
	 * Retrieves the raw {@link LeoMap} that contains the reference and {@link LeoObject} associations
	 * 
	 * @return the {@link LeoMap} of the references and values
	 */
	public LeoMap getRawObjects() {
		return this.values;
	}

	/**
	 * @return true if this scope is the global scope
	 */
	public boolean isGlobalScope() {
	    return parent == null;
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
		Scope clone = new Scope(this.parent);
		clone.classDefinitions = this.classDefinitions;
	    clone.namespaceDefinitions = this.namespaceDefinitions;

	    clone.values = (LeoMap)this.values.clone();
		return clone;
	}

	/**
	 * Loads the objects methods into the supplied {@link Scope}
	 * 
	 * @param jObject
	 */
    public void loadNatives(Object jObject) {
        Class<?> aClass = jObject.getClass();
        loadClass(aClass, jObject, false);
    }

	/**
	 * Loads the static methods of the native class into the supplied {@link Scope}
	 *
	 * @param aClass
	 */
	public void loadStatics(Class<?> aClass) {
		loadClass(aClass, null, true);
	}
	
	/**
	 * Loads the class methods into this {@link Scope}
	 * 
	 * @param aClass the class to look for methods
	 * @param jObject the instance object (may be null)
	 * @param onlyStatics if we should only be loading static
	 */
    private void loadClass(Class<?> aClass, Object jObject, boolean onlyStatics) {
        List<Method> methods = ClassUtil.getAllDeclaredMethods(aClass);

        /*
         * first join all the functions with the same name (group the overloaded
         * functions)
         */
        Map<String, List<Method>> grouped = new HashMap<String, List<Method>>();
        for (Method m : methods) {
            boolean isStatic = (m.getModifiers() & Modifier.STATIC) != 0;
            if (isStatic || !onlyStatics) {
                if (!grouped.containsKey(m.getName())) {
                    grouped.put(m.getName(), new ArrayList<Method>());
                }

                grouped.get(m.getName()).add(m);

            }
        }

        /*
         * Now iterate over the grouped functions to add them to the scoped
         * object
         */
        for (List<Method> overloads : grouped.values()) {
            if (!overloads.isEmpty()) {
                Method m = overloads.get(0);
                LeoNativeFunction func = new LeoNativeFunction(overloads, jObject);

                if (m.isAnnotationPresent(LeolaMethod.class)) {
                    storeObject(m.getAnnotation(LeolaMethod.class).alias(), func);
                }
                else {
                    storeObject(m.getName(), func);
                }
            }
        }
    }
	
	/**
     * Looks up a namespace.
     *
     * @param name
     * @return
     */
    private LeoNamespace lookupSimpleNamespace(LeoObject name) {
        LeoNamespace result = null;
        
        Scope scope = this;
        while(scope != null) {
            if(scope.hasNamespaceDefinitions()) {
                NamespaceDefinitions ndefs = scope.getNamespaceDefinitions();
                result = ndefs.getNamespace(name);
                if ( result != null ) {                 
                    break;
                }
            }
            
            scope = scope.getParent();
        }
        
        return (result);
    }

    /**
     * Looks up name spaces (Ex. io:net:sytem:etc) going from the first namespace to the last (with
     * each namespace defining the next namespace.)
     *
     * @param namespace
     * @return the bottom most namespace
     */
    public LeoNamespace lookupNamespace(LeoObject namespace) {
        LeoNamespace ns = null;

        String[] namespaces = namespace.toString().replace(".", ":").split(":");
        if ( namespaces.length > 0 ) {
            ns = this.lookupSimpleNamespace(LeoString.valueOf(namespaces[0]));

            for(int i = 1; i < namespaces.length && ns != null; i++ ) {
                Scope scope = ns.getScope();
                if ( scope.hasNamespaceDefinitions() ) {
                    ns = scope.getNamespaceDefinitions().getNamespace(LeoString.valueOf(namespaces[i]));
                }
            }
        }

        return ns;
    }

    /**
     * Looks up the appropriate {@link ClassDefinitions} containing the className
     * 
     * @param currentScope
     * @param className
     * @return the {@link ClassDefinitions} or null if not found
     */
    public ClassDefinitions lookupClassDefinitions(Scope currentScope, LeoObject className) {
        if ( className == null ) {
            throw new LeolaRuntimeException("InvalidClassNameError: The class name can not be empty!");
        }
        
        String jclassName = className.toString();
        LeoObject lclassName = className;
    
        ClassDefinitions result = null;
        String formattedClassName = jclassName.replace(".", ":");
        int index = formattedClassName.lastIndexOf(':');
        if ( index > -1 ) {
            String namespace = formattedClassName.substring(0, index);
            LeoNamespace ns = lookupNamespace(LeoString.valueOf(namespace));
            if( ns != null ) {
                String justClassName = formattedClassName.substring(index + 1);
    
                Scope scope = ns.getScope();
                result = checkScopeForDefinitions(scope, LeoString.valueOf(justClassName));
            }
        }
        else {
            Scope scope = currentScope;
            while(scope != null) {
                result = checkScopeForDefinitions(scope, lclassName);
                if ( result != null ) {
                    break;
                }
                
                scope = scope.getParent();
            }
        }

        return (result);
    }
    
    /**
     * Looks up the appropriate {@link ClassDefinitions} containing the className.
     * 
     * @param className
     * @return the {@link ClassDefinitions} or null if not found
     */
    public ClassDefinitions lookupClassDefinitions(LeoObject className) {
        return lookupClassDefinitions(this, className);
    }
    
    /**
     * Gets just the class name, removing any package or namespaces.
     * 
     * @param fullyQualifiedClassName
     * @return just the class name (the simple class name).
     */
    public LeoObject getClassName(LeoObject fullyQualifiedClassName) {
        return LeoString.valueOf(getClassName(fullyQualifiedClassName.toString()));
    }
    
    
    /**
     * Gets just the class name, removing any package or namespaces.
     * 
     * @param fullyQualifiedClassName
     * @return just the class name (the simple class name).
     */
    public String getClassName(String fullyQualifiedClassName) {
        String result = fullyQualifiedClassName;
        
        String formattedClassName = fullyQualifiedClassName.replace(".", ":");
        int index = formattedClassName.lastIndexOf(':');
        if ( index > -1 ) {         
            String justClassName = formattedClassName.substring(index + 1);
            result = justClassName;
        }       
        
        return result;
    }
    
    
    private ClassDefinitions checkScopeForDefinitions(Scope scope, LeoObject justClassName) {
        ClassDefinitions result = null;

        if ( scope.hasClassDefinitions() ) {
            ClassDefinitions defs = scope.getClassDefinitions();
            if ( defs.containsClass(justClassName) ) {
                result = defs;
            }
        }

        return result;      
    }
}

