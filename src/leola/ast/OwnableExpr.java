/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.ast;

/**
 * A variable owned by a class.
 * 
 * @author Tony
 *
 */
public abstract class OwnableExpr extends Expr {

	/**
	 * Owner
	 */
	private String owner;
	private boolean isParent;

	/**
	 * @param owner
	 */
	public OwnableExpr(String owner) {
		this.owner = owner;
		this.isParent = false;
	}

	/**
	 * @return the owner
	 */
	public String getOwner() {
		return owner;
	}

	/**
	 * @param owner the owner to set
	 */
	public void setOwner(String owner) {
		this.owner = owner;
	}
		
	/**
	 * @param isParent the isParent to set
	 */
	public void setParent(boolean isParent) {
		this.isParent = isParent;
	}
	
	/**
	 * @return the isParent
	 */
	public boolean isParent() {
		return isParent;
	}
}

