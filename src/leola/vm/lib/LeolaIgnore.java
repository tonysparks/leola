/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.vm.lib;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Ignore a method
 * 
 * @author Tony
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface LeolaIgnore {
}
