/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.lang;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentMap;

import leola.vm.Leola;
import leola.vm.exceptions.LeolaRuntimeException;
import leola.vm.lib.LeolaIgnore;
import leola.vm.lib.LeolaLibrary;
import leola.vm.lib.LeolaMethod;
import leola.vm.types.LeoArray;
import leola.vm.types.LeoGenerator;
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
    public void init(Leola leola, LeoNamespace namespace) throws LeolaRuntimeException {
        this.runtime = leola;
        runtime.putIntoNamespace(this, namespace);
    }

    /**
     * Converts the optional supplied {@link LeoMap} into a {@link ConcurrentMap}.
     * 
     * @param map
     * @return the {@link ConcurrentMap}
     */
    public ConcurrentMap<LeoObject, LeoObject> concurrentMap(LeoMap map) {
        if(map == null) {
            map = new LeoMap();
        }
        
        /* Since the LeoMap class does not use an internal java.util.Map structure;
         * we have to add in some of the common API's to the concurrentMap.  This
         * is fairly sub-optimal from a development/maintenance perspective...
         */
        return new ConcurrentHashMap<LeoObject, LeoObject>(map) {            
            private static final long serialVersionUID = 6526027801906112095L;

            @LeolaMethod(alias="$sindex")
            public LeoObject $sindex(LeoObject key, LeoObject value) {
                return put(key,value);
            }
            
            @LeolaMethod(alias="$index")
            public LeoObject $index(LeoObject key) {
                return get(key);
            }
            
            @LeolaMethod(alias="has")
            public boolean has(LeoObject key) {
                return this.containsKey(key);
            }

            @LeolaMethod(alias="empty")
            public boolean empty() {
                return this.isEmpty();
            }
            
            @LeolaMethod(alias="vals")
            public LeoArray vals() {
                return new LeoArray(new ArrayList<LeoObject>(this.values()));
            }
            
            @LeolaMethod(alias="foreach")
            public void foreach(LeoObject function) {
                if(function != null) {
                    for(Map.Entry<LeoObject, LeoObject> entry : this.entrySet()) {
                        LeoObject key = entry.getKey();
                        if(key != null) {
                            LeoObject value = entry.getValue();
                            LeoObject result = function.xcall(key, value);
                            if(LeoObject.isTrue(result)) {
                                break;
                            }
                        }
                    }
                }
            }
            
            @LeolaMethod(alias="filter")
            public ConcurrentMap<LeoObject, LeoObject> filter(LeoObject function) {
                if(function != null) {
                    ConcurrentMap<LeoObject, LeoObject> map = concurrentMap(null);
                    for(Map.Entry<LeoObject, LeoObject> entry : this.entrySet()) {
                        LeoObject key = entry.getKey();
                        if(key != null) {
                            LeoObject value = entry.getValue();
                            if( LeoObject.isTrue(function.xcall(key, value)) ) {
                                map.put(key, value);
                            }
                        }
                    }
                    return map;
                }
                return this;
            }
            
            @LeolaMethod(alias="map")
            public ConcurrentMap<LeoObject, LeoObject> map(LeoObject function) {
                if(function != null) {
                    ConcurrentMap<LeoObject, LeoObject> map = concurrentMap(null);
                    for(Map.Entry<LeoObject, LeoObject> entry : this.entrySet()) {
                        LeoObject key = entry.getKey();
                        if(key != null) {
                            LeoObject value = entry.getValue();
                            value = function.xcall(key, value);
                            map.put(key, value);                    
                        }
                    }
                    return map;
                }
                return this;
            }
            
        };
    }
    
    /**
     * Creates a concurrent Deque data structure based off of the data in the {@link LeoArray}
     * 
     * @param array
     * @return the concurrent data structure
     */
    public ConcurrentLinkedDeque<LeoObject> concurrentDeque(LeoArray array) {
        if(array!=null && !array.isEmpty()) {
            return new ConcurrentLinkedDeque<LeoObject>(array);
        }
        
        return new ConcurrentLinkedDeque<LeoObject>();
    }
    
    /**
     * Creates a concurrent {@link Set} data structure based off of the data in the {@link LeoArray}
     * 
     * @param array
     * @return the concurrent {@link Set} data structure
     */
    public Set<LeoObject> concurrentSet(LeoArray array) {
        Set<LeoObject> set = Collections.newSetFromMap(new ConcurrentHashMap<LeoObject, Boolean>());
        if(array!=null && !array.isEmpty()) {            
            for(int i = 0; i < array.size(); i++) {
                LeoObject v = array.get(i);
                set.add(v);
            }
        }
        
        return set;
    }
    
    /**
     * Repeats the function N times
     * 
     * @param n
     * @param function
     */
    public final void repeat(int n, LeoObject function){
        for(int i = 0; i < n; i++) {
            LeoObject result = function.xcall(LeoInteger.valueOf(i));                    
            if ( LeoObject.isTrue(result) ) {
                break;
            }
        }        
        
    }    
    
    /**
     * returns the length of the array
     *
     * @param array
     * @return the length
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
     * @param list
     * @param function
     * @return the object returned from the function is true 
     */
    public final LeoObject foreach(LeoObject list, LeoObject function) {
        LeoObject result = LeoNull.LEONULL;
        if(list != null) {
            switch(list.getType()) {
                case ARRAY: {
                    LeoArray array = list.as();
                    result = array.foreach(function);                                    
                    break;
                }
                case MAP: {
                    LeoMap map = list.as();
                    result = map.foreach(function);                    
                    break;
                }
                case STRING: {
                    LeoString str = list.as();
                    result = str.foreach(function);                    
                    break;
                }
                case GENERATOR: {
                    LeoGenerator gen = list.as();
                    result = gen.foreach(function);
                    break;
                }
                default: {                    
                }
            }
        }
        
        return result;
    }

    /**
     * Iterates through the array.
     * 
     * @param obj
     * @param start
     * @param end
     * @param function
     */
    @LeolaMethod(alias="for")
    public void __for(LeoObject obj, int start, int end, LeoObject function) {
        switch(obj.getType()) {
            case ARRAY: {
                LeoArray array = obj.as();
                array._for(start, end, function);
                break;
            }
            case STRING: {
                LeoString string = obj.as();
                string._for(start, end, function);
                break;
            }
            default:
        }
                
    }

    /**
     * Iterates through the array invoking the call back filling the
     * array
     *
     * @param array
     * @param function
     * @return the supplied array
     */
    public final LeoArray fill(LeoArray array, LeoObject function) {
        return array.fill(function);
    }
    
    private void checkTypes(LeoObject start, LeoObject end) {
        if(start.getType() != end.getType()) {
            throw new LeolaRuntimeException("Ranges do not match in type: " + start.getType() + " vs. " + end.getType());
        }
    }
    
    /**
     * Constructs a list given the range.
     * 
     * @param start
     * @param end
     * @return the {@link LeoArray} of the range
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
     * @return the filtered list
     */
    public final LeoObject filter(LeoObject list, LeoObject function) {
        LeoObject result = LeoNull.LEONULL;

        switch(list.getType()) {
            case STRING: {
                LeoString string = list.as();
                result = string.filter(function);
                break;
            }
            case ARRAY: {
                LeoArray array = list.as();                                                    
                result = array.filter(function);                
                break;
            }
            case MAP: {
                LeoMap map = list.as();                        
                result = map.filter(function);                
                break;
            }
            case GENERATOR: {
                LeoGenerator gen = list.as();
                result = gen.filter(function);                
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
     * @return the resulting list
     */
    public final LeoObject map(LeoObject list, LeoObject function) {
        LeoObject result = LeoNull.LEONULL;

        switch(list.getType()) {
            case STRING: {
                LeoString string = list.as();
                result = string.map(function);
                break;
            }
            case ARRAY: {
                LeoArray array = list.as();        
                result = array.map(function);
                break;
            }
            case MAP: {
                LeoMap map = list.as();
                result = map.map(function);
                break;
            }
            case GENERATOR: {
                LeoGenerator gen = list.as();
                result = gen.map(function);                
                break;
            }
            default: {                
            }
        }        
        
        return result;
    }
    
    /**
     * Reduces all of the values in the list into one value.
     * 
     * @param list (either an Array or Generator)
     * @param function
     * @return the resulting object
     */
    public final LeoObject reduce(LeoObject list, LeoObject function) {
        LeoObject result = LeoNull.LEONULL;
            
        switch(list.getType()) {
            case ARRAY: {
                LeoArray array = list.as();                    
                result = array.reduce(function);                                            
                break;
            }
            case GENERATOR: {
                LeoGenerator gen = list.as();
                result = gen.reduce(function);
                break;
            }
            default: {                
            }
        }        
        
        return result;
    }
}

