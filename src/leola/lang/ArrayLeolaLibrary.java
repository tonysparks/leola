/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.lang;

import java.util.Collections;
import java.util.Comparator;

import leola.vm.Leola;
import leola.vm.lib.LeolaIgnore;
import leola.vm.lib.LeolaLibrary;
import leola.vm.types.LeoArray;
import leola.vm.types.LeoNamespace;
import leola.vm.types.LeoObject;

/**
 * Standard array library
 * 
 * @author Tony
 *
 */
public class ArrayLeolaLibrary implements LeolaLibrary {

	private Leola runtime;
	
	/* (non-Javadoc)
	 * @see leola.vm.lib.LeolaLibrary#init(leola.vm.Leola)
	 */
	@Override
	@LeolaIgnore
	public void init(Leola runtime, LeoNamespace namespace) throws Exception {
		this.runtime = runtime;
		this.runtime.putIntoNamespace(this, namespace);
	}

	
	public void foreach(LeoArray array, LeoObject function) {		
		int size = array.size();

		for(int i = 0; i < size; i++) {
			LeoObject result = this.runtime.execute(function, array.get(i) );	
			if ( LeoObject.isTrue(result) ) {
				break;
			}
		}
		
	}


	public void add(LeoArray array, LeoObject v) {
		array.$add(v);
	}
	public void addAll(LeoArray array, LeoObject values) {
		array.addAll(values);
	}
	
	public boolean remove(LeoArray array, LeoObject v) {
		return array.remove(v);
	}
	public void removeAll(LeoArray array, LeoObject v) {
		array.removeAll(v);
	}
	
	public LeoObject get(LeoArray array, int i) {
		return array.get(i);
	}
	
	public boolean has(LeoArray array, LeoObject v) {
		return array.has(v);
	}
	public LeoObject reverse(LeoArray array) {
		return array.reverse();
	}
	public LeoObject slice(LeoArray array, int start, int end) {
		return array.slice(start, end);
	}
	public LeoObject tail(LeoArray array, int start) {
		return array.tail(start);
	}
	public int size(LeoArray array) {
		return array.size();
	}
	public void clear(LeoArray array) {
		array.clear();
	}
	public LeoArray keep(LeoArray array, LeoArray others) {
		LeoArray copy = new LeoArray(array);
		copy.retainAll(others);
		return copy;
	}
	
	public void push(LeoArray array, LeoObject v) {
		array.push(v);
	}
	public LeoObject pop(LeoArray array) {
		return array.pop();
	}
	public LeoObject peek(LeoArray array) {
		return array.peek();
	}
	public boolean empty(LeoArray array) {
		return array.empty();
	}
	
	public LeoObject rest(LeoArray array) {
		return array.rest();
	}
	public LeoObject first(LeoArray array) {
		return array.first();
	}
	public LeoObject last(LeoArray array) {
		return array.last();
	}
	public LeoObject clone(LeoArray array) {
		return array.clone();
	}
	
	public LeoObject sort(LeoArray array, final LeoObject comparator) {
		Collections.sort(array, new Comparator<LeoObject>() {

			@Override
			public int compare(LeoObject o1, LeoObject o2) {
				LeoObject res = runtime.execute(comparator, o1, o2);
				return (Integer)LeoObject.toJavaObject(int.class, res);
			}
		});
		return array;
	}
}

