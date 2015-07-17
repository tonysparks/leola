/*
 * see license.txt
 */
package leola.vm.compiler;


/**
 * Used to keep track of the current scope while compiling/emitting bytecode.
 * 
 * @author Tony
 *
 */
public class EmitterScope {

    /**
     * Scope type
     * @author Tony
     *
     */
    public static enum ScopeType {
        LOCAL_SCOPE,
        OBJECT_SCOPE,
        GLOBAL_SCOPE
        ;
    }
    
    private Constants constants;
    private Locals locals;
    private Outers outers;
    
    private int maxstacksize;
    
    private ScopeType scopeType;
    
    private EmitterScope parent;
    
    /**
     * @param scopeType 
     */
    public EmitterScope(EmitterScope parent, ScopeType scopeType) {
        this.parent = parent;
        this.scopeType = scopeType;
        this.maxstacksize = 2; /* always leave room for binary operations */
    }
    
    /**
     * Determines if this {@link EmitterScope} has a parent
     * @return true if there is a parent {@link EmitterScope}
     */
    public boolean hasParent() {
        return this.parent != null;
    }
    
    /**
     * @return the parent
     */
    public EmitterScope getParent() {
        return parent;
    }
    
    /**
     * @return the scopeType
     */
    public ScopeType getScopeType() {
        return scopeType;
    }
    
    /**
     * @return the maxstacksize
     */
    public int getMaxstacksize() {
        return maxstacksize;
    }

    /**
     * Increments the allocated stack size by delta.
     * @param delta
     */
    public void incrementMaxstacksize(int delta) {
        this.maxstacksize += delta;
    }
    
    /**
     * @return the constants
     */
    public Constants getConstants() {
        if ( constants == null ) {
            constants = new Constants();
        }
        return constants;
    }

    /**
     * @return true if there are constants in this scope
     */
    public boolean hasConstants() {
        return constants != null && constants.getNumberOfConstants() > 0;
    }

    /**
     * @return the globals
     */
    public Outers getOuters() {
        if ( outers == null ) {
            outers = new Outers();
        }
        return outers;
    }

    /**
     * @return true if there are outers in this scope
     */
    public boolean hasOuters() {
        return outers != null && outers.getNumberOfOuters() > 0;
    }

    /**
     * @return the locals
     */
    public Locals getLocals() {
        if ( locals == null ) {
            locals = new Locals();
        }
        return locals;
    }

    /**
     * @return true if there are locals for this scope
     */
    public boolean hasLocals() {
        return locals != null && locals.getNumberOfLocals() > 0;
    }


}
