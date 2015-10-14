/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.vm.types;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import leola.vm.exceptions.LeolaRuntimeException;
import leola.vm.lib.LeolaMethod;
import leola.vm.util.ArrayUtil;


/**
 * A {@link Map} data structure.  All contents of the map must be  {@link LeoObject}, that is both the keys and
 * values.
 * 
 * <p>
 * This implements all optional methods of the {@link Map} interface.  This implementation attempts to reduce the
 * amount of allocations by storing keys in an array, and their corresponding values in another array.  That is,
 * this implementation is <b>not</b> like that of {@link HashMap}.
 *
 * @author Tony
 *
 */
public class LeoMap extends LeoObject implements Map<LeoObject, LeoObject> {

    /**
     * Converts the java {@link Map} into a {@link LeoMap}.
     * 
     * @param jMap
     * @return the {@link LeoMap}
     */
    public static LeoMap toMap(Map<?, ?> jMap) {
        LeoMap result = new LeoMap();
        for(Map.Entry<?, ?> entry : jMap.entrySet()) {
            result.put(LeoObject.valueOf(entry.getKey()), LeoObject.valueOf(entry.getValue()));
        }
        
        return result;
    }

    /**
     * A convenience method that creates a new {@link LeoMap}, inserting the key/value pair.
     * 
     * @param key the key of an entry
     * @param value the value of an entry
     * @return the newly created {@link LeoMap}.
     */
    public static LeoMap newLeoMap(Object key, Object value) {
        LeoMap result = new LeoMap();
        result.put(LeoObject.valueOf(key), LeoObject.valueOf(value));
        return result;
    }
    
	/**
	 */
	public LeoMap() {
		this(8);
	}

	/**
	 * @param initialCapacity
	 */
	public LeoMap(int initialCapacity) {
		super(LeoType.MAP);
		
		presize(initialCapacity);
	}

	/**
	 * @param array
	 */
	public LeoMap(Map<LeoObject, LeoObject> map) {
		this(map.size());
		putAll(map);
	}
	
	/**
     * Adds ability to reference the public API of this class
     */
    private Map<LeoObject, LeoObject> mapApi;  
    private Map<LeoObject, LeoObject> getApiMappings() {
        if(this.mapApi == null) {
            synchronized (this) {                
                if(this.mapApi == null) {    
                    this.mapApi = new LeoMap();
                }
            }
        }
        return this.mapApi;
    }
    private LeoObject getNativeMethod(LeoObject key) {        
        return getNativeMethod(this, getApiMappings(), key);
    }
	
    
    /**
     * Iterates through the array, invoking the supplied 
     * function object for each element
     * 
     * <pre>
     *   var x = {x->1,y->2,z->3}.foreach(def(k,v) println(k + ":" + v))
     *   // prints: 
     *   // x : 1
     *   // y : 2
     *   // z : 3
     *   
     * </pre>
     * 
     * @param function
     * @return the {@link LeoObject} returned from the supplied function if returned <code>true</code>
     */
    public LeoObject foreach(LeoObject function) {
        if(function != null) {
            for(int i = 0; i < this.bucketLength(); i++) {
                LeoObject key = getKey(i);
                if(key != null) {
                    LeoObject value = getValue(i);
                    LeoObject result = function.xcall(key, value);
                    if(LeoObject.isTrue(result)) {
                        return result;
                    }
                }
            }
        }
        return LeoObject.NULL;
    }
    
    /**
     * Filters the {@link LeoArray}
     * 
     * <pre>
     *     var evens = {x->1,y->2,z->3}.filter(def(k,v) {
     *         return v % 2 == 0
     *     }) 
     *     println(evens) // prints, {y->2}
     * </pre>
     * 
     * @param function
     * @return a resulting {@link LeoArray}
     */
    public LeoMap filter(LeoObject function) {
        if(function != null) {
            LeoMap map = new LeoMap();
            for(int i = 0; i < this.bucketLength(); i++) {
                LeoObject key = getKey(i);
                if(key != null) {
                    LeoObject value = getValue(i);
                    if( LeoObject.isTrue(function.xcall(key, value)) ) {
                        map.put(key, value);
                    }
                }
            }
            return map;
        }
        return this;
    }
    
    
    /**
     * Maps the supplied function to each element in the array.
     * 
     * <pre>
     *     var result = [1,2,3,4].map(def(e) return e*2)
     *     println(result) // 2, 4, 6, 8
     * </pre>
     * 
     * @param function
     * @return the new mapped {@link LeoMap}
     */
    public LeoMap map(LeoObject function) {
        if(function != null) {
            LeoMap map = new LeoMap();
            for(int i = 0; i < this.bucketLength(); i++) {
                LeoObject key = getKey(i);
                if(key != null) {
                    LeoObject value = getValue(i);
                    value = function.xcall(key, value);
                    map.put(key, value);                    
                }
            }
            return map;
        }
        return this;
    }
    
