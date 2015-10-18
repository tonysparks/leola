/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.vm.types;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import leola.vm.exceptions.LeolaRuntimeException;
import leola.vm.lib.LeolaMethod;
import leola.vm.util.ClassUtil;


/**
 * A Leola String. For the most part all String operations return a new {@link LeoString} instance, leaving the {@link LeoString}
 * more or less immutable.  The one exception is the indexing into the string itself:
 * 
 * <pre>
 *    var aString = "Hello"
 *    aString[2] = "X"
 *    println(aString) // HeXllo ; the indexing does an insert
 * </pre>
 * 
 * <p>
 * This might change in the future, I'm a bit undecided if this is a desirable feature -- furthermore this probably can cause
 * issues with the {@link LeoString} interning (due to the original string referencing the LeoString, which if altered would no
 * longer match).
 *
 * @author Tony
 *
 */
public class LeoString extends LeoObject {

	private static final Map<String, WeakReference<LeoString>> interns = new ConcurrentHashMap<String, WeakReference<LeoString>>();
	private static LeoString getString(String ref) {
		WeakReference<LeoString> str = interns.get(ref);
		return str != null ? str.get() : null;
	}
	private static LeoString putString(String ref) {
		LeoString lStr = new LeoString(ref);
		interns.put(ref, new WeakReference<LeoString>(lStr));
		return lStr;
	}
	
	/**
	 * Value
	 */
	private String value;

	/**
	 * @param value
	 */
	public LeoString(String value) {
		this(new StringBuilder(value));
	}

	/**
	 * @param value
	 */
	public LeoString(StringBuilder value) {
		super(LeoType.STRING);
		this.value = value==null ? "" : value.toString();
	}

	/**
	 * Interns the {@link LeoString}.
	 * 
	 * @param str
	 * @return
	 */
	public static LeoString valueOf(String str) {
		LeoString lStr = getString(str);
		return lStr != null ? lStr : putString(str);		
	}
	
	/**
	 */
	public LeoString() {
		this(new StringBuilder());
	}	
	
	/**
     * Adds ability to reference the public API of this class
     */
    private Map<LeoObject, LeoObject> stringApi; 
    private Map<LeoObject, LeoObject> getApiMappings() {
        if(this.stringApi == null) {
            synchronized (this) {                
                if(this.stringApi == null) {    
                    this.stringApi = new LeoMap();
                }
            }
        }
        return this.stringApi;
    }
    private LeoObject getNativeMethod(LeoObject key) {        
        return getNativeMethod(this, getApiMappings(), key);
    }
	
	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#toLeoString()
	 */
	@Override
	public LeoString toLeoString() {
		return this;
	}
	
	/* (non-Javadoc)
	 * @see leola.types.LeoObject#isString()
	 */
	@Override
	public boolean isString() {
		return true;
	}
	
	/**
	 * @return the value
	 */
	public String getString() {
		return value.toString();
	}


	/* (non-Javadoc)
	 * @see leola.types.LeoObject#toString()
	 */
	@Override
	public String toString() {
		return this.value;
	}

	/**
     * Maps the supplied function to each element in the string.
     * 
     * <pre>
     *     var result = "hi".map(def(e) return e+"2")
     *     println(result) // h2i2
     * </pre>
     * 
     * @param function
     * @return the new mapped {@link LeoString}
     */
	public LeoString map(LeoObject function) {	    
        StringBuilder sb = new StringBuilder(length());
        
        int len = length();
        for(int i = 0; i < len; i++) {            
            LeoObject result = function.xcall(charAt(i));
            sb.append(result.toString());                    
        }
        
        return LeoString.valueOf(sb.toString());
	}
	
