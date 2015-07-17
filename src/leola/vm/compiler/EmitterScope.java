/*
 * see license.txt
 */
package leola.vm.compiler;

import java.util.Stack;

import leola.vm.exceptions.LeolaRuntimeException;


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
    
    /**
     * Constants pool
     */
    private Constants constants;
    
    /**
     * Local variables
     */
    private Locals locals;
    
    /**
     * Closure 'outer' variables
     */
    private Outers outers;
    
    
    /**
     * Max stack space needed for this scope
     */
    private int maxstacksize;
    
    /**
     * The type of scope this is
     */
    private ScopeType scopeType;
    
    
    /**
     * Parent Scope
     */
    private EmitterScope parent;
    
    
    /**
     * The bytecode instructions
     */
    private Instructions instructions;  
    private Labels labels;
    
    
    /**
     * Lexical scopes of local variables
     */
    private Stack<Integer> lexicalScopes;
    
    /**
     * sizes of try or on block statements
     */
    private Stack<Integer> blockSize;
    
    /**
     * Debug information symbols
     */
    private DebugSymbols debugSymbols;
    
    private boolean usesLocals;
    private boolean debug;    
    private boolean hasParamIndexes;
    private boolean isVarargs;
    private boolean hasBlocks;
    
    private int currentLineNumber;
    private int numArgs;
    
    
    /**
     * @param parent
     * @param scopeType 
     */
    public EmitterScope(EmitterScope parent, ScopeType scopeType) {
        this.parent = parent;
        this.scopeType = scopeType;
        this.maxstacksize = 2; /* always leave room for binary operations */
        
        this.usesLocals = false;
        this.isVarargs = false;
        this.hasBlocks = false;
        this.hasParamIndexes = false;
        
        this.currentLineNumber = -1;
        this.lexicalScopes = new Stack<Integer>();
        this.blockSize = new Stack<Integer>();
        this.debugSymbols = new DebugSymbols();
        
        this.usesLocals = scopeType == ScopeType.LOCAL_SCOPE;
        
        this.instructions = new Instructions();     
        this.labels = new Labels();
    }
    
    /**
     * @return the debugSymbols
     */
    public DebugSymbols getDebugSymbols() {
        return debugSymbols;
    }
    
    /**
     * @return the numArgs
     */
    public int getNumArgs() {
        return numArgs;
    }
    
    /**
     * @param numArgs the numArgs to set
     */
    public void setNumArgs(int numArgs) {
        this.numArgs = numArgs;
    }
    
    
    /**
     * @return true if there are variable arguments passed to
     * this scope
     */
    public boolean hasVarargs() {
        return this.isVarargs;
    }
    
    
    /**
     * Sets if there are variable arguments passed to
     * this scope.
     * 
     * @param hasVarargs
     */
    public void setVarargs(boolean hasVarargs) {
        this.isVarargs = hasVarargs;
    }
    
    
    /**
     * @return true if there are try/on/finally blocks in
     * this scope
     */
    public boolean hasBlocks() {
        return this.hasBlocks;
    }
    
    
    /**
     * Lets the compiler know there are try/on/finally blocks
     * that need to be handled for this scope.
     */
    public void activateBlocks() {
        this.hasBlocks = true;
    }
    
    /**
     * Activate a try or on block, this will capture the 
     * starting instruction pointer.
     * 
     * @see EmitterScope#popBlock()
     * @param instructionPosition
     */
    public void activateBlocks(int instructionPosition) {
        this.hasBlocks = true;
        this.blockSize.add(instructionPosition);
    }
    
    
    /**
     * Removes the try or on block, returning the 
     * starting instruction pointer.
     * 
     * @return the starting instruction pointer of when {@link EmitterScope#activateBlocks(int)}
     */
    public int popBlock() {
        return blockSize.pop();
    }

    
    /**
     * @return true if this scope has named parameters
     */
    public boolean hasParameterIndexes() {
        return this.hasParamIndexes;
    }
    
    /**
     * This scope has named parameters
     */
    public void activateParameterIndexes() {
        this.hasParamIndexes = true;
    }
    
    /**
     * Retrieves the raw instruction set that has been built up.
     * @return the fixed array size (i.e., all element in the array are
     * populated with an instruction) of the instructions.
     */
    public int[] getRawInstructions() {
        return this.instructions.truncate();
    }
    
    /**
     * @return the currentLineNumber
     */
    public int getCurrentLineNumber() {
        return currentLineNumber;
    }
    
    /**
     * @param currentLineNumber the currentLineNumber to set
     */
    public void setCurrentLineNumber(int currentLineNumber) {
        this.currentLineNumber = currentLineNumber;
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
     * @return the debug
     */
    public boolean isDebug() {
        return debug;
    }
    
    /**
     * @param debug the debug to set
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }
    
    /**
     * @return true if the current scope stores variables on the stack
     * or in the current environment
     */
    public boolean usesLocals() {
        return usesLocals || !lexicalScopes.isEmpty();
    }

    
    /**
     * Adds the symbol to the {@link Locals}.
     * 
     * @param reference
     * @return the index it is stored in the locals table
     */
    public int addLocal(String reference) {
        if(isDebug()) {
            debugSymbols.store(reference, getInstructionCount());
        }
        
        Locals locals = getLocals();
        return locals.store(reference);
    }
    
    
    /**
     * Adds an instruction
     * 
     * @param instruction
     */
    public void addInstr(int instruction) {
        instructions.add(instruction);
    }
    
    /**
     * Reconcile the labels, will correctly mark
     * the <code>jump</code> labels with the correct instruction 
     * positions. 
     */
    public void reconcileLabels() {
        getLabels().reconcileLabels(getInstructions());
    }
    
    /**
     * @return the instructions
     */
    public Instructions getInstructions() {
        return instructions;
    }
    
    /**
     * @return the number of instructions
     */
    public int getInstructionCount() {
        return getInstructions().getCount();
    }
    
    /**
     * Mark the beginning of an inner scope
     */
    public void markLexicalScope() {
        int index = getLocals().getIndex();
        lexicalScopes.push(index);
        
        if(isDebug()) {
            debugSymbols.startScope(getInstructionCount());
        }
    }
    
    /**
     * Leave the scope
     */
    public void unmarkLexicalScope() {
        if(lexicalScopes.isEmpty()) {
            throw new LeolaRuntimeException("Illegal lexical scope");
        }
        
        /*
         * This allows us for reusing the stack space
         * for other local variables that will be in
         * of scope by the time they get here
         */
        int index = lexicalScopes.pop();
        int currentIndex = getLocals().getIndex();
        if(currentIndex != index) {         
            getLocals().setIndex(index);                
        }
        
        if(isDebug()) {
            debugSymbols.endScope(getInstructionCount());
        }
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
     * @return the labels
     */
    public Labels getLabels() {
        return labels;
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
