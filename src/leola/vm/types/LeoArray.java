/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.vm.types;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;

import leola.vm.exceptions.LeolaRuntimeException;
import leola.vm.lib.LeolaMethod;


/**
 * An expandable array that can act as a Stack or a randomly accessed array.  The underlying implementation
 * is almost identical to an {@link ArrayList}, the major difference is that it only accepts {@link LeoObject}'s
 * as the contained object.
 * 
 * <p>
 * All optional methods are implemented from the {@link List} interface and {@link Iterator}.
 *
 * @author Tony
 *
 */
public class LeoArray extends LeoObject implements List<LeoObject> {
    
    
    /**
     * Copies the elements of supplied {@link Collection} into a {@link LeoArray}.
     * 
     * @param list the list of elements to be converted/copied into the {@link LeoArray}
     * @return the new {@link LeoArray} containing the elements from the {@link Collection}
     */
    public static LeoArray toArray(Collection<?> list) {
        LeoArray array = new LeoArray(list.size());
        for(Object o : list) {
            array.add(LeoObject.valueOf(o));
        }
        return array;
    }
    
    /**
     * Converts the raw array to a {@link LeoArray}
     * 
     * @param leoObjects
     * @return a new {@link LeoArray} consisting of the supplied objects
     */
    public static LeoArray newLeoArray(LeoObject ...leoObjects ) {
        LeoArray array = null;
        if ( leoObjects != null ) {
            array = new LeoArray(leoObjects);           
        }
        else {
            array = new LeoArray();
        }
        
        return array;
    }
    
    
	private LeoObject[] array;
	private int size;
	
	/**
	 */
	public LeoArray() {
		this(10);
	}

	/**
	 * @param initialSize
	 */
	public LeoArray(int initialSize) {
		super(LeoType.ARRAY);

		this.size = 0;
		this.array = new LeoObject[initialSize];	
		clear();
	}
	
	/**
	 * @param array
	 */
	public LeoArray(List<LeoObject> array) {
		super(LeoType.ARRAY);

		this.size = array.size();
		this.array = new LeoObject[this.size];
		
		for(int i = 0; i < this.size; i++) {
			this.array[i] = array.get(i);
		}
	}

	/**
	 * @param a
	 */
	public LeoArray(LeoObject[] a) {
		this(a, a.length);
	}
	
	/**
	 * @param a
	 * @param size
	 */
	public LeoArray(LeoObject[] a, int size) {
		super(LeoType.ARRAY);
		
		this.array = a;
		this.size = size;
	}

	/**
	 * Adds ability to reference the public API of this class
	 */
    private Map<LeoObject, LeoObject> arrayApi;	
	private LeoObject getNativeMethod(LeoObject key) {
	    if(this.arrayApi == null) {
	        this.arrayApi = new LeoMap();
	    }
	    return getNativeMethod(this, this.arrayApi, key);
	}
	

	
	/**
	 * Iterates through the array given the supplied bounds
	 * 
	 * <pre>
	 *     [1,2,3,4].for(1,3,println)
	 *     // prints 
	 *     // 2
	 *     // 3
	 * </pre>
	 * 
	 * @param startIndex
	 * @param endIndex
	 * @param function
	 */
	@LeolaMethod(alias="for")
	public void _for(int startIndex, int endIndex, LeoObject function) {	    
        if ( startIndex < 0 || endIndex > size()) {
            throw new LeolaRuntimeException("Invalid array index: " + startIndex + " to " + endIndex + "[Size of array: " + size() + "]");
        }

        for(int i = startIndex; i < endIndex; i++) {
            LeoObject result = function.call(get(i)); 
            if ( LeoObject.isTrue(result) ) {
                break;
            }
        }
	}
	
	
	/**
	 * Sorts the Array, it sorts it in place (meaning this will <b>not</b> allocate
	 * a new array, but rather sort this instance of the array).
	 * 
	 * <pre>
	 *    println( [1,5,2,87,324,4,2].sort(def(a,b) return a-b) )
	 *    // prints:
	 *    // [1,2,2,4,5,87,324]
	 * 
	 * @param function
	 * @return this array sorted
	 */
	public LeoArray sort(final LeoObject function) {
	    if(function != null) {
            Arrays.sort(this.array, 0, this.size, new Comparator<LeoObject>() {                
                @Override
                public int compare(LeoObject o1, LeoObject o2) {
                    LeoObject result = function.call(o1, o2);
                    return result.asInt();
                }
            });
        }
	    else {
	        Arrays.sort(this.array, 0, this.size);
	    }
	    
	    return this;
	}
	
