/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.lang;

import leola.vm.Leola;
import leola.vm.lib.LeolaIgnore;
import leola.vm.lib.LeolaLibrary;
import leola.vm.types.LeoArray;
import leola.vm.types.LeoNamespace;
import leola.vm.types.LeoNull;
import leola.vm.types.LeoObject;
import leola.vm.types.LeoString;

/**
 * Standard String functions
 * 
 * @author Tony
 *
 */
public class StringLeolaLibrary implements LeolaLibrary {

	private Leola runtime;
	
	/* (non-Javadoc)
	 * @see leola.frontend.LeolaLibrary#init(leola.frontend.Leola)
	 */
	@Override
	@LeolaIgnore
	public void init(Leola runtime, LeoNamespace namespace) throws Exception {
		this.runtime = runtime;
		this.runtime.putIntoNamespace(this, namespace);
	}
		
	public void foreach(LeoString str, LeoObject function) {		
		int size = str.length();

		for(int i = 0; i < size; i++) {
			LeoObject result = this.runtime.execute(function, str.charAt(i) );	
			if ( LeoObject.isTrue(result) ) {
				break;
			}
		}
		
	}
	
	public LeoString printf(Object str, LeoArray args) {
		LeoString result = null;
		if( args.isArray() ) {
			LeoArray array = args.as();
			
			int len = array.size();
			Object[] params = new Object[len];						
			for(int i = 0; i < len; i++) {
				params[i] = array.get(i).getValue();
			}
			
			result = LeoString.valueOf(String.format(str.toString(), params));
		}
		else {
			result = LeoString.valueOf(String.format(str.toString(), args.getValue()));
		}
		
		return result;	
	}
	
	public LeoString append(LeoString str, LeoString v) {
		return str.append(v);
	}
	
	public LeoString charAt(LeoString str, int i) {
		return str.charAt(i); 
	}
	
	public LeoString insert(LeoString str, int index, LeoObject v) {
		return str.insert(index, v);
	}
	
	public LeoString replace(LeoString str, int start, int end, LeoObject v) {
		return str.replace(start, end, v);
	}
	
	public LeoString replaceAll(LeoString str, LeoObject replaceMe, LeoObject v) {
		return str.replaceAll(replaceMe, v);
	}
	
	public LeoArray split(LeoString str, LeoObject v) {
		return str.split(v);
	}
	
	public boolean contains(LeoString str, LeoObject v) {
		return str.contains(v);
	}
	
	public int indexOf(LeoString str, LeoObject v) {
		return str.indexOf(v);
	}
	
	public LeoString substring(LeoString str, int start, int end) {
		return str.substring(start, end);
	}
	
	public LeoString rest(LeoString str, int start) {
		return str.rest(start);
	}
	
	public boolean endsWith(LeoString str, LeoObject v) {
		return str.endsWith(v);
	}
	
	public boolean startsWith(LeoString str, LeoObject v) {
		return str.startsWith(v);
	}
	
	public LeoString toLower(LeoString str) {
		return str.toLower();
	}
	
	public LeoString toUpper(LeoString str) {
		return str.toUpper();
	}
	
	public boolean empty(LeoObject str) {
		return str == null || 
			   str == LeoNull.LEONULL ||
			   str.toLeoString().empty();
	}	
	
	public LeoString trim(LeoObject str) {
		return LeoString.valueOf(str.toString().trim());
	}
		
	public byte[] bytes(LeoObject str) {
		return str.toString().getBytes();
	}
}