	/**
	 * Returns a sequence consisting of those items from the sequence for which function(item) is true
	 * 
	 * @param function
	 * @return the new String
	 */
	public LeoString filter(LeoObject function) {	    
        StringBuilder sb = new StringBuilder(this.value);
        
        int len = this.value.length();
        for(int i = 0; i < len; i++) {
            char c = this.value.charAt(i);
            
            LeoString ch = LeoString.valueOf( String.valueOf(c));
            if ( LeoObject.isTrue(function.xcall(ch)) ) {                       
                sb.append(c);
            }
        }
        
        return LeoString.valueOf(sb.toString());
	}
	
	
    /**
     * Iterates through the string, invoking the supplied 
     * function object for each element.  The start and end index are [start, end).
     * 
     * <pre>
     *   "hi".for(0, 1, def(c) println(c))
     *   // prints: 
     *   // h
     *   
     * </pre>
     * 
     * @param start starting index (inclusive)
     * @param end ending index (exclusive)
     * @param function
     */
	@LeolaMethod(alias="for")
    public void _for(int start, int end, LeoObject function) {
        int len = length();               

        for(int i = start; i < len && i < end; i++) {
            LeoObject result = function.xcall(charAt(i));  
            if(LeoObject.isTrue(result)) {
                break;
            }
        }
    }
	
	/**
     * Iterates through the array, invoking the supplied 
     * function object for each element
     * 
     * <pre>
     *   "hi".foreach(def(c) println(c))
     *   // prints: 
     *   // h
     *   // i
     *   
     * </pre>
     * 
     * @param function
     * @return the {@link LeoObject} returned from the supplied function if returned <code>true</code>
     */
	public LeoObject foreach(LeoObject function) {
	    int len = length();               
        
        for(int i = 0; i < len; i++) {
            LeoObject result = function.xcall(charAt(i));  
            if ( LeoObject.isTrue(result) ) {
                return result;
            }
        }
        return LeoObject.NULL;
	}

	/**
     * Combines the list of arguments (separating them by the supplied delimiter) and appending them
     * to this string.
     * 
     * @param delimiter
     * @param args
     * @return the joined string
     */
    public LeoString join(String delimiter, Object ... args) {
        StringBuilder sb = new StringBuilder(this.value);
        for(int i = 0; i < args.length; i++) {            
            sb.append(args[i]);
        }
        return LeoString.valueOf(sb.toString());
    }
	
	/**
	 * Formats the current string according to {@link String#format(String, Object...)}.
	 * @param args
	 * @return the formatted string
	 */
	public LeoString format(Object ...args) {
	    return LeoString.valueOf(String.format(this.value, args));
	}
	
	/**
	 * @return a new instance in lower case
	 */
	public LeoString toLower() {
		return LeoString.valueOf(this.value.toLowerCase());
	}

	/**
	 * @return a new instance in upper case
	 */
	public LeoString toUpper() {
		return LeoString.valueOf(this.value.toUpperCase());
	}

	@Override
	public LeoObject $add(LeoObject other) {
		return append(other);
	}	
	@Override
	public LeoObject $add(double other) {
		return LeoString.valueOf(other + this.value);
	}	
	@Override
	public LeoObject $add(int other) {
		return LeoString.valueOf(other + this.value);
	}
	@Override
	public LeoObject $add(long other) {
		return LeoString.valueOf(other + this.value);
	}
	
	@Override
	public LeoObject $sub(LeoObject other) {	
	    return replaceAll(other, LeoObject.valueOf(""));
	}
	
    @Override
    public LeoObject $index(double other) {
        return charAt( (int) other);
    }
    
    @Override
    public LeoObject $index(int other) {
        return charAt(other);
    }
    
    /* (non-Javadoc)
     * @see leola.vm.types.LeoObject#$index(long)
     */
    @Override
    public LeoObject $index(long other) {    
        return charAt( (int)other );
    }
    
    /* (non-Javadoc)
     * @see leola.vm.types.LeoObject#$index(leola.vm.types.LeoObject)
     */
    @Override
    public LeoObject $index(LeoObject other) {
        return charAt(other.asInt());
    }
    
