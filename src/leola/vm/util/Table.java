/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.vm.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import leola.vm.exceptions.LeolaRuntimeException;
import leola.vm.types.LeoNull;
import leola.vm.types.LeoObject;

/**
 * Taken from LuaJ, represents a simple HashTable
 * 
 * @author Tony
 *
 */
public class Table implements Map<LeoObject, LeoObject> {
	private static final int      MIN_HASH_CAPACITY = 2;
	
	/** the hash keys */
	protected LeoObject[] hashKeys;
	
	/** the hash values */
	protected LeoObject[] hashValues;
	
	/** the number of hash entries */
	protected int hashEntries;
		
	
	/** Construct empty table */
	public Table() {		
		hashKeys = ArrayUtil.EMPTY_LEOOBJECTS;
		hashValues = ArrayUtil.EMPTY_LEOOBJECTS;
		hashEntries = 0;
	}
	
	/** 
	 * Construct table with preset capacity.
	 * @param nhash capacity of hash part
	 */
	public Table(int nhash) {
		this();
		
		presize(nhash);
	}
	
	public void presize(int nhash) {
		if ( nhash > 0 && nhash < MIN_HASH_CAPACITY )
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
	

	/** caller must ensure key is not nil */
	public void set( LeoObject key, LeoObject value ) {			
		rawset(key, value);
	}

	
	/** caller must ensure key is not nil */
	public void rawset( LeoObject key, LeoObject value ) {	
		hashset( key, value );
	}



	public int length() {
		return this.hashEntries;
	}


	private void error(String error) {
		throw new LeolaRuntimeException(error);
	}

	public int nexti( LeoObject key ) {
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
	
	public LeoObject nextKey(LeoObject key) {
		int i = nexti(key);
		return hashKeys[i];
	}
	
	public LeoObject nextValue(LeoObject key) {
		int i = nexti(key);
		return hashValues[i];
	}	
	

	/**
	 * Set a hashtable value
	 * @param key key to set
	 * @param value value to set
	 */
	public LeoObject hashset(LeoObject key, LeoObject value) {
		LeoObject r = LeoNull.LEONULL;
		if ( value == LeoNull.LEONULL )
			r = hashRemove(key);
		else {
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
	public int hashFindSlot(LeoObject key) {		
		int i = ( key.hashCode() & 0x7FFFFFFF ) % hashKeys.length;
		
		// This loop is guaranteed to terminate as long as we never allow the
		// table to get 100% full.
		LeoObject k;
		while ( ( k = hashKeys[i] ) != null && k.$neq(key) ) {
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

	
	// equality w/ metatable processing	
	public boolean equal( Object val )  {
		if ( val == null ) return false;
		if ( this == val ) return true;
		if ( getClass() != val.getClass() ) return false;
		Table t = (Table)val;
		return  t.hashEntries==hashEntries && t.hashKeys.equals(hashKeys) && t.hashValues.equals(hashValues);
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
	@Override
	public boolean containsKey(Object key) {
		int slot = hashFindSlot((LeoObject)key);		
		return this.hashValues[slot] != null;
	}

	/* (non-Javadoc)
	 * @see java.util.Map#containsValue(java.lang.Object)
	 */
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
	@Override
	public LeoObject get(Object key) {
		return hashget((LeoObject)key);
	}

	/* (non-Javadoc)
	 * @see java.util.Map#put(java.lang.Object, java.lang.Object)
	 */
	@Override
	public LeoObject put(LeoObject key, LeoObject value) {
		return hashset(key, value);		
	}

	/* (non-Javadoc)
	 * @see java.util.Map#remove(java.lang.Object)
	 */
	@Override
	public LeoObject remove(Object key) {
		return this.hashRemove((LeoObject)key);		
	}

	/* (non-Javadoc)
	 * @see java.util.Map#putAll(java.util.Map)
	 */
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
		return null;
	}
	
}