	/* (non-Javadoc)
	 * @see leola.types.LeoObject#isMap()
	 */
	@Override
	public boolean isMap() {
		return true;
	}

	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#hashCode()
	 */
	@Override
	public int hashCode() {
		int h = 0;
		for(int i = 0; i < this.hashKeys.length; i++) {
			if(this.hashKeys[i] != null) {
				h += this.hashKeys[i].hashCode();				
			}
			if(this.hashValues[i] != null ) {
				h += this.hashValues[i].hashCode();
			}
		}
		return h;
	}
	
	/* (non-Javadoc)
	 * @see leola.types.LeoObject#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		boolean isFirst = true;
		sb.append("{ ");
		for(int i = 0; i < this.hashKeys.length; i++) {
			if(this.hashKeys[i] != null) {
				if ( !isFirst) {
					sb.append(", ");
				}
				
				LeoObject key = this.hashKeys[i];
				if(key.isString() ) {
					sb.append("\"").append(key).append("\"");
				}
				else if(key.isNull()) {
	                sb.append("null");
	            }
				else {
					sb.append(key);
				}
				
				sb.append(" : ");
				
				
				LeoObject val = get(this.hashKeys[i]);
				if ( val != null ) {
					if(val.isString()) {
						sb.append("\"").append(val).append("\"");
					}
					else if(val.isNull()) {
		                sb.append("null");
		            }
					else {
						sb.append(val);
					}
				}
				
				isFirst = false;
			}
		}
		sb.append(" }");
		
		return sb.toString();
	}

	   /* (non-Javadoc)
     * @see leola.vm.types.LeoObject#$index(double)
     */
    @Override
    public LeoObject $index(double other) {
        return get(other);
    }
    
    /* (non-Javadoc)
     * @see leola.vm.types.LeoObject#$index(int)
     */
    @Override
    public LeoObject $index(int other) {
        return get(other);
    }
    
    /* (non-Javadoc)
     * @see leola.vm.types.LeoObject#$index(long)
     */
    @Override
    public LeoObject $index(long other) {    
        return get(other);
    }
    
    /* (non-Javadoc)
     * @see leola.vm.types.LeoObject#$index(leola.vm.types.LeoObject)
     */
    @Override
    public LeoObject $index(LeoObject other) {
        return get(other);
    }
	
    /* (non-Javadoc)
     * @see leola.vm.types.LeoObject#$sindex(leola.vm.types.LeoObject, leola.vm.types.LeoObject)
     */
    @Override
    public void $sindex(LeoObject key, LeoObject value) {
        put(key,value);     
    }
    
	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#setObject(leola.vm.types.LeoObject, leola.vm.types.LeoObject)
	 */
	@Override
	public void setObject(LeoObject key, LeoObject value) {
	    if(has(key)) {
	        put(key,value);
	    }
	    else {	    
    	    getApiMappings().put(key, value);
	    }
	}
	
	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#getObject(leola.vm.types.LeoObject)
	 */
	@Override
	public LeoObject getObject(LeoObject key) {
	    if(has(key)) {
	        return get(key);
	    }
	    else {
	        return getNativeMethod(key);
	    }
	}
	
	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#hasObject(leola.vm.types.LeoObject)
	 */
	@Override
	public boolean hasObject(LeoObject key) {	 
	    return containsKey(key) || hasNativeMethod(this, key);
	}
	
	/**
	 * Determines if the supplied key is contained in the {@link LeoMap}
	 * 
	 * @param key
	 * @return true if the supplied key is contained in the map
	 */
	public boolean has(LeoObject key) {
		return this.containsKey(key);
	}

	/**
	 * Determines if the supplied value is contained in the {@link LeoMap}
	 * 
	 * @param value
	 * @return true if the supplied value is contained in the map.
	 */
	public boolean hasValue(LeoObject value) {
	    return containsValue(value);
	}

	
	public void putAllEntries(LeoObject obj) {
		if ( obj.isOfType(LeoType.MAP)) {
			LeoMap map = obj.as();
			this.putAll((Map<LeoObject, LeoObject>)map);
		}
		
	}

