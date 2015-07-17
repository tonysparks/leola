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
    
    /**
     * Finds a reference, generating an {@link OuterDesc} if found
     * 
     * @param reference
     * @return the {@link OuterDesc} that describes the {@link Outer}, which
     * includes its local index and the up value (the number of scopes above
     * this current scope).
     */
    public OuterDesc find(String reference) {
        OuterDesc upvalue = null;
        
        int up = 0;
        EmitterScope scope = peek();
        while(scope != null) {
            if(scope.hasLocals()) {
                Locals locals = scope.getLocals();
                int index = locals.get(reference);
                if ( index > -1) {
                    upvalue = new OuterDesc(index, up);             
                    break;
                }
            }
            
            scope = scope.getParent();
            up++;
        }
        
        return upvalue;
    }       
}
