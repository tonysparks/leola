/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.ast;

import java.util.List;

import leola.vm.EvalException;

/**
 * Array declaration expression.
 * 
 * <pre>
 *   var array = [ 1, 2, 3 ]
 * </pre>
 * 
 * @author Tony
 *
 */
public class ArrayDeclExpr extends Expr {

    /**
     * Elements
     */
    private List<Expr> elements;



    /**
     * @param elements
     */
    public ArrayDeclExpr(List<Expr> elements) {
        this.elements = elements;
        if(elements!=null) {
            for(int i = 0; i < this.elements.size(); i++) {
                becomeParentOf( this.elements.get(i) );
            }
        }
    }

    @Override
    public void visit(ASTNodeVisitor v) throws EvalException {
        v.visit(this);
    }

    /**
     * @return the elements
     */
    public List<Expr> getElements() {
        return elements;
    }
}