	/**
	 * Iterates through the array, invoking the supplied 
	 * function object for each element
	 * 
	 * <pre>
	 *   [1,2,3].foreach(println)
	 *   // prints:
	 *   // 1
	 *   // 2
	 *   // 3
	 * </pre>
	 * 
	 * @param function
	 */
	public void foreach(LeoObject function) {
	    if(function != null) {
    	    for(int i = 0; i < this.size; i++) {
    	        LeoObject obj = get(i);
    	        LeoObject result = function.call(obj);
    	        if(LeoObject.isTrue(result)) {
    	            break;
    	        }
    	    }
	    }
	}
	
	/**
	 * Filters the {@link LeoArray}
	 * 
	 * <pre>
	 *     var evens = [1,2,3,4].filter(def(e) {
	 *         return e % 2 == 0
	 *     }) 
	 *     println(evens) // prints, 2, 4
	 * </pre>
	 * 
	 * @param function
	 * @return a resulting {@link LeoArray}
	 */
	public LeoArray filter(LeoObject function) {
	    if(function != null) {
	        LeoArray array = new LeoArray();
            for(int i = 0; i < this.size; i++) {
                LeoObject obj = get(i);
                if( LeoObject.isTrue(function.call(obj)) ) {
                    array.add(obj);
                }
            }
            return array;
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
	 * @return
	 */
	public LeoArray map(LeoObject function) {
        if(function != null) {
            LeoArray array = new LeoArray(this.size);
            for(int i = 0; i < this.size; i++) {                
                LeoObject obj = get(i);
                LeoObject result = function.call(obj);
                
                array.add(result);
            }
            return array;
        }
        return this;
    }
	
	
	/**
	 * Reduces all of the elements in this array into one value.
	 * 
	 * <pre>
	 *     var sum = [1,2,3,4].reduce(def(p,n) return p+n)
	 *     println(sum) // 10
	 * </pre>
	 * 
	 * 
	 * @param function
	 * @return
	 */
	public LeoObject reduce(LeoObject function) {
	    if(function != null && !isEmpty()) {
            
            LeoObject result = get(0);
            for(int i = 1; i < this.size; i++) {            
                LeoObject obj = get(i);
                result = function.call(result, obj);
            }
            return result;
        }
        return LeoNull.LEONULL;
	}
	
	/* (non-Javadoc)
	 * @see leola.types.LeoObject#isArray()
	 */
	@Override
	public boolean isArray() {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see leola.types.LeoObject#toString()
	 */
	@Override
	public String toString() {
        if (this.array == null)
            return "[]";
        int iMax = this.size - 1;
        if (iMax == -1)
            return "[]";

        StringBuilder b = new StringBuilder();
        b.append('[');
        for (int i = 0; ; i++) {
            LeoObject v = this.array[i];
            if(v.isString()) {
                b.append("\"").append(this.array[i].toString()).append("\"");
            }
            else if(v.isNull()) {
                b.append("null");
            }
            else {
                b.append(this.array[i].toString());
            }
            
            if (i == iMax)
            	return b.append(']').toString();
            b.append(", ");
        }        
	}

	/**
	 * Adds an object
	 * @param obj
	 */
	@Override
	public LeoObject $add(LeoObject obj) {
		add(obj);
		return this;
	}	
	
	public void addAll(LeoObject other) {
		if ( other.isOfType(LeoType.ARRAY)) {
			LeoArray aOther = other.as();
			addAll((Collection<LeoObject>)aOther);
		}
		else {
			$add(other);
		}
	}
	
	/**
	 * Adds an object to the array
	 * @param obj
	 */
	public void push(LeoObject obj) {
		add(obj);
	}

	/**
	 * Removes an object.
	 * @param obj
	 */
	@LeolaMethod(alias="remove")
	public boolean remove(LeoObject obj) {
		return this._remove(obj);
	}
	
	/* (non-Javadoc)
	 * @see leola.types.LeoObject#sub(leola.types.LeoObject)
	 */
	@Override
	public LeoObject $sub(LeoObject other) {
		this._remove(other);
		return this;
	}
	
	public void removeAll(LeoObject other) {
		if ( other.isOfType(LeoType.ARRAY)) {
			LeoArray aOther = other.as();
			this.removeAll((Collection<?>)aOther);
		}
		else {
			_remove(other);
		}
	}
	/**
	 * Clears the array
	 */
	public void clear() {
	    for(int i = 0; i < this.size; i++ ) {
	    	this.array[i] = LeoNull.LEONULL;
	    }
	    this.size = 0;
	}

	/**
	 * @return
	 */
	public LeoObject pop() {
		return this.remove(this.size-1);
	}

	/**
	 * @return
	 */
	public LeoObject peek() {
		return this.get(this.size-1);
	}

	/**
	 * Gets an element
	 * @param i
	 * @return
	 */
	@LeolaMethod(alias="get")
	public LeoObject get(int i) {
		return this.array[i];
	}

	/**
	 * @return the size of the array
	 */
	public int size() {
		return this.size;
	}

	/**
	 * @return
	 */
	public boolean empty() {
		return this.size == 0;
	}
	
	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#$index(double)
	 */
	@Override
	public LeoObject $index(double other) {
	    return get( (int) other);
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
	    return get( (int)other );
	}
	
	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#$index(leola.vm.types.LeoObject)
	 */
	@Override
	public LeoObject $index(LeoObject other) {
	    return get(other.asInt());
	}
	
	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#$index(leola.vm.types.LeoObject, leola.vm.types.LeoObject)
	 */
	@Override
	public void $sindex(LeoObject key, LeoObject other) {
	    set(key, other);
	}

	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#setObject(leola.vm.types.LeoObject, leola.vm.types.LeoObject)
	 */
	@Override
	public void setObject(LeoObject key, LeoObject value) {	
//		set(key, value);
	    getNativeMethod(key);
	    this.arrayApi.put(key, value);	    
	}
	
	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#getObject(leola.vm.types.LeoObject)
	 */
	@Override
	public LeoObject getObject(LeoObject key) {
//		return get(key.asInt());
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
	 * Sets an element
	 * @param index
	 * @param obj
	 */
	public LeoObject set(int index, LeoObject obj) {
		return this.array[index] = obj;
	}

	/**
	 * Sets the element at the provided index
	 * @param index
	 * @param obj
	 */
	public void set(LeoObject index, LeoObject obj) {
		this.array[index.asInt()] = obj;
	}

	public LeoObject reverse() {
		LeoArray result = new LeoArray(this.size);
		for(int i = this.size-1; i >=0; i--) {
			result.add(get(i));
		}
		
		return result;
	}
	
	/**
	 * gets a sublist
	 * @param start
	 * @param end
	 * @return
	 */
	public LeoArray slice(int start, int end) {
		if(start>end) {
			throw new LeolaRuntimeException("Can't slice an array with start > end");			
		}
		
		LeoObject[] slice = new LeoObject[end-start];
		System.arraycopy(this.array, start, slice, 0, slice.length);
		
		return new LeoArray(slice);
	}

	/**
	 * gets a sublist
	 * @param start
	 * @param end
	 * @return
	 */
	public LeoArray tail(int start) {
		return slice(start, this.size);
	}

	/**
	 * @return the first element
	 */
	public LeoObject first() {
		return this.array[0];
	}
	/**
	 * @return the last element
	 */
	public LeoObject last() {
		if(this.array.length > 0) {
			return this.array[this.size-1];
		}
		return LeoNull.LEONULL;
	}

	/**
	 * Truncates the first element and returns the rest of the array.
	 * @return
	 */
	public LeoObject rest() {
		if (this.size < 2 ) return LeoNull.LEONULL;
		return slice(1, this.size);
	}

	/**
	 * @return the array
	 */
	public List<LeoObject> getArray() {
		return this;
	}

	/**
	 * @param value
	 * @return
	 */
	public boolean has(LeoObject value) {
		for(int i = 0; i < this.size; i++) {
			LeoObject l = this.array[i];
			if ( l != null && l.$eq(value)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return a native array representation
	 */
	public LeoObject[] toArray() {
	    LeoObject[] clone = new LeoObject[this.size];
	    System.arraycopy(this.array, 0, clone, 0, clone.length);
		return clone;
	}
	
	/**
     * @return a native array representation
     */
    public LeoObject[] getRawArray() {
        return this.array;
    }
	
	/* (non-Javadoc)
	 * @see leola.types.LeoObject#clone()
	 */
	@Override
	public LeoObject clone() {
		return new LeoArray(this.array, this.size);
	}

	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#$bsl(double)
	 */
	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#$bsl(leola.vm.types.LeoObject)
	 */
	@Override
	public LeoObject $bsl(LeoObject other) {	
		addAll(other);
		return this;
	}
	
	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#$bsr(leola.vm.types.LeoObject)
	 */
	@Override
	public LeoObject $bsr(LeoObject other) {
		removeAll(other);
		return this;
	}
	
	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#$xor(leola.vm.types.LeoObject)
	 */
	@Override
	public LeoObject $xor(LeoObject other) {	
		return has(other) ? LeoBoolean.LEOTRUE : LeoBoolean.LEOFALSE;
	}
	
	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#$neq(leola.vm.types.LeoObject)
	 */
	@Override
	public boolean $neq(LeoObject other) {
	    return ! $eq(other);
	}
	
	/* (non-Javadoc)
	 * @see leola.types.LeoObject#eq(leola.types.LeoObject)
	 */
	@Override
	public boolean $eq(LeoObject other) {
		if ( other != null && other.isOfType(LeoType.ARRAY)) {
			LeoArray otherarray = other.as();
			if ( otherarray.size == this.size) {
				for(int i = 0; i < this.size; i++) {
					LeoObject l = this.array[i];
					LeoObject r = otherarray.array[i];
					if ( ! LeoObject.$eq(l, r) ) {
						return false;
					}
				}

				return true;
			}			
		}

		return false;

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
		out.writeInt(this.size);
		for(int i = 0; i < this.size; i++) {			
			this.array[i].write(out);
		}
	}
	
	/**
	 * Reads from the {@link DataInput} stream, constructing a {@link LeoObject}
	 * 
	 * @param in
	 * @return the {@link LeoObject}
	 * @throws IOException
	 */
	public static LeoArray read(LeoObject env, DataInput in) throws IOException {
		int size = in.readInt();
		LeoArray result = new LeoArray(size);
		for(int i = 0; i < size; i++) {
			LeoObject obj = LeoObject.read(env, in);
			result.$add(obj);
		}
		
		return result;
	}

	/* (non-Javadoc)
	 * @see java.util.List#isEmpty()
	 */	
	public boolean isEmpty() {
		return this.size == 0;
	}

	/* (non-Javadoc)
	 * @see java.util.List#contains(java.lang.Object)
	 */
	@LeolaMethod(alias="contains")
	public boolean contains(Object o) {
		return this.has(LeoObject.valueOf(o));
	}

	/* (non-Javadoc)
	 * @see java.util.List#iterator()
	 */
	
	public Iterator<LeoObject> iterator() {
		return new LeoArrayListIterator(this, 0);
	}

	
	
	/* (non-Javadoc)
	 * @see java.util.List#toArray(T[])
	 */
	
	@SuppressWarnings("unchecked")
	public <T> T[] toArray(T[] a) {
		if(a.length < this.size) {
			a = (T[])Array.newInstance(a.getClass().getComponentType(), this.size);
		}
		
		for(int i = 0; i < this.size; i++) {			
			a[i] = (T)this.array[i].getValue();
		}
		return a;
	}
	
	

	private void ensureCapacity(int minCapacity) {
		
		int oldCapacity = this.array.length;
		if (minCapacity > oldCapacity) {
		    LeoObject oldData[] = this.array;
		    int newCapacity = (oldCapacity * 3)/2 + 1;
		    
	    	if (newCapacity < minCapacity) newCapacity = minCapacity;
	    	
	    	this.array = new LeoObject[newCapacity];
	    	System.arraycopy(oldData, 0, this.array, 0, oldCapacity);	 
	    	for(int i = this.size; i < newCapacity; i++) {
	    		this.array[i] = LeoNull.LEONULL;
	    	}
		}
	}
	
	/* (non-Javadoc)
	 * @see java.util.List#add(java.lang.Object)
	 */
	
	public boolean add(LeoObject e) {
		ensureCapacity(this.size + 1);
		this.array[this.size++] = e;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.util.List#remove(java.lang.Object)
	 */
	
	public boolean remove(Object o) {
		return _remove(o);
    }

	private boolean _remove(Object o) {		
	    for (int index = 0; index < size; index++) {
			if (o.equals(this.array[index])) {
			    fastRemove(index);
			    return true;
			}
	    }
    
		return false;
	}
	
    /*
     * Private remove method that skips bounds checking and does not
     * return the value removed.
     */
    private void fastRemove(int index) {
        
        int numMoved = size - index - 1;
        if (numMoved > 0)
            System.arraycopy(array, index+1, array, index,
                             numMoved);
        array[--size] = LeoNull.LEONULL;
    }

	/* (non-Javadoc)
	 * @see java.util.List#containsAll(java.util.Collection)
	 */
	
	public boolean containsAll(Collection<?> c) {
		boolean containsAll = true;
		for(Object o : c) {
			if( ! contains(o) ) {
				return false;
			}
		}
		
		return containsAll;
	}

	/* (non-Javadoc)
	 * @see java.util.List#addAll(java.util.Collection)
	 */
	
	public boolean addAll(Collection<? extends LeoObject> c) {
		ensureCapacity(this.size + c.size());
		for(LeoObject o : c) {
			this.add(o);
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see java.util.List#addAll(int, java.util.Collection)
	 */
	
	public boolean addAll(int index, Collection<? extends LeoObject> c) {
		ensureCapacity(this.size + c.size());
		
		for(LeoObject o : c) {
			add(index++, o);
		}
		
		return true;
	}

	/* (non-Javadoc)
	 * @see java.util.List#removeAll(java.util.Collection)
	 */
	
	public boolean removeAll(Collection<?> c) {
		for(Object o : c) {
			this._remove(o);
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see java.util.List#retainAll(java.util.Collection)
	 */
	
	public boolean retainAll(Collection<?> c) {
		List<LeoObject> objectsToRemove = new ArrayList<LeoObject>();
		for(int i = 0; i < this.size; i++) {
			LeoObject o = this.array[i];
			if(o != null) {
				if(!c.contains(o)) {
					objectsToRemove.add(o);
				}
			}
		}
		
		return this.removeAll(objectsToRemove);		
	}

	/* (non-Javadoc)
	 * @see java.util.List#add(int, java.lang.Object)
	 */
	
	public void add(int index, LeoObject element) {
		ensureCapacity(size+1); 
		System.arraycopy(this.array, index, this.array, index + 1, size - index);
		this.array[index] = element;
		size++;
	}

	/* (non-Javadoc)
	 * @see java.util.List#remove(int)
	 */
	
	public LeoObject remove(int index) {
		LeoObject oldValue = this.array[index];

		int numMoved = size - index - 1;
		if (numMoved > 0)
		    System.arraycopy(this.array, index+1, this.array, index, numMoved);
		this.array[--size] = LeoNull.LEONULL;
		return oldValue;
	}

	/* (non-Javadoc)
	 * @see java.util.List#indexOf(java.lang.Object)
	 */
	
	public int indexOf(Object o) {	
	    for (int i = 0; i < size; i++) {
			if (o.equals(this.array[i]))
			    return i;
	    }
	
		return -1;
	}

	/* (non-Javadoc)
	 * @see java.util.List#lastIndexOf(java.lang.Object)
	 */
	
	public int lastIndexOf(Object o) {	
	    for (int i = size-1; i >= 0; i--) {
			if (o.equals(this.array[i]))
			    return i;
	    }	
		return -1;
	}

	/* (non-Javadoc)
	 * @see java.util.List#listIterator()
	 */
	
	public ListIterator<LeoObject> listIterator() {
		return new LeoArrayListIterator(this, 0);
	}

	/* (non-Javadoc)
	 * @see java.util.List#listIterator(int)
	 */
	
	public ListIterator<LeoObject> listIterator(int index) {
		return new LeoArrayListIterator(this, index);
	}

	/* (non-Javadoc)
	 * @see java.util.List#subList(int, int)
	 */
	
	public List<LeoObject> subList(int fromIndex, int toIndex) {
		return slice(fromIndex, toIndex);
	}
	
	static class LeoArrayListIterator implements ListIterator<LeoObject> {
		private int cursor;
		private int lastRet;
		private LeoArray array;
		
		
		/**
		 * 
		 */
		public LeoArrayListIterator(LeoArray leoArray, int index) {
			this.array = leoArray;			
			this.cursor = index;
			this.lastRet = -1;
			
		}
		
		@Override
		public boolean hasNext() {
			return cursor < array.size;
		}

		@Override
		public LeoObject next() {
			int i = cursor;
            if (i >= array.size)
                throw new NoSuchElementException();
                                    
            cursor = i + 1;
            return array.array[(lastRet = i)];			
		}

		@Override
		public boolean hasPrevious() {
			return cursor > 0;
		}

		@Override
		public LeoObject previous() {
			
            int i = cursor - 1;
            if (i < 0)
                throw new NoSuchElementException();            
            cursor = i;
            return array.array[(lastRet = i)];			
		}

		@Override
		public int nextIndex() {
			return cursor;
		}

		@Override
		public int previousIndex() {
			return cursor-1;
		}

		@Override
		public void remove() {
	         if (lastRet < 0)
                 throw new IllegalStateException();
             
             try {
                 
                 array.remove(lastRet);
                 cursor = lastRet;
                 lastRet = -1;
                 
             } catch (IndexOutOfBoundsException ex) {
                 throw new ConcurrentModificationException();
             }
			
			
		}

		@Override
		public void set(LeoObject e) {
            if (lastRet < 0)
                throw new IllegalStateException();            

            try {
            	array.set(lastRet, e);
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
		}

		@Override
		public void add(LeoObject e) {
            try {
                int i = cursor;                
                array.add(i, e);
                cursor = i + 1;
                lastRet = -1;
                
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
		}
	};
}