    /* (non-Javadoc)
     * @see leola.vm.types.LeoObject#$index(leola.vm.types.LeoObject, leola.vm.types.LeoObject)
     */
    @Override
    public void $sindex(LeoObject key, LeoObject other) {
        if(key.isNumber()) {
            int index = key.asInt();
            
            StringBuilder sb = new StringBuilder(this.value);
            sb.insert(index, other.toString());
            this.value = sb.toString();
        }
        else {
            String regex = key.toString();
            this.value = this.value.replaceAll(regex, other.toString());
        }
    }

    /* (non-Javadoc)
     * @see leola.vm.types.LeoObject#setObject(leola.vm.types.LeoObject, leola.vm.types.LeoObject)
     */
    @Override
    public void setObject(LeoObject key, LeoObject value) { 
        this.getApiMappings().put(key, value);      
    }
    
    /* (non-Javadoc)
     * @see leola.vm.types.LeoObject#getObject(leola.vm.types.LeoObject)
     */
    @Override
    public LeoObject getObject(LeoObject key) {
        return getNativeMethod(key);
    }
    
    /* (non-Javadoc)
     * @see leola.vm.types.LeoObject#hasObject(leola.vm.types.LeoObject)
     */
    @Override
    public boolean hasObject(LeoObject key) {     
        return hasNativeMethod(this, key);
    }
	
	/**
	 * Appends to this string.
	 *
	 * @param v
	 * @return
	 */
	public LeoString append(LeoObject v) {
	    //this.value.append(v.toString());
	    return LeoString.valueOf(this.value + v.toString());
	}

	/**
	 * inserts a string into the supplied position.
	 * @param position
	 * @param v
	 * @return this string
	 */
	public LeoString insert(int position, LeoObject v) {
//		this.value.insert(position, v.toString());
		StringBuilder sb = new StringBuilder(this.value);
		sb.insert(position, v.toString());
		return LeoString.valueOf(sb.toString());
	}

	/**
	 * Determines if this string contains the supplied string (v)
	 *
	 * @param v
	 * @return true if this string contains the supplied string (v)
	 */
	public boolean contains(LeoObject v) {
		return this.value.indexOf(v.toString()) > -1;
	}

	/**
	 * The index of the supplied string
	 * @param v
	 * @return -1 if the supplied string is not in this string.
	 */
	public int indexOf(LeoObject v) {
		return this.value.indexOf(v.toString());
	}

	/**
	 * The rest of the string from position i.
	 * @param i
	 * @return
	 */
	public LeoString rest(int i) {
		return new LeoString(this.value.substring(i));
	}

	/**
	 * Gets the substring.
	 *
	 * @param start
	 * @param end
	 * @return
	 */
	public LeoString substring(int start, int end) {
		return new LeoString(this.value.substring(start, end));
	}


	/**
	 * Replaces a portion of the string.
	 *
	 * @param start
	 * @param end
	 * @param v
	 * @return
	 */
	public LeoString replace(int start, int end, LeoObject v) {
		//this.value.replace(start, end, v.toString());
		StringBuilder sb = new StringBuilder(this.value);
		sb.replace(start, end, v.toString());
		return LeoString.valueOf(sb.toString());
	}
	
	/**
	 * Replaces all occurrences of the supplied string.
	 * 
	 * @param replaceMe
	 * @param v
	 * @return
	 */
	public LeoString replaceAll(LeoObject replaceMe, LeoObject v) {
		return LeoString.valueOf(this.value.replaceAll(replaceMe.toString(), v.toString()));
//		String source = replaceMe.toString();
//		String replacementString = v.toString();
//		
//		int sourceLength = source.length();
//		int targetLength = replacementString.length();
//		
//		int index = this.value.indexOf(source);
//	    while (index != -1) {
//	    	int startIndex = index;
//	    	int endIndex = index + sourceLength;
//	    		    	
//	    	this.value.replace(startIndex, endIndex, replacementString);
//	        index += targetLength; // Move to the end of the replacement
//	        index = this.value.indexOf(source, index);	        
//	    }
//		return this;
	}