	public void removeAllEntries(LeoObject obj) {
		if ( obj.isOfType(LeoType.MAP)) {
			LeoMap map = obj.as();
			for(LeoObject key : map.keySet()) {
				this.remove(key);
			}
		}
		
	}
	
	

	/**
	 * @return true if empty; false otherwise
	 */
	public boolean empty() {
		return this.isEmpty();
	}


	/**
	 * @return the key set
	 * TODO - Optimize this mess
	 */
	public LeoArray keys() {
		return new LeoArray(new ArrayList<LeoObject>(this.keySet()));
	}

	/**
	 * @return the value set
	 */
	public LeoArray vals() {
		return new LeoArray(new ArrayList<LeoObject>(this.values()));
	}

	/**
	 * @return the array
	 */
	public Map<LeoObject,LeoObject> getMap() {
		return this;
	}

	/* (non-Javadoc)
	 * @see leola.types.LeoObject#clone()
	 */
	@Override
	public LeoObject clone() {
		return new LeoMap(this);
	}

	/* (non-Javadoc)
	 * @see leola.types.LeoObject#eq(leola.types.LeoObject)
	 */
	@Override
	public boolean $eq( LeoObject val )  {
		if ( val == null ) return false;
		if ( this == val ) return true;	
		if ( val.getType() != LeoType.MAP ) return false;
	
		LeoMap t = (LeoMap)val;
		return  t.hashEntries==hashEntries && t.hashKeys.equals(hashKeys) && t.hashValues.equals(hashValues);
		
	}
	
	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#$add(leola.vm.types.LeoObject)
	 */
	@Override
	public LeoObject $add(LeoObject other) {
		if (other.isString()) {
			return LeoString.valueOf(toString() + other.toString());
		}
		
		return super.$add(other);
	}
		
	
	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#$xor(leola.vm.types.LeoObject)
	 */
	@Override
	public LeoObject $xor(LeoObject other) {	
		return has(other) ? LeoBoolean.LEOTRUE : LeoBoolean.LEOFALSE;
	}
	
	/* (non-Javadoc)
	 * @see leola.types.LeoObject#getValue()
	 */
	@Override
	public Object getValue() {
		return this; /*this.array;*/
	}
		
	/* (non-Javadoc)
	 * @see leola.types.LeoObject#gt(leola.types.LeoObject)
	 */
	@Override
	public boolean $gt(LeoObject other) {
		return false;
	}

	/* (non-Javadoc)
	 * @see leola.types.LeoObject#lt(leola.types.LeoObject)
	 */
	@Override
	public boolean $lt(LeoObject other) {
		return false;
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.write(this.getType().ordinal());
		out.writeInt(this.size());
		for(Map.Entry<LeoObject, LeoObject> entry : this.entrySet()) {
			entry.getKey().write(out);
			entry.getValue().write(out);
		}
	}
	
	/**
	 * Reads from the {@link DataInput} stream, constructing a {@link LeoObject}
	 * 
	 * @param in
	 * @return the {@link LeoObject}
	 * @throws IOException
	 */
	public static LeoMap read(LeoObject env, DataInput in) throws IOException {
		int size = in.readInt();
		LeoMap map = new LeoMap(size);
		for(int i = 0; i < size; i++) {
			LeoObject key = LeoObject.read(map, in);
			LeoObject value = LeoObject.read(map, in);
			
			map.put(key, value);
		}
		
		return map;
	}

	private static final int      MIN_HASH_CAPACITY = 2;
	
	/** the hash keys */
	protected LeoObject[] hashKeys;
	
	/** the hash values */
	protected LeoObject[] hashValues;
	
	/** the number of hash entries */
	protected int hashEntries;
		
	
	protected void presize(int nhash) {
		if ( nhash >= 0 && nhash < MIN_HASH_CAPACITY )
			nhash = MIN_HASH_CAPACITY;
		
		hashKeys = (nhash>0? new LeoObject[nhash]: ArrayUtil.EMPTY_LEOOBJECTS);
		hashValues = (nhash>0? new LeoObject[nhash]: ArrayUtil.EMPTY_LEOOBJECTS);
		hashEntries = 0;
	}


	
	protected LeoObject hashget(LeoObject key) {
		if ( hashEntries > 0 ) {
			LeoObject v = hashValues[hashFindSlot(key)];
			return v!=null? v: LeoNull.LEONULL;
		}
		return LeoNull.LEONULL;
	}
	
