/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.lang;

import java.util.List;
import java.util.Map;

import leola.vm.Leola;
import leola.vm.exceptions.LeolaRuntimeException;
import leola.vm.lib.LeolaIgnore;
import leola.vm.lib.LeolaLibrary;
import leola.vm.lib.LeolaMethod;
import leola.vm.types.LeoArray;
import leola.vm.types.LeoInteger;
import leola.vm.types.LeoMap;
import leola.vm.types.LeoNamespace;
import leola.vm.types.LeoNull;
import leola.vm.types.LeoObject;
import leola.vm.types.LeoString;



/**
 * Base collections API
 * 
 * @author Tony
 *
 */
public class CollectionsLeolaLibrary implements LeolaLibrary {

	private Leola runtime;
	
	/* (non-Javadoc)
	 * @see leola.frontend.LeolaLibrary#init(leola.frontend.Leola)
	 */
	@LeolaIgnore
	public void init(Leola leola, LeoNamespace namespace) throws Exception {
		this.runtime = leola;
		runtime.putIntoNamespace(this, namespace);
//		leola.eval(CollectionsLeolaLibrary.class.getResourceAsStream("collections.leola"));
	}

	
	/**
	 * Repeats the function N times
	 * 
	 * @param n
	 * @param function
	 */
	public final void repeat(int n, LeoObject function){
		for(int i = 0; i < n; i++) {
			LeoObject result = this.runtime.execute(function, LeoInteger.valueOf(i));	
			if ( LeoObject.isTrue(result) ) {
				break;
			}
		}		
		
	}	
	
	/**
	 * returns the length of the array
	 *
	 * @param array
	 * @return
	 */
	public final int length(LeoObject array) {
		if ( array == null ) return 0;
		switch(array.getType()) {
			case NULL: return 0;
			case STRING: return ((LeoString)array.as()).getString().length();
			case ARRAY: return ((LeoArray)array.as()).size();
			case MAP: return ((LeoMap)array.as()).size();
			default: throw new IllegalArgumentException("Illegal Argument type: " + array);
		}				
	}

	/**
	 * Iterates through the array invoking the call back.
	 *
	 * @param array
	 * @param function
	 * @throws Exception
	 */
	public final LeoObject foreach(LeoObject array, LeoObject function) throws Exception {
		LeoObject r = LeoNull.LEONULL;
		if(array != null) {
			switch(array.getType()) {
				case ARRAY: {
					LeoArray a = array.as();
					List<LeoObject> list = a.getArray();
					int size = list.size();
			
	
					for(int i = 0; i < size; i++) {
						LeoObject result = this.runtime.execute(function, list.get(i));	
						if ( LeoObject.isTrue(result) ) {
							r = result;
							break;
						}
					}
					
					break;
				}
				case MAP: {
					LeoMap map = array.as();
	
					for(Map.Entry<LeoObject, LeoObject> entry : map.getMap().entrySet()) {
						LeoObject result = this.runtime.execute(function, entry.getKey(), entry.getValue() );	
						if ( LeoObject.isTrue(result) ) {
							r = result;
							break;
						}
					}
					
					break;
				}
				case STRING: {
					LeoString str = array.as();
					int size = str.length();				
					
					for(int i = 0; i < size; i++) {
						LeoObject result = this.runtime.execute(function, str.charAt(i) );	
						if ( LeoObject.isTrue(result) ) {
							r = result;
							break;
						}
					}
					
					break;
				}
				case GENERATOR: {
					while(true) {
						LeoObject generatorResult = this.runtime.execute(array);
						if(generatorResult == LeoNull.LEONULL) {
							break;
						}
						
						LeoObject result = this.runtime.execute(function, generatorResult);
						if ( LeoObject.isTrue(result) ) {
							r = result;
							break;
						}
					}
					
					break;

				}
				default: {					
				}
			}
		}
		return r;
	}

	/**
	 * Iterates through the array.
	 *
	 * @param interpreter
	 * @param start
	 * @param end
	 * @param function
	 * @throws Exception
	 */
	@LeolaMethod(alias="for")
	public void __for(LeoArray array, int start, int end, LeoObject function) throws Exception {
		List<LeoObject> list = array.getArray();

		if ( start < 0 || end > list.size()) {
			throw new LeolaRuntimeException("Invalid array index: " + start + " to " + end + "[Size of array: " + list.size() + "]");
		}

		for(int i = start; i < end; i++) {
			LeoObject result = this.runtime.execute(function, list.get(i));	
			if ( LeoObject.isTrue(result) ) {
				break;
			}
		}
		
	}

	/**
	 * Iterates through the array invoking the call back filling the
	 * array
	 *
	 * @param array
	 * @param function
	 * @return the supplied array
	 * @throws Exception
	 */
	public final LeoArray fill(LeoArray array, LeoObject function) throws Exception {
		List<LeoObject> list = array.getArray();
		int size = list.size();

		for(int i = 0; i < size; i++) {
			LeoObject result = this.runtime.execute(function, LeoInteger.valueOf(i));	
			list.set(i, result);
		}
		
		return array;
	}
	
	private void checkTypes(LeoObject start, LeoObject end) {
		if(start.getType() != end.getType()) {
			throw new IllegalArgumentException("Ranges do not match in type: " + start.getType() + " vs. " + end.getType());
		}
	}
	