	/**
	 * Splits the string by the regex
	 * 
	 * @param v
	 * @return a {@link LeoArray}
	 */
	public LeoArray split(LeoObject v) {
		String[] res = this.value.toString().split(v.toString());
		LeoArray result = new LeoArray(res.length);
		for(int i = 0; i < res.length; i++) {
		    result.add(LeoString.valueOf(res[i]));
		}
		return result;
	}

	public boolean endsWith(LeoObject v) {
		String suffix = v.toString();
		return startsWith(suffix, length() - suffix.length());
	}
	
	public boolean startsWith(LeoObject v) {
		return startsWith(v.toString(), 0);
	}
	
    public boolean startsWith(String prefix, int toffset) {    	
    	int to = toffset;    	    	
    	int po = 0;
    	int pc = prefix.length();
    	
    	// Note: toffset might be near -1>>>1.
    	if ((toffset < 0) || (toffset > this.value.length() - pc)) {
    	    return false;
    	}
    	
    	while (--pc >= 0) {
    	    if (this.value.charAt(to++) != prefix.charAt(po++) ) {
    	        return false;
    	    }
    	}
    	return true;
    }
	
    /**
     * Retrieves all of the indexes where 'v' is found in this string.
     * @param v
     * @return list of all indexes where 'v' is found in this string.
     */
    public LeoArray indexesOf(LeoObject v) {
        LeoArray results = new LeoArray();
        String str = v.toString();
        
        int index = 0;
        int result = 0;
        while (result > -1) {
           result = this.value.indexOf(str, index);
           if(result > -1) {
               results.add(LeoInteger.valueOf(result));
               index = result + 1;
           }
        } 
        
        return results;
    }
    
    /**
     * @return removes any leading or trailing whitespace characters
     */
    public LeoString trim() {
        return LeoString.valueOf(this.value.trim());
    }
    
	/**
	 * @return the length of the string
	 */
	public int length() {
	    return (this.value != null ) ? this.value.length() : 0;
	}

	/**
	 * @return true if the string is "" or NULL
	 */
	public boolean empty() {
	    return length() == 0;
	}

	/**
	 * @param n
	 * @return the character at this position
	 */
	public LeoString charAt(int n) {
	    return LeoString.valueOf(String.valueOf( this.value.charAt(n) ));
	}

	/**
	 * @param n
	 * @return the integer value of the character at a location
	 */
	public int byteAt(int n) {
		return this.value.charAt(n);
	}
	
	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#hashCode()
	 */
	@Override
	public int hashCode() {	
		return (this.value != null) ? this.value.hashCode() : super.hashCode();
	}

	/* (non-Javadoc)
	 * @see leola.types.LeoObject#eq(leola.types.LeoObject)
	 */
	@Override
	public boolean $eq(LeoObject other) {
		boolean result = false;
		if ( (other != null ) && other.isString() ) {			
			String a = this.value;
			String b = other.toString();
			if ( a != null ) {
				result = a.equals(b);
			}
			else {
				result = (b==null);
			}
		}

		return result;
	}

	/**
	 * Compares two {@link StringBuilder}s
	 * @param l
	 * @param anotherString
	 * @return
	 */
	private int compareTo(String l, String anotherString) {
		int len1 = l.length();
		int len2 = anotherString.length();
		int n = Math.min(len1, len2);

		int i = 0;
		int j = 0;

		while (n-- != 0) {
			char c1 = l.charAt(i++);
			char c2 = anotherString.charAt(j++);
			if (c1 != c2) {
			    return c1 - c2;
			}
		}
		return len1 - len2;
	}

	/* (non-Javadoc)
	 * @see leola.types.LeoObject#gt(leola.types.LeoObject)
	 */
	@Override
	public boolean $gt(LeoObject other) {
		if ( other != null && other.isOfType(LeoType.STRING)) {
			LeoString str = other.as();
			int c = compareTo(this.value, str.value);
			return c > 0;
		}

		return false;
	}

