/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.ast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import leola.frontend.EvalException;

/**
 * Abstract Syntax tree node
 *
 * @author Tony
 *
 */
public abstract class ASTNode {

	/**
	 * Attributes
	 */
	private Map<String, Object>  attributes;

	/**
	 * Children
	 */
	private List<ASTNode> children;

	/**
	 * The line number
	 */
	private int lineNumber;
	
	/**
	 * Flags
	 */
	private int flags;
	
	/**
	 * Member access flag
	 */
	private boolean isMemberAccess;
	
	/**
	 * The parent node
	 */
	private ASTNode parentNode;
	
	/**
	 * The source in which this node was created
	 */
	private String source;
	
	/**
	 */
	protected ASTNode() {
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return (this.attributes != null) ? this.attributes.toString() : ""
			+ "[ " + this.children != null ? this.children.toString() : "" + " ]" + " @ line: " + this.lineNumber;
	}
	/**
	 * @return the attributes
	 */
	public Map<String, Object> getAttributes() {
		if ( this.attributes == null ) {
			this.attributes = new HashMap<String, Object>();
		}

		return attributes;
	}

	/**
	 * @return true if this node has no parent nodes (aka is the root)
	 */
	public boolean isRootNode() {
		return this.parentNode == null;
	}
	
	/**
	 * @return the parentNode
	 */
	public ASTNode getParentNode() {
		return parentNode;
	}
	
	/**
	 * @param parentNode the parentNode to set
	 */
	public void setParentNode(ASTNode parentNode) {
		this.parentNode = parentNode;
	}
	
	/**
	 * @return true if the parent of this node is a member access (foo.bar)
	 */
	public boolean isMemberAccessChild() {
		return this.isMemberAccess || 
			   this.parentNode instanceof MemberAccessExpr ||
		       this.parentNode instanceof NamespaceAccessExpr ||
		       this.parentNode instanceof ChainedMemberAccessExpr;
	}
	
	/**
	 * @param isMemberAccess the isMemberAccess to set
	 */
	public void setMemberAccess(boolean isMemberAccess) {
		this.isMemberAccess = isMemberAccess;
	}
	
	/**
	 * Sets the parent of the supplied node to this node
	 * @param node
	 * @return the node passed in
	 */
	protected <T extends ASTNode> T becomeParentOf(T node) {
		if(node !=null ) {
			node.setParentNode(this);
		}
		return node;
	}

	
	/**
	 * @param lineNumber the lineNumber to set
	 */
	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}
	
	/**
	 * @return the lineNumber
	 */
	public int getLineNumber() {
		return lineNumber;
	}
	
	/**
	 * @param sourceLine
	 */
	public void setSourceLine(String sourceLine) {
		this.source = sourceLine;
	}
	
	/**
	 * @return the sourceFile
	 */
	public String getSourceLine() {
		return source;
	}
	
	/**
	 * @return the flags
	 */
	public int getFlags() {
		return flags;
	}
	
	/**
	 * @param flags the flags to set
	 */
	public void setFlags(int flags) {
		this.flags = flags;
	}
	
	public void appendFlag(int flag) {
		this.flags |= flag;
	}
	
	public boolean hasFlag(int flag) {
		return (this.flags & flag) != 0;
	}
	
	/**
	 * @param attributes the attributes to set
	 */
	public void setAttributes(Map<String, Object> attributes) {
		this.attributes = attributes;
	}

	/**
	 * Adds an attribute
	 *
	 * @param key
	 * @param value
	 */
	public void setAttribute(String key, Object value) {
		getAttributes().put(key, value);
	}

	/**
	 * Tests to see if the attribute exists
	 *
	 * @param key
	 * @return true if the attribute is found;false otherwise
	 */
	public boolean hasAttribute(String key) {
		boolean result = false;
		if ( this.attributes != null ) {
			result = this.attributes.containsKey(key);
		}

		return result;
	}

	/**
	 * Gets an attribute
	 *
	 * @param key
	 * @return the attribute value
	 */
	public Object getAttribute(String key) {
		if ( this.attributes == null) return null;
		return getAttributes().get(key);
	}


	/**
	 * @return the children
	 */
	public List<ASTNode> getChildren() {
		if ( this.children == null ) {
			this.children = new ArrayList<ASTNode>();
		}
		return children;
	}

	/**
	 * @param node
	 */
	public void addChild(ASTNode node) {
		getChildren().add(node);
		becomeParentOf(node);
	}

	/**
	 * @param children the children to set
	 */
	public void setChildren(List<ASTNode> children) {
		this.children = children;
		if(this.children != null ) {
			for(ASTNode node : children) {
				becomeParentOf(node);
			}
		}
	}

	/**
	 * Visits the {@link ASTNode}.
	 *
	 * @param v
	 */
	public abstract void visit(ASTNodeVisitor v) throws EvalException;
}

