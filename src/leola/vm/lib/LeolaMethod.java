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
 * Used for aliasing method names
 * 
 * @author chq-tonys
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface LeolaMethod {
	/* Used for overriding the method name */
	String alias();
}