	/**
	 * Returns the value associated with the key.  If the key is not found, a Java
	 * <code>null</code> is returned.
	 * 
	 * @param key 
	 * @return Returns the value associated with the key.  If the key is not found, a Java
     * <code>null</code> is returned.
	 */
	public LeoObject getWithJNull(LeoObject key) {
		if ( hashEntries > 0 ) {
			LeoObject v = hashValues[hashFindSlot(key)];
			return v;
		}
		return null;
	}
	

	/** caller must ensure key is not nil */
	public void set( LeoObject key, LeoObject value ) {			
		rawset(key, value);
	}

	
	/** caller must ensure key is not nil */
	protected void rawset( LeoObject key, LeoObject value ) {	
		hashset( key, value );
	}


	public String getString(String key) {
		LeoObject obj = get(LeoString.valueOf(key));
		return obj != null ? obj.toString() : "";
	}
	
	public int getInt(String key) {
		LeoObject obj = get(LeoString.valueOf(key));
		return obj != null ? obj.asInt() : 0;
	}

	public long getLong(String key) {
        LeoObject obj = get(LeoString.valueOf(key));
        return obj != null ? obj.asLong() : 0;
    }

    public double getDouble(String key) {
        LeoObject obj = get(LeoString.valueOf(key));
        return obj != null ? obj.asDouble() : 0;
    }
    
    public LeoMap getMap(String key) {
        LeoObject obj = get(LeoString.valueOf(key));
        return obj != null ? ((LeoMap)obj.as()) : null;
    }
    
    public LeoArray getArray(String key) {
        LeoObject obj = get(LeoString.valueOf(key));
        return obj != null ? ((LeoArray)obj.as()) : null;
    }   
    
    public boolean getBoolean(String key) {
        LeoObject obj = get(LeoString.valueOf(key));
        return LeoObject.isTrue(obj);
    }

    
	public int length() {
		return this.hashEntries;
	}

	public int bucketLength() {
		return this.hashKeys.length;
	}

	private void error(String error) {
		throw new LeolaRuntimeException("LeoMapHashError: " + error);
	}

	protected int nexti( LeoObject key ) {
		int i = 0;
		do {
			// find current key index
			if ( key != LeoNull.LEONULL ) {
				
				if ( hashKeys.length == 0 )
					error( "invalid key to 'next'" );
				i = hashFindSlot(key);
				if ( hashKeys[i] == null )
					error( "invalid key to 'next'" );				
			}
		} while ( false );
		
		// check hash part
		for ( ; i<hashKeys.length; ++i )
			if ( hashKeys[i] != null )
				return i;
		
		// nothing found, push nil, return nil.
		return -1;
	}
	
	public LeoObject getKey(int index) {
		return hashKeys[index];
	}
	
	public LeoObject getValue(int index) {
		return hashValues[index];
	}
	
	protected LeoObject nextKey(LeoObject key) {
		int i = nexti(key);
		return hashKeys[i];
	}
	
	protected LeoObject nextValue(LeoObject key) {
		int i = nexti(key);
		return hashValues[i];
	}	
	

	/**
	 * Set a hashtable value
	 * 
	 * @param key key to set
	 * @param value value to set
	 */
	protected LeoObject hashset(LeoObject key, LeoObject value) {
		LeoObject r = LeoNull.LEONULL;
		/*if ( value == LeoNull.LEONULL )
			r = hashRemove(key);
		else */
		{
			if ( hashKeys.length == 0 ) {
				hashKeys = new LeoObject[ MIN_HASH_CAPACITY ];
				hashValues = new LeoObject[ MIN_HASH_CAPACITY ];
			}
			int slot = hashFindSlot( key );
			r = hashValues[slot];
			
			if ( hashFillSlot( slot, value ) )
				return r;
			hashKeys[slot] = key;
			hashValues[slot] = value;
			if ( checkLoadFactor() )
				rehash();
		}
		
		return r;
	}
	
	/** 
	 * Find the hashtable slot to use
	 * @param key key to look for
	 * @return slot to use
	 */
	protected int hashFindSlot(LeoObject key) {		
		int i = ( key.hashCode() & 0x7FFFFFFF ) % hashKeys.length;
		
		// This loop is guaranteed to terminate as long as we never allow the
		// table to get 100% full.
		LeoObject k;
		while ( ( k = hashKeys[i] ) != null && !k.$eq(key) ) {
			i = ( i + 1 ) % hashKeys.length;
		}
		return i;
	}

