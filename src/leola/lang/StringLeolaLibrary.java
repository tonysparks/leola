/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.lang;

import leola.vm.Leola;
import leola.vm.exceptions.LeolaRuntimeException;
import leola.vm.lib.LeolaIgnore;
import leola.vm.lib.LeolaLibrary;
import leola.vm.lib.LeolaMethodVarargs;
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
    public void init(Leola runtime, LeoNamespace namespace) throws LeolaRuntimeException {
        this.runtime = runtime;
        this.runtime.putIntoNamespace(this, namespace);
    }
        
    public void foreach(LeoString str, LeoObject function) {        
        int size = str.length();

        for(int i = 0; i < size; i++) {
            LeoObject result = function.xcall(str.charAt(i));    
            if ( LeoObject.isTrue(result) ) {
                break;
            }
        }
        
    }

    /**
     * Combines the list of arguments (separating them by the supplied delimiter).
     * 
     * @param delimiter
     * @param args
     * @return the joined string
     */
    @LeolaMethodVarargs
    public LeoString join(Object delimiter, Object ... args) {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < args.length; i++) {
            if(i>0) {
                sb.append(delimiter);
            }
            sb.append(args[i]);
        }
        return LeoString.valueOf(sb.toString());
    }
    
    @LeolaMethodVarargs
    public LeoString printf(Object str, LeoObject ... args) {
        LeoString result = null;
        if(args!=null) {
            int len = args.length;
            Object[] params = new Object[len];                      
            for(int i = 0; i < len; i++) {
                params[i] = args[i].getValue();
            }
            result = LeoString.valueOf(String.format(str.toString(), params));
        }
        else {
            result = LeoString.valueOf(String.format(str.toString()));
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
    
    /**
     * Retrieves all of the indexes where 'v' is found in this string.
     * @param v
     * @return list of all indexes where 'v' is found in this string.
     */
    public LeoArray indexesOf(LeoString str, LeoObject v) {
        return str.indexesOf(v);
    }
}

