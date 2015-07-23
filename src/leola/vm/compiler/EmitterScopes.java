/*
 * see license.txt
 */
package leola.vm.compiler;

import leola.vm.compiler.EmitterScope.ScopeType;

/**
 * Keeps a stack of {@link EmitterScope}s
 * 
 * @author Tony
 *
 */
public class EmitterScopes {

    private EmitterScope globalScope;
    private EmitterScope currentScope;
    
    /**
     */
    public EmitterScopes() {
        this.globalScope = pushScope(ScopeType.OBJECT_SCOPE);
    }
    
    /**
     * @return the current active {@link EmitterScope}
     */
    public EmitterScope peek() {
        return this.currentScope == null ? this.globalScope : this.currentScope;
    }
    
    /**
     * @return the globalScope
     */
    public EmitterScope getGlobalScope() {
        return globalScope;
    }
        
    /**
     * Pushes a new {@link EmitterScope}
     * @return the new {@link EmitterScope}
     */
    public EmitterScope pushScope(ScopeType scopeType) {       
        EmitterScope result = new EmitterScope(peek(), scopeType);
                                                        
        this.currentScope = result;
        return result;
    }
    
    /**
     * Pops the current {@link EmitterScope}
     * @return the popped {@link EmitterScope}
     */
    public EmitterScope popScope() {
        if ( this.currentScope == null ) {
            this.currentScope = peek(); /* default to global scope */
        }
        
        EmitterScope poppedScope = this.currentScope;
        this.currentScope = poppedScope.getParent();
        return poppedScope;
    }
}
