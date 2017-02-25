/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.ast;

import java.util.List;

import leola.frontend.EvalException;
import leola.vm.util.Pair;

/**
 * @author Tony
 *
 */
public class MapDeclExpr extends Expr {

    /**
     * Elements
     */
    private List<Pair<Expr, Expr>> elements;



    /**
     * @param elements
     */
    public MapDeclExpr(List<Pair<Expr, Expr>> elements) {
        this.elements = elements;
        for(Pair<Expr, Expr> p : elements) {
            becomeParentOf(p.getFirst());
            becomeParentOf(p.getSecond());
        }
    }



    /* (non-Javadoc)
     * @see leola.ast.ASTNode#visit(leola.ast.ASTNodeVisitor)
     */
    @Override
    public void visit(ASTNodeVisitor v) throws EvalException {
        v.visit(this);
    }

    /**
     * @return the elements
     */
    public List<Pair<Expr, Expr>> getElements() {
        return elements;
    }
}