	/* (non-Javadoc)
	 * @see leola.types.LeoObject#gte(leola.types.LeoObject)
	 */
	@Override
	public boolean $gte(LeoObject other) {
		if ( other != null && other.isOfType(LeoType.STRING)) {
			LeoString str = other.as();
			int c = compareTo(this.value, str.value);
			return c >= 0;
		}

		return false;
	}

	/* (non-Javadoc)
	 * @see leola.types.LeoObject#lt(leola.types.LeoObject)
	 */
	@Override
	public boolean $lt(LeoObject other) {
		if ( other != null && other.isOfType(LeoType.STRING)) {
			LeoString str = other.as();
			int c = compareTo(this.value, str.value);
			return c < 0;
		}

		return false;
	}

	/* (non-Javadoc)
	 * @see leola.types.LeoObject#lte(leola.types.LeoObject)
	 */
	@Override
	public boolean $lte(LeoObject other) {
		if ( other != null && other.isOfType(LeoType.STRING)) {
			LeoString str = other.as();
			int c = compareTo(this.value, str.value);
			return c <= 0;
		}

		return false;
	}

	/* (non-Javadoc)
	 * @see leola.types.LeoObject#getValue()
	 */
	@Override
	public Object getValue() {
		return this.value;
	}
	
	private void checkSizeForConversion() {
		if ( this.value.length() > 1) {
			throw new LeolaRuntimeException
				("StringCharError: The supplied LeoString: '" + this.value + "' is larger than 1 character and therefore does not match the native type: char");
		}
	}
	
	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#getValue(java.lang.Class)
	 */
	@Override
	public Object getValue(Class<?> type) {
		Object resultJavaObj = this.value;
		if(ClassUtil.inheritsFrom(type, LeoObject.class) ) {
			resultJavaObj = this;
		}
		else if(ClassUtil.isType(type, ClassUtil.CHAR) ) {
			checkSizeForConversion();
			resultJavaObj = this.value.charAt(0);
		} else if(ClassUtil.isType(type, ClassUtil.BYTE) ) {
			checkSizeForConversion();
			resultJavaObj = (byte)this.value.charAt(0);
		} else if(ClassUtil.isType(type, ClassUtil.SHORT) ) {
			checkSizeForConversion();
			resultJavaObj = (short)this.value.charAt(0);
		} else if(ClassUtil.isType(type, ClassUtil.INT) ) {
			checkSizeForConversion();
			resultJavaObj = (int)this.value.charAt(0);
		} else if(ClassUtil.isType(type, ClassUtil.LONG) ) {
			checkSizeForConversion();
			resultJavaObj = (long)this.value.charAt(0);
		} else if(ClassUtil.isType(type, ClassUtil.FLOAT) ) {
			checkSizeForConversion();
			resultJavaObj = (float)this.value.charAt(0);
		} else if(ClassUtil.isType(type, ClassUtil.DOUBLE) ) {
			checkSizeForConversion();
			resultJavaObj = (double)this.value.charAt(0);
		} 
		
		return resultJavaObj;
	}

	/* (non-Javadoc)
	 * @see leola.types.LeoObject#clone()
	 */
	@Override
	public LeoObject clone() {
		return this;
	}

	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#write(java.io.DataOutput)
	 */
	@Override
	public void write(DataOutput out) throws IOException {
		out.write(this.getType().ordinal());	
		out.writeInt(this.value.length());
		out.writeBytes(this.value);
	}

	/**
	 * Reads from the {@link DataInput} stream, constructing a {@link LeoObject}
	 * 
	 * @param in
	 * @return the {@link LeoObject}
	 * @throws IOException
	 */
	public static LeoString read(DataInput in) throws IOException {
		String str = "";
		int length = in.readInt();
		if ( length > 0 ) {
			byte[] buf = new byte[length];
			in.readFully(buf);
			str = new String(buf);
		}
		return LeoString.valueOf(str);
	}
}

