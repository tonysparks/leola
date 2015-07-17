/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.vm;


import leola.vm.exceptions.LeolaRuntimeException;
import leola.vm.types.LeoNamespace;
import leola.vm.types.LeoObject;
import leola.vm.types.LeoString;


/**
 * Keeps track of all the symbols
 * 
 * @author Tony
 *
 */
public class Symbols {

	private Scope globalScope;
	private Scope currentScope;
	
	/**
	 */
	public Symbols() {
		this.globalScope = pushObjectScope();
	}
			
	/**
	 * Pushes a new {@link Scope} and pops it off right away.
	 * @return the new {@link Scope}
	 */
	public Scope newObjectScope() {
		Scope scope = pushObjectScope();
		popScope();
		
		return scope;
	}
	

	
	/**
	 * @return a new {@link Scope} 
	 */
	private Scope pushObjectScope() {
		return pushScope();
	}
		
	/**
	 * Pushes a new {@link Scope}
	 * @return the new {@link Scope}
	 */
	private Scope pushScope() {		
		Scope result = new Scope(this, peek());
		                								
		this.currentScope = result;
		return result;
	}
	
	/**
	 * Pops the current {@link Scope}
	 * @return the popped {@link Scope}
	 */
	private Scope popScope() {
		if ( this.currentScope == null ) {
			this.currentScope = peek(); /* default to global scope */
		}
		
		Scope poppedScope = this.currentScope;
		this.currentScope = poppedScope.getParent();
		return poppedScope;
	}
		
	/**
	 * Gets a {@link LeoObject} by reference
	 * 
	 * @param reference
	 * @return the object if found, otherwise null
	 */
	public LeoObject getObject(String reference) {				
		Scope scope = peek();				
		return (scope != null) ? scope.getObject(reference) : null;		
	}
	
		
	/**
	 * @return the current active {@link Scope}
	 */
	public Scope peek() {
		return this.currentScope == null ? this.globalScope : this.currentScope;
	}
	
	/**
	 * @return the globalScope
	 */
	public Scope getGlobalScope() {
		return globalScope;
	}
	
	
	/**
	 * Looks up a namespace.
	 *
	 * @param name
	 * @return
	 */
	private LeoNamespace lookupSimpleNamespace(String name) {
		LeoNamespace result = null;
		
		Scope scope = peek();
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
	public LeoNamespace lookupNamespace(String namespace) {
		LeoNamespace ns = null;

		String[] namespaces = namespace.replace(".", ":").split(":");
		if ( namespaces.length > 0 ) {
			ns = this.lookupSimpleNamespace(namespaces[0]);

			for(int i = 1; i < namespaces.length && ns != null; i++ ) {
				Scope scope = ns.getScope();
				if ( scope.hasNamespaceDefinitions() ) {
					ns = scope.getNamespaceDefinitions().getNamespace(namespaces[i]);
				}
			}
		}

		return ns;
	}

	   /**
     * Looks up the appropriate {@link ClassDefinitions} containing the className
     * @param className
     * @return the {@link ClassDefinitions} or null if not found
     */
    public ClassDefinitions lookupClassDefinitions(Scope currentScope, LeoObject className) {
        if ( className == null ) {
            throw new LeolaRuntimeException("Invalid class name, can not be empty!");
        }
        
        String jclassName = className.toString();
        LeoString lclassName = className.toLeoString();
    
        ClassDefinitions result = null;
        String formattedClassName = jclassName.replace(".", ":");
        int index = formattedClassName.lastIndexOf(':');
        if ( index > -1 ) {
            String namespace = formattedClassName.substring(0, index);
            LeoNamespace ns = lookupNamespace(namespace);
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
	 * Looks up the appropriate {@link ClassDefinitions} containing the className
	 * @param className
	 * @return the {@link ClassDefinitions} or null if not found
	 */
	public ClassDefinitions lookupClassDefinitions(LeoObject className) {
		return lookupClassDefinitions(peek(), className);
	}
	
	/**
	 * Gets just the class name, removing any package or namespaces.
	 * 
	 * @param fullyQualifiedClassName
	 * @return
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
	
	
	private ClassDefinitions checkScopeForDefinitions(Scope scope, LeoString justClassName) {
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

