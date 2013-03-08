/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.vm.util;

/**
 * Creates new objects
 *
 * @see ExpandableObjectPool
 * @author Tony
 *
 */
public interface ObjectFactory<V> {

    /**
     * @return a new object
     */
    public V create();
}

