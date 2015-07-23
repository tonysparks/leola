/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.vm;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import leola.vm.types.LeoNamespace;
import leola.vm.types.LeoObject;

/**
 * Stores the {@link LeoNamespace}s
 * 
 * @author Tony
 *
 */
public class NamespaceDefinitions {

	private Map<LeoObject, LeoNamespace> namespaces;
	
	
    /**
     * Determine if there are any {@link LeoNamespace}'s defined
     * 
     * @return true if there are {@link LeoNamespace}'s defined
     */
	public boolean hasDefinitions() {
		return this.namespaces != null && ! this.namespaces.isEmpty();
	}
	
	/**
	 * @return the classDefinitions
	 */
	public Map<LeoObject, LeoNamespace> getNamespaceDefinitions() {
		if ( this.namespaces == null ) {
			this.namespaces = new ConcurrentHashMap<LeoObject, LeoNamespace>();
		}
		
		return namespaces;
	}
	

    /**
     * Stores the {@link LeoNamespace}
     * 
     * @param ns
     */
	public void storeNamespace(LeoNamespace ns) {
	    storeNamespace(ns.getName(), ns);
	}
	
	/**
	 * Stores the {@link LeoNamespace}, bounded to the supplied namespace name
	 * 
	 * @param namespaceName
	 * @param ns
	 */
	public void storeNamespace(LeoObject namespaceName, LeoNamespace ns) {
		getNamespaceDefinitions().put(namespaceName, ns);
	}
	
	/**
	 * Removes the {@link LeoNamespace} associated with the supplied name
	 * 
	 * @param namespaceName
	 */
	public void removeNamespace(LeoObject namespaceName) {
		getNamespaceDefinitions().remove(namespaceName);
	}
	
	/**
	 * Determines if there is a {@link LeoNamespace} associated with the supplied name.
	 * 
	 * @param namespace
	 * @return true if there is a {@link LeoNamespace} associated with the supplied name.
	 */
	public boolean containsNamespace(LeoObject namespace) {
		return getNamespaceDefinitions().containsKey(namespace);
	}
	
	
	/**
	 * Retrieves the {@link LeoNamespace} associated with the supplied name.
	 * @param namespace
	 * @return the {@link LeoNamespace} associated with the supplied name,
	 * or null if not bound.
	 */
	public LeoNamespace getNamespace(LeoObject namespace) {
		return getNamespaceDefinitions().get(namespace);
	}
}

