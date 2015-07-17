/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.vm.compiler;

import java.util.ArrayList;
import java.util.List;

import leola.vm.types.LeoInteger;
import leola.vm.types.LeoObject;
import leola.vm.types.LeoString;
import leola.vm.util.ArrayUtil;

/**
 * The {@link Constants} pool.  For each {@link Bytecode} (in Leola that translates to either a Class, Namespace or a Function) there 
 * exists a constants pool.  The pool stores literals such as strings and numbers.
 * 
 * @author Tony
 *
 */
public class Constants {

	/**
	 * Storage
	 */
	private List<LeoObject> storage;
	
	/**
	 */
	public Constants() {		
	}
	
	/**
	 * @return The size of the constant pool
	 */
	public int getNumberOfConstants() {
		return (this.storage != null ) ? this.storage.size() : 0;
	}
	
	/**
	 * @return the storage
	 */
	private List<LeoObject> lazystorage() {
		if ( this.storage == null ) {
			this.storage = new ArrayList<LeoObject>();
		}
		return storage;
	}
	
	/**
	 * Stores the string literal (converts it to a {@link LeoString}).
	 * @param value
	 * @return the constant index of where it's stored
	 */
	public int store(String value) {
		LeoObject str = LeoString.valueOf(value);
		return store(str);
	}
	
	/**
	 * Stores the number literal (converts it to a {@link LeoInteger}).
	 * @param value
	 * @return the constant index of where it's stored
	 */
	public int store(int n) {
		LeoInteger number = LeoInteger.valueOf(n);
		return store(number);
	}
	
	/**
	 * Stores a {@link LeoObject} literal
	 * 
	 * @param obj
	 * @return the constant index of where it's stored
	 */
	public int store(LeoObject obj) {
		if ( this.lazystorage().contains(obj) ) {
			return this.storage.indexOf(obj);
		}
		
		this.storage.add(obj);
		return this.storage.size() - 1;
	}
	
	/**
	 * Retrieves the index in which the supplied {@link LeoObject} is stored.
	 * @param obj
	 * @return the index (or -1 if not found) in where this object is stored in the pool
	 */
	public int get(LeoObject obj) {
		return this.storage == null ? -1 : this.storage.indexOf(obj);
	}
	
	/**
	 * Retrieves the {@link LeoObject} and a particular index.
	 * 
	 * @param index
	 * @return the {@link LeoObject} stored and the supplied index
	 */
	public LeoObject get(int index) {
		return this.storage == null ? null : this.storage.get(index);
	}
	
	/**
	 * @return compiles into an array of constants
	 */
	public LeoObject[] compile() {
	    LeoObject[] constants = ArrayUtil.EMPTY_LEOOBJECTS;
		if ( this.storage != null) {
			constants = this.storage.toArray(new LeoObject[this.storage.size()]);
		}
		return constants;
	}
}

