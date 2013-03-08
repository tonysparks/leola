/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.vm.util;

import java.util.Collection;

/**
 * @author Tony
 *
 * @param <V>
 */
public interface ObjectPool<V> {

    /**
     * The raw underlying pool.  This should only be used to cleanup the
     * resource upon shutdown of the system.  The collection returned reflects
     * the live pool, and therefore may have concurrent actions on the raw collection.
     *
     * @return the raw pool.
     */
    public Collection<V> getPool();

    /**
     * Releases all references to the allocated objects
     */
    public void clear();

    /**
     * Add on object to the pool.
     * @param object
     */
    public void add(V object);

    /**
     * Removes the object from the pool forever.
     * @param object
     */
    public void remove(V object);

    /**
     * @return the current size of the pool
     */
    public int size();

    /**
     * @return the total number of allocations made
     */
    public int numberOfAllocations();
    
    /**
     * Allocates a new object if non are available in the pool.  If there are objects
     * available it will retrieve one.
     * @return
     */
    public V allocate();

    /**
     * Place this object back in the pool so it can be consumed again.
     * @param object
     */
    public void release(V object);

}