	private boolean hashFillSlot( int slot, LeoObject value ) {
		hashValues[ slot ] = value;
		if ( hashKeys[ slot ] != null ) {
			return true;
		} else {
			++hashEntries;
			return false;
		}
	}
	
	private LeoObject hashRemove( LeoObject key ) {
		LeoObject r = LeoNull.LEONULL;
		if ( hashKeys.length > 0 ) {
			int slot = hashFindSlot( key );
			r = hashValues[slot];
			
			hashClearSlot( slot );
		}
		return r;
	}
	
	/**
	 * Clear a particular slot in the table
	 * @param i slot to clear.
	 */
	protected void hashClearSlot( int i ) {
		if ( hashKeys[ i ] != null ) {
			
			int j = i;
			int n = hashKeys.length; 
			while ( hashKeys[ j = ( ( j + 1 ) % n ) ] != null ) {
				final int k = ( ( hashKeys[ j ].hashCode() )& 0x7FFFFFFF ) % n;
				if ( ( j > i && ( k <= i || k > j ) ) ||
					 ( j < i && ( k <= i && k > j ) ) ) {
					hashKeys[ i ] = hashKeys[ j ];
					hashValues[ i ] = hashValues[ j ];
					i = j;
				}
			}
			
			--hashEntries;
			hashKeys[ i ] = null;
			hashValues[ i ] = null;
			
			if ( hashEntries == 0 ) {
				hashKeys = ArrayUtil.EMPTY_LEOOBJECTS;
				hashValues = ArrayUtil.EMPTY_LEOOBJECTS;
			}
		}
	}

	private boolean checkLoadFactor() {
		// Using a load factor of (n+1) >= 7/8 because that is easy to compute without
		// overflow or division.
		final int hashCapacity = hashKeys.length;
		return hashEntries >= (hashCapacity - (hashCapacity>>3));
	}

	private void rehash() {
		final int oldCapacity = hashKeys.length;
		final int newCapacity = oldCapacity+(oldCapacity>>2)+MIN_HASH_CAPACITY;
		
		final LeoObject[] oldKeys = hashKeys;
		final LeoObject[] oldValues = hashValues;
		
		hashKeys = new LeoObject[ newCapacity ];
		hashValues = new LeoObject[ newCapacity ];
		
		for ( int i = 0; i < oldCapacity; ++i ) {
			final LeoObject k = oldKeys[i];
			if ( k != null ) {
				final LeoObject v = oldValues[i];
				final int slot = hashFindSlot( k );
				hashKeys[slot] = k;
				hashValues[slot] = v;
			}
		}
	}


	/* (non-Javadoc)
	 * @see java.util.Map#size()
	 */
	@Override
	public int size() {
		return this.hashEntries;
	}

	/* (non-Javadoc)
	 * @see java.util.Map#isEmpty()
	 */	
	@Override
	public boolean isEmpty() {
		return this.hashEntries == 0;
	}

	/* (non-Javadoc)
	 * @see java.util.Map#containsKey(java.lang.Object)
	 */
	@LeolaMethod(alias="containsKey")
	@Override
	public boolean containsKey(Object key) {
	    if(key instanceof String) {
	        key = LeoString.valueOf(key.toString());
	    }
	    
		int slot = hashFindSlot((LeoObject)key);		
		return this.hashValues[slot] != null;
	}
	/**
	 * Checks the keys, converts to {@link LeoString}
	 * @param key
	 * @return
	 * @see LeoMap#containsKey(Object)
	 */
	public boolean containsKeyByString(String key) {
		int slot = hashFindSlot(LeoString.valueOf(key));		
		return this.hashValues[slot] != null;
	}

	/* (non-Javadoc)
	 * @see java.util.Map#containsValue(java.lang.Object)
	 */
	@LeolaMethod(alias="containsValue")
	@Override
	public boolean containsValue(Object value) {
		LeoObject val = (LeoObject)value;
		for(int i = 0; i < this.hashKeys.length; i++) {			
			if ( this.hashValues[i] != null) {
				if ( this.hashValues[i].$eq(val) )
					return true;
			}
		}
		
		return false;
	}

	/* (non-Javadoc)
	 * @see java.util.Map#get(java.lang.Object)
	 */
	@LeolaMethod(alias="get")
	@Override
	public LeoObject get(Object key) {
	    if(key instanceof String) {
            key = LeoString.valueOf(key.toString());
        }
	    
		return hashget((LeoObject)key);
	}
	
