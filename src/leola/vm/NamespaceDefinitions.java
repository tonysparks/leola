/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.vm;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import leola.vm.types.LeoNamespace;

/**
 * Stores the {@link LeoNamespace}s
 * 
 * @author Tony
 *
 */
public class NamespaceDefinitions {

	private Map<String, LeoNamespace> namespaces;
	
	public boolean hasDefinitions() {
		return this.namespaces != null && ! this.namespaces.isEmpty();
	}
	
	/**
	 * @return the classDefinitions
	 */
	public Map<String, LeoNamespace> getNamespaceDefinitions() {
		if ( this.namespaces == null ) {
			this.namespaces = new ConcurrentHashMap<String, LeoNamespace>();
		}
		
		return namespaces;
	}
	
	public void storeNamespace(String namespace, LeoNamespace ns) {
		getNamespaceDefinitions().put(namespace, ns);
	}
	
	public void removeNamespace(String namespace) {
		getNamespaceDefinitions().remove(namespace);
	}
	
	public boolean containsNamespace(String namespace) {
		return getNamespaceDefinitions().containsKey(namespace);
	}
	
	public LeoNamespace getNamespace(String namespace) {
		return getNamespaceDefinitions().get(namespace);
	}
}