	/**
	 * Constructs a list given the range.
	 * 
	 * @param start
	 * @param end
	 * @return
	 */
	public final LeoObject range(LeoObject start, LeoObject end) {
		checkTypes(start, end);
		
		LeoObject result = LeoNull.LEONULL;
		switch(start.getType()) {
			case LONG:
			case INTEGER: {
				
				int s = start.asInt();
				int e = end.asInt();
				
				LeoArray r = new LeoArray( Math.abs(e-s) );
				
				for(; s < e; s++) {
					r.add(LeoInteger.valueOf(s));
				}
				
				result = r;
				break;
			}		
			default: {				
			}
		}
		
		return result;
	}
	
	/**
	 * returns a sequence consisting of those items from the sequence for which function(item) is true
	 * 
	 * @param list (either an Array, Map or String)
	 * @param function
	 * @return
	 * @throws Exception
	 */
	public final LeoObject filter(LeoObject list, LeoObject function) throws Exception {
		LeoObject result = LeoNull.LEONULL;

		switch(list.getType()) {
			case STRING: {
				String str = list.toString();
				StringBuilder sb = new StringBuilder(str);
				
				int len = str.length();
				for(int i = 0; i < len; i++) {
					char c = str.charAt(i);
					
					LeoString ch = LeoString.valueOf( String.valueOf(c));
					if ( LeoObject.isTrue(this.runtime.execute(function, ch)) ) {						
						sb.append(c);
					}
				}
				
				result = LeoString.valueOf(sb.toString());
				
				break;
			}
			case ARRAY: {
				LeoArray array = list.as();					
				int size = array.size();
				
				LeoArray r = new LeoArray(size);
				for(int i = 0; i < size; i++) {
					LeoObject obj = array.get(i);
					if ( LeoObject.isTrue(this.runtime.execute(function, obj)) ) {						
						r.add(obj);
					}
				}
				
				result = r;
				
				break;
			}
			case MAP: {
				LeoMap map = list.as();					
				int size = map.bucketLength();
				
				LeoMap m = new LeoMap(size);
				for(int i = 0; i < size; i++) {
					LeoObject key = map.getKey(i);
					if(key==null) continue;
					
					LeoObject value = map.getValue(i);
					
					if ( LeoObject.isTrue(this.runtime.execute(function, key, value)) ) {
						m.put(key, value);
					}
				}
				
				result = m;
				
				break;
			}
			case GENERATOR: {
				
				result = new LeoArray();
				while(true) {
					LeoObject generatorResult = this.runtime.execute(list);
					if(generatorResult == LeoNull.LEONULL) {
						break;
					}
					
					if( LeoObject.isTrue(this.runtime.execute(function, generatorResult))) {
						result.$add(generatorResult);
					}
				}
				
				break;
			}
			default: {				
			}
		}		
		
		return result;
	}
	
	
	/**
	 * calls function(item) for each of the sequence’s items and returns a list of the return values.
	 * 
	 * @param list (either an Array or String)
	 * @param function
	 * @return
	 * @throws Exception
	 */
	public final LeoObject map(LeoObject list, LeoObject function) throws Exception {
		LeoObject result = LeoNull.LEONULL;

		switch(list.getType()) {
			case STRING: {
				String str = list.toString();
				StringBuilder sb = new StringBuilder(str);
				
				int len = str.length();
				for(int i = 0; i < len; i++) {
					char c = str.charAt(i);
					
					LeoString ch = LeoString.valueOf( String.valueOf(c));
					LeoObject r = this.runtime.execute(function, ch);
						
					sb.append(r.toString());					
				}
				
				result = LeoString.valueOf(sb.toString());
				
				break;
			}
			case ARRAY: {
				LeoArray array = list.as();					
				int size = array.size();
				
				LeoArray r = new LeoArray(size);
				for(int i = 0; i < size; i++) {
					LeoObject obj = array.get(i);
					LeoObject o = this.runtime.execute(function, obj);
						
					r.add(o);					
				}
				
				result = r;
				
				break;
			}
			case GENERATOR: {
				
				result = new LeoArray();
				while(true) {
					LeoObject generatorResult = this.runtime.execute(list);
					if(generatorResult == LeoNull.LEONULL) {
						break;
					}
					
					result.$add(this.runtime.execute(function, generatorResult));					
				}
				
				break;
			}
			default: {				
			}
		}		
		
		return result;
	}
	
	/**
	 * calls function(item) for each of the sequence’s items and returns a list of the return values.
	 * 
	 * @param list (either an Array, Map or String)
	 * @param function
	 * @return
	 * @throws Exception
	 */
	public final LeoObject reduce(LeoObject list, LeoObject function) throws Exception {
		LeoObject result = LeoNull.LEONULL;
			
		switch(list.getType()) {
			case ARRAY: {
				LeoArray array = list.as();					
				int size = array.size();
				switch(size) {
					case 0: break;
					case 1: result= array.get(0); break;
					default: {
						LeoObject prev = array.get(0);
						for(int i = 1; i < size; i++) {
							LeoObject obj = array.get(i);
							LeoObject o = this.runtime.execute(function, prev, obj);
							prev = o;
						}
						
						result = prev;		
					}
				}												
				break;
			}
			default: {				
			}
		}		
		
		return result;
	}
}
