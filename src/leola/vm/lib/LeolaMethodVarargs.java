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
 * Denote that the native Java method is to accept
 * variable arguments from Leola code.
 * 
 * <pre>
 *   @LeolaMethodVarargs
 *   public String printf(Object str, LeoObject ... args) {
 *     ...
 *   }
 *   
 *   ...
 *   // in Leola
 *   printf("%s, is awesome and so is: %s", "Brett", "Packers")
 *   
 * </pre>
 * 
 * @author Tony
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface LeolaMethodVarargs {	
}
