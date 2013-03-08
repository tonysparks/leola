/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.vm.util;

import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Creates a pool of objects that can be reused.  This class is thread-safe.
 *
 * @author Tony
 *
 */
public class ExpandableObjectPool<V> implements ObjectPool<V> {

    /**
     * Object factory
     */
    private ObjectFactory<V> factory;

    /**
     * List of free objects to be used
     */
    private Queue<V> freeList;

    /**
     * List of objects allocated
     */
    private Collection<V> allocated;

    /**
     * @param factory
     */
    public ExpandableObjectPool(ObjectFactory<V> factory) {
        this.factory = factory;
        this.freeList = new ConcurrentLinkedQueue<V>();
        this.allocated = new ConcurrentLinkedQueue<V>();
    }


    /* (non-Javadoc)
     * @see com.expd.arch.depot.repository.util.ObjectPooll#getPool()
     */
    public Collection<V> getPool() {
        return this.allocated;
    }

    /* (non-Javadoc)
     * @see com.expd.arch.depot.repository.util.ObjectPooll#clear()
     */
    public void clear() {
        this.freeList.clear();
        this.allocated.clear();
    }

    /* (non-Javadoc)
     * @see com.expd.arch.depot.repository.util.ObjectPooll#add(V)
     */
    public void add(V object) {
        this.freeList.add(object);
        this.allocated.add(object);
    }

    /* (non-Javadoc)
     * @see com.expd.arch.depot.repository.util.ObjectPooll#remove(V)
     */
    public void remove(V object) {
        this.freeList.remove(object);
        this.allocated.remove(object);
    }

    /* (non-Javadoc)
     * @see com.expd.arch.depot.repository.util.ObjectPooll#size()
     */
    public int size() {
        return this.freeList.size();
    }
    
    /* (non-Javadoc)
     * @see leola.vm.util.ObjectPool#numberOfAllocations()
     */
    @Override
    public int numberOfAllocations() {    
    	return this.allocated.size();
    }

    /* (non-Javadoc)
     * @see com.expd.arch.depot.repository.util.ObjectPooll#allocate()
     */
    public V allocate() {
        V object = null;
        if ( this.freeList.isEmpty() ) {
            object = this.factory.create();
            this.allocated.add(object);
        }
        else {
            object = this.freeList.poll();
        }

        return object;
    }

    /* (non-Javadoc)
     * @see com.expd.arch.depot.repository.util.ObjectPooll#release(V)
     */
    public void release(V object) {
        this.freeList.add(object);
    }
}