	/**
	 * Retrieves by key (a java {@link String})
	 * @param key
	 * @return
	 * @see LeoMap#get(Object)
	 */
	public LeoObject getByString(String key) {
		return hashget(LeoString.valueOf(key));
	}

	/* (non-Javadoc)
	 * @see java.util.Map#put(java.lang.Object, java.lang.Object)
	 */
	@Override
	public LeoObject put(LeoObject key, LeoObject value) {
		return hashset(key, value);		
	}
	
	/**
	 * Converts the Java String into a {@link LeoString}
	 * @param key
	 * @param value
	 * @return
	 * @see LeoMap#put(LeoObject, LeoObject)
	 */
	public LeoObject putByString(String key, LeoObject value) {
		return hashset(LeoString.valueOf(key), value);
	}

	/* (non-Javadoc)
	 * @see java.util.Map#remove(java.lang.Object)
	 */
	@LeolaMethod(alias="remove")
	@Override
	public LeoObject remove(Object key) {
	    if(key instanceof String) {
            key = LeoString.valueOf(key.toString());
        }
	    
		return this.hashRemove((LeoObject)key);		
	}

	/**
	 * Removes by key (a java String)
	 * @param key
	 * @return
	 * @see LeoMap#remove(Object)
	 */
	public LeoObject removeByString(String key) {
		return this.hashRemove(LeoString.valueOf(key));
	}
	
	/* (non-Javadoc)
	 * @see java.util.Map#putAll(java.util.Map)
	 */
	@LeolaMethod(alias="putAll")
	@Override
	public void putAll(Map<? extends LeoObject, ? extends LeoObject> m) {
		for(Map.Entry<? extends LeoObject, ? extends LeoObject> entry : m.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}

	/* (non-Javadoc)
	 * @see java.util.Map#clear()
	 */
	@Override
	public void clear() {
		for(int i = 0; i < this.hashKeys.length; i++) {
			this.hashKeys[i] = null;
			this.hashValues[i] = null;
		}
		this.hashEntries = 0;
	}

	/* (non-Javadoc)
	 * @see java.util.Map#keySet()
	 */
	@Override
	public Set<LeoObject> keySet() {
		Set<LeoObject> r = new HashSet<LeoObject>(this.hashEntries);
		for(int i = 0; i < this.hashKeys.length; i++) {
			if ( this.hashKeys[i] != null ) {
				r.add(this.hashKeys[i]);
			}
		}
		return r;
	}

	/* (non-Javadoc)
	 * @see java.util.Map#values()
	 */
	@Override
	public Collection<LeoObject> values() {
		List<LeoObject> r = new ArrayList<LeoObject>(this.hashEntries);
		for(int i = 0; i < this.hashValues.length; i++) {
			if ( this.hashValues[i] != null ) {
				r.add(this.hashValues[i]);
			}
		}
		return r;
	}

	/* (non-Javadoc)
	 * @see java.util.Map#entrySet()
	 */
	@Override
	public Set<java.util.Map.Entry<LeoObject, LeoObject>> entrySet() {
		Set<java.util.Map.Entry<LeoObject, LeoObject>> r = new HashSet<java.util.Map.Entry<LeoObject, LeoObject>>(this.hashEntries);
		for(int i = 0; i < this.hashKeys.length; i++) {
			if ( this.hashKeys[i] != null ) {
				r.add( new Entry(this.hashKeys[i], get(this.hashKeys[i])));
			}
		}
		return r;
	}

	private class Entry implements java.util.Map.Entry<LeoObject, LeoObject> {
		LeoObject key;
		LeoObject val;
		Entry(LeoObject key, LeoObject val) {
			this.key = key;
			this.val = val;
		}

		/* (non-Javadoc)
		 * @see java.util.Map.Entry#getKey()
		 */
		@Override
		public LeoObject getKey() {
			return key;
		}

		/* (non-Javadoc)
		 * @see java.util.Map.Entry#getValue()
		 */
		@Override
		public LeoObject getValue() {
			return val;
		}

		/* (non-Javadoc)
		 * @see java.util.Map.Entry#setValue(java.lang.Object)
		 */
		@Override
		public LeoObject setValue(LeoObject value) {
		    LeoObject oldVal = this.val;
		    put(key, value);
		    this.val = value;
			return oldVal;
		}
		
	}
}

