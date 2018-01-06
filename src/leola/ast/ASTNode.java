/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.ast;

import leola.vm.EvalException;

/**
 * Abstract Syntax tree node
 *
 * @author Tony
 *
 */
public abstract class ASTNode {

    
    public static final int MEMBER_PROPERTY = (1<<1);
    
    /**
     * The line number
     */
    private int lineNumber;
    
    /**
     * Flags
     */
    private int flags;
    
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
        return "[ " + this.source + " ]" + " @ line: " + this.lineNumber;
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
     * Visits the {@link ASTNode}.
     *
     * @param v
     */
    public abstract void visit(ASTNodeVisitor v) throws EvalException;
}

