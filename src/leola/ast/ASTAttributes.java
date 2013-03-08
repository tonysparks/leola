/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.ast;

/**
 * Attributes for an {@link ASTNode}
 * 
 * @author Tony
 *
 */
public interface ASTAttributes {
	
	public static final int
		IS_PROPERTY = (1<<1),
		NAMESPACE_PROPERTY = (1<<1)
		;
	
	/**
	 * Member access
	 */
	public static final String MEMBER_ACCESS = "member";
	
	
}

