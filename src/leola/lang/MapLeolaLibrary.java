/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.lang;

import java.util.Map;

import leola.vm.Leola;
import leola.vm.exceptions.LeolaRuntimeException;
import leola.vm.lib.LeolaIgnore;
import leola.vm.lib.LeolaLibrary;
import leola.vm.types.LeoMap;
import leola.vm.types.LeoNamespace;
import leola.vm.types.LeoObject;

/**
 * Standard Map operations
 * 
 * @author Tony
 *
 */
public class MapLeolaLibrary implements LeolaLibrary {

	private Leola runtime;
	
	/* (non-Javadoc)
	 * @see leola.vm.lib.LeolaLibrary#init(leola.vm.Leola)
	 */
	@Override
	@LeolaIgnore
	public void init(Leola runtime, LeoNamespace namespace) throws LeolaRuntimeException {
		this.runtime = runtime;
		this.runtime.putIntoNamespace(this, namespace);		
	}
	
	
	public void foreach(LeoMap map, LeoObject function) {

		for(Map.Entry<LeoObject, LeoObject> entry : map.getMap().entrySet()) {
			LeoObject result = this.runtime.execute(function, entry.getKey(), entry.getValue() );	
			if ( LeoObject.isTrue(result) ) {
				break;
			}
		}
		
	}
	
	public void put(LeoMap map, LeoObject key, LeoObject value) {
		map.put(key, value);
	}
	public LeoObject remove(LeoMap map, LeoObject key) {
		return map.remove(key);
	}
	public LeoObject get(LeoMap map, LeoObject key) {
		return map.get(key);
	}
	public boolean has(LeoMap map, LeoObject key) {
		return map.has(key);
	}
	public void putAll(LeoMap map, LeoObject values) {
		map.putAllEntries(values);
	}
	public void removeAll(LeoMap map, LeoObject values) {
		map.removeAllEntries(values);
	}
	public int size(LeoMap map) {
		return map.size();
	}
	public boolean empty(LeoMap map) {
		return map.empty();
	}
	public void clear(LeoMap map) {
		map.clear();
	}
	public LeoObject keys(LeoMap map) {
		return map.keys();
	}
	public LeoObject values(LeoMap map) {
		return map.vals();
	}
	public LeoObject clone(LeoMap map) {
		return map.clone();
	}

}

