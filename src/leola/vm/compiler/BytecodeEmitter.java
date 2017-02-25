/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.vm.compiler;

import static leola.vm.Opcodes.ADD;
import static leola.vm.Opcodes.AND;
import static leola.vm.Opcodes.BNOT;
import static leola.vm.Opcodes.BSL;
import static leola.vm.Opcodes.BSR;
import static leola.vm.Opcodes.CLASS_DEF;
import static leola.vm.Opcodes.DIV;
import static leola.vm.Opcodes.DUP;
import static leola.vm.Opcodes.END_BLOCK;
import static leola.vm.Opcodes.EQ;
import static leola.vm.Opcodes.FUNC_DEF;
import static leola.vm.Opcodes.GEN_DEF;
import static leola.vm.Opcodes.GET;
import static leola.vm.Opcodes.GETK;
import static leola.vm.Opcodes.GET_GLOBAL;
import static leola.vm.Opcodes.GET_NAMESPACE;
import static leola.vm.Opcodes.GT;
import static leola.vm.Opcodes.GTE;
import static leola.vm.Opcodes.IDX;
import static leola.vm.Opcodes.IFEQ;
import static leola.vm.Opcodes.INIT_CATCH_BLOCK;
import static leola.vm.Opcodes.INIT_FINALLY_BLOCK;
import static leola.vm.Opcodes.INVOKE;
import static leola.vm.Opcodes.IS_A;
import static leola.vm.Opcodes.JMP;
import static leola.vm.Opcodes.LAND;
import static leola.vm.Opcodes.LINE;
import static leola.vm.Opcodes.LOAD_CONST;
import static leola.vm.Opcodes.LOAD_FALSE;
import static leola.vm.Opcodes.LOAD_LOCAL;
import static leola.vm.Opcodes.LOAD_NAME;
import static leola.vm.Opcodes.LOAD_NULL;
import static leola.vm.Opcodes.LOAD_OUTER;
import static leola.vm.Opcodes.LOAD_TRUE;
import static leola.vm.Opcodes.LOR;
import static leola.vm.Opcodes.LT;
import static leola.vm.Opcodes.LTE;
import static leola.vm.Opcodes.MOD;
import static leola.vm.Opcodes.MUL;
import static leola.vm.Opcodes.NAMESPACE_DEF;
import static leola.vm.Opcodes.NEG;
import static leola.vm.Opcodes.NEQ;
import static leola.vm.Opcodes.NEW_ARRAY;
import static leola.vm.Opcodes.NEW_MAP;
import static leola.vm.Opcodes.NEW_OBJ;
import static leola.vm.Opcodes.NOT;
import static leola.vm.Opcodes.OPCODE;
import static leola.vm.Opcodes.OPPOP;
import static leola.vm.Opcodes.OR;
import static leola.vm.Opcodes.PARAM_END;
import static leola.vm.Opcodes.POP;
import static leola.vm.Opcodes.REQ;
import static leola.vm.Opcodes.RNEQ;
import static leola.vm.Opcodes.RET;
import static leola.vm.Opcodes.ROTL;
import static leola.vm.Opcodes.ROTR;
import static leola.vm.Opcodes.SET;
import static leola.vm.Opcodes.SETK;
import static leola.vm.Opcodes.SET_ARG1;
import static leola.vm.Opcodes.SET_ARG2;
import static leola.vm.Opcodes.SET_ARGsx;
import static leola.vm.Opcodes.SET_ARGx;
import static leola.vm.Opcodes.SET_GLOBAL;
import static leola.vm.Opcodes.SIDX;
import static leola.vm.Opcodes.STORE_LOCAL;
import static leola.vm.Opcodes.STORE_OUTER;
import static leola.vm.Opcodes.SUB;
import static leola.vm.Opcodes.SWAP;
import static leola.vm.Opcodes.SWAPN;
import static leola.vm.Opcodes.TAIL_CALL;
import static leola.vm.Opcodes.THROW;
import static leola.vm.Opcodes.XOR;
import static leola.vm.Opcodes.YIELD;
import static leola.vm.Opcodes.xLOAD_LOCAL;
import static leola.vm.Opcodes.xLOAD_OUTER;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import leola.vm.Opcodes;
import leola.vm.compiler.EmitterScope.ScopeType;
import leola.vm.types.LeoClass;
import leola.vm.types.LeoDouble;
import leola.vm.types.LeoFunction;
import leola.vm.types.LeoGenerator;
import leola.vm.types.LeoInteger;
import leola.vm.types.LeoLong;
import leola.vm.types.LeoNamespace;
import leola.vm.types.LeoObject;
import leola.vm.types.LeoString;
import leola.vm.util.ArrayUtil;

/**
 * Easily emit opcode codes via the assembler methods.  There are also some helper (non-opcode) methods here to ease the
 * development of emitting instructions.
 * 
 * <p>
 * When using the {@link BytecodeEmitter}, in general you will only need one instance to compile code as all the scope
 * handling is abstracted away by the use of {@link BytecodeEmitter#start(ScopeType)} and {@link BytecodeEmitter#end()}
 * methods.
 * 
 * <p>
 * Example usage:
 * <pre>
 *    BytecodeEmitter em = new BytecodeEmitter();
 *    
 *    // start compiling using the global scope
 *    em.start(ScopeType.GLOBAL_SCOPE);
 *    {
 *        em.funcdef(2, false);
 *        {
 *          // all instructions will be for the newly defined function scope
 *          em.loadlocal(0);
 *          em.loadlocal(1);
 *          // ...
 *          
 *        }
 *        em.end(); // pops the function definition scope, returning control to the parent scope (global scope)
 *    } 
 *    em.end(); // pops the global scope
 * </pre>
 * 
 * @author Tony
 *
 */
public class BytecodeEmitter {    
    
    /**
     * Keeps track of the embedded scopes
     */
    private Stack<BytecodeEmitter> innerEmitterStack;
    
    /**
     * Keeps track of all of the scopes that we have 
     * created in this Emitter; so that we can compile bytecode for 
     * them
     */
    private List<BytecodeEmitter> innerEmmitters;
    
    /**
     * The local scope of a {@link BytecodeEmitter}
     */
    private EmitterScope localScope;
    
    /**
     * The global scope stack
     */
    private EmitterScopes scopes;
    
    
    /**
     * If we are in debugging mode
     */
    private boolean isDebugMode;
    
    
    /**
     */
    public BytecodeEmitter() {
        this(new EmitterScopes());
    }
    
    /**
     * @param scopes keeps track of created {@link EmitterScope}'s
     */
    public BytecodeEmitter(EmitterScopes scopes) {
        this.scopes = scopes;

        this.innerEmitterStack = new Stack<BytecodeEmitter>();
        this.innerEmmitters = new ArrayList<BytecodeEmitter>();
    }

    
    /**
     * @return true if the current scope stores variables on the stack
     * or in the current environment
     */
    public boolean usesLocals() {
        return peek().localScope.usesLocals();
    }

    /**
     * Mark the beginning of an inner scope
     */
    public void markLexicalScope() {
        peek().localScope.markLexicalScope();
    }
    
    /**
     * Leave the scope
     */
    public void unmarkLexicalScope() {
        peek().localScope.unmarkLexicalScope();
    }
    
    /**
     * @return the debug
     */
    public boolean isDebug() {
        return this.isDebugMode;
    }
    
    /**
     * @param debug the debug to set
     */
    public void setDebug(boolean debug) {
        this.isDebugMode = debug;
        if(this.localScope != null) {
            this.localScope.setDebug(debug);
        }
    }
    
    private void incrementMaxstackSize(int delta) {
        peek().localScope.incrementMaxstacksize(delta);
    }
    
    private void incrementMaxstackSize() {
        peek().localScope.incrementMaxstacksize(1);
    }
    private void decrementMaxstackSize(int delta) {
        peek().localScope.incrementMaxstacksize(-delta);
    }
    
    private void decrementMaxstackSize() {
        peek().localScope.incrementMaxstacksize(-1);
    }
    
    
    /**
     * @return the max stack size this bytecode chunk will require
     */
    public int getMaxstacksize() {
        return peek().localScope.getMaxstacksize();
    }
    
    /**
     * Set if this {@link BytecodeEmitter} accepts variable arguments
     * as parameters.
     * 
     * @param hasVarargs
     */
    public void setVarargs(boolean hasVarargs) {
        peek().localScope.setVarargs(hasVarargs);
    }
    
    /**
     * Set the number of arguments this {@link BytecodeEmitter} can accept.
     * 
     * @param numberOfArgs
     */
    public void setNumberOfArgs(int numberOfArgs) {
        peek().localScope.setNumArgs(numberOfArgs);
    }

    /**
     * The current active {@link BytecodeEmitter}.  {@link BytecodeEmitter} may contain
     * sub-scopes (aka {@link BytecodeEmitter} within {@link BytecodeEmitter}'s).  This 
     * will return the current active one.  The {@link BytecodeEmitter#start(ScopeType)} will always
     * place <code>this</code> instance on the stack.
     * 
     * <p>
     * Unless stated otherwise, all methods within the {@link BytecodeEmitter} will always take
     * the <b>head</b> of the stack ( via {@link #peek()}).  That is, all operations invoked are operated on
     * the current scoped {@link BytecodeEmitter}.
     * 
     * @return the current active {@link BytecodeEmitter}
     */
    public BytecodeEmitter peek() {
        return this.innerEmitterStack.peek();
    }   
    
    /**
     * Starts an assembler scope.  This is the equivalent of invoking:
     * 
     * <pre>
     *   start(scopeType, 0 false);
     * </pre>
     * 
     * @see BytecodeEmitter#start(ScopeType, int, boolean)
     * @param scopeType
     */
    public void start(ScopeType scopeType) {
        start(scopeType, 0, false);
    }
    
    /**
     * Starts an assembler scope.  Any {@link Bytecode} to be generated must be contained within
     * a scope.  The {@link BytecodeEmitter#start(ScopeType)} and {@link BytecodeEmitter#end()} coordinate the
     * life cycle of the {@link Bytecode} scope.
     * 
     * <p>
     * Scopes are created in {@link LeoNamespace}, {@link LeoClass}, {@link LeoFunction}, and {@link LeoGenerator}.  These
     * scopes are not to be confused with the lexical-scopes ({@link BytecodeEmitter#markLexicalScope()} and {@link BytecodeEmitter#unmarkLexicalScope()});
     * which a lexical-scope only accounts for the life cycle of a local variable as it pertains the the current scope.
     * 
     * <p>
     * These assembler scopes can be embedded within other assembler scopes because Leola allows to define scoped objects within
     * other scoped objects.  As an example:
     * 
     * <pre>
     *   class ClassScope() {
     *     var functionScope = def() {
     *       var innerFunctionScope = def() {
     *       }
     *     }
     *    }
     * </pre>
     * 
     * <p>
     * In the example code above, we have three scopes defined (<code>ClassScope</code>, <code>functionScope</code> and <code>innerFunctionScope</code>), 
     * all contained within one parent scope of the global {@link LeoNamespace} scope.  Each subsequent scope is stored in within its parent scope.
     * 
     * 
     * 
     * @param scopeType the type of scope
     * @param numberOfArguments
     * @param hasVarargs
     */
    public void start(ScopeType scopeType, int numberOfArguments, boolean hasVarargs) {        
        this.localScope = scopeType == ScopeType.GLOBAL_SCOPE ? 
                              this.scopes.getGlobalScope()
                            : this.scopes.pushScope(scopeType); 
                              
        this.localScope.setNumArgs(numberOfArguments);
        this.localScope.setVarargs(hasVarargs);
        this.localScope.setDebug(isDebugMode);
        
        this.innerEmitterStack.push(this);
    }
    

    
    /**
     * Ends a scope, which will pop the current embedded {@link BytecodeEmitter} (if any).
     * 
     * @see BytecodeEmitter#start(ScopeType, int, boolean)
     */
    public BytecodeEmitter end() {    
        
        /* reconcile the labels */
        reconcileLabels();
        
        /* reconcile any Outers */        
        reconcileOuters(peek());
        
        this.scopes.popScope();
        
        BytecodeEmitter asm = this;
        if ( !this.innerEmitterStack.isEmpty() ) {
            asm = this.innerEmitterStack.pop();
        }
        
        return asm;
    }
    
    /**
     * Reconciles the labels
     */
    private void reconcileLabels() {
        peek().localScope.reconcileLabels();        
    }
    
    /**
     * Reconciles the outer variables
     * 
     * @param asm - this scoped Asm
     */
    private void reconcileOuters(BytecodeEmitter asm) {
        Outers outers = asm.getOuters();

        for (int i = 0; i < outers.getNumberOfOuters(); i++) {
            OuterDesc outer = outers.get(i);            
            int outerUpIndex = outer.getUp();
            
            /*
             * If the outer is not in this scope (UP index is != 0),
             * then we must find the parent Scope to reconcile
             * it
             */
            if (outerUpIndex > 0) {
                
                EmitterScope scope = asm.localScope;
                if (scope != null) {

                    /* find the asm from the parent scope */
                    scope = scope.getParent();
                    BytecodeEmitter outerAsm = findAsmByScope(scope);
                    if (outerAsm != null) {
                        int nup = outerUpIndex - 1;

                        /* if the outer is several parent scopes deep, we'll need to store
                         * an outer in this parent scope, so that we can chain this outer
                         * to finally become an xLOAD_LOCAL once it reaches the appropriate
                         * scope
                         */
                        if (nup > 0) {
                            int store = outerAsm.localScope.getOuters().store(new OuterDesc(outer.getIndex(), nup));
                            outerAsm.linstrx(xLOAD_OUTER, store);
                        }
                        else {
                            outerAsm.linstrx(xLOAD_LOCAL, outer.getIndex());
                        }
                    }
                }
            }
        }
    }
    
    /**
     * @return the globals
     */
    public Outers getOuters() {
        return peek().localScope.getOuters();
    }
    
    /**
     * @return the constants
     */
    public Constants getConstants() {
        return peek().localScope.getConstants();
    }
    
    /**
     * @return the locals
     */
    public Locals getLocals() {
        return peek().localScope.getLocals();
    }

    
    /**
     * Reserve space for the number of locals
     * @param numberOfLocals the number of locals to reserve space for
     */
    public void allocateLocals(int numberOfLocals) {
        getLocals().allocate(numberOfLocals);
    }
    

    /**
     * Finds the associated {@link BytecodeEmitter} by Scope
     * @param scope the scope to find the representative {@link BytecodeEmitter}
     * @return the {@link BytecodeEmitter} if found, null if not found
     */
    private BytecodeEmitter findAsmByScope(EmitterScope scope) {
        for(int i = 0; i < this.innerEmitterStack.size(); i++ ) {
            BytecodeEmitter asm = this.innerEmitterStack.get(i);
            
            /* check by reference */
            if ( asm.localScope == scope ) {
                return asm;
            }
        }
        
        return null;
    }
    
    /**
     * Stores the {@link LeoObject} in the constant table.
     * 
     * @param obj
     * @return the index at which the constant is stored.
     */
    public int addConst(LeoObject obj) {
        Constants constants = getConstants();
        return constants.store(obj);
    }

    /**
     * Stores the {@link LeoObject} in the constants table and
     * emits a load instruction for it.
     * 
     * @param obj
     */
    public void addAndloadconst(LeoObject obj) {
        int index = addConst(obj);
        loadconst(index);
    }
    
    /**
     * Stores the constant 'str' into the constants table and
     * emits a load instruction for it.
     * 
     * @param str
     */
    public void addAndloadconst(String str) {
        addAndloadconst(LeoString.valueOf(str));
    }
    
    
    /**
     * Stores the integer in the constants table and
     * emits a load instruction for it.
     * 
     * @param i
     */
    public void addAndloadconst(int i) {
        addAndloadconst(LeoInteger.valueOf(i));
    }
    
    
    /**
     * Stores the double in the constants table and
     * emits a load instruction for it.
     * 
     * @param i
     */
    public void addAndloadconst(double i) {
        addAndloadconst(LeoDouble.valueOf(i));
    }
    
    
    /**
     * Stores the long in the constants table and
     * emits a <code>loadconst</code> instruction for it.
     * 
     * @param i
     */
    public void addAndloadconst(long i) {
        addAndloadconst(LeoLong.valueOf(i));
    }
        
    /**
     * Invokes a load instruction of the variable, either as a <code>loadlocal</code> or a <code>loadouter</code>.
     * 
     * @see Outer
     * @see BytecodeEmitter#loadlocal(int)
     * @see BytecodeEmitter#loadouter(int)
     * @param ref the reference of the variable name to load
     * @return true if loaded to either as a local or an outer; false if it was not found in either the 
     * local storage or any parent scopes (i.e., Outer).
     */
    public boolean load(String ref) {
        boolean success = true;            
        int    i = getLocals().get(ref);
        if ( i > -1) {
            loadlocal(i);
        }        
        else {                    
            
            OuterDesc upvalue = this.scopes.peek().find(ref);
            if ( upvalue == null ) {                
                success = false;
            }
            else {
                
                Outers outers = getOuters();
                
                int store = outers.store(upvalue);
                loadouter(store);
            }    
        }        
                
        return success;
    }
    
        
    /**
     * Adds the symbol to the {@link Locals}.
     * 
     * @param reference
     * @return the index it is stored in the locals table
     */
    public int addLocal(String reference) {
        return peek().localScope.addLocal(reference);
    }

    /**
     * Adds the reference to the {@link Locals} pool and calls 
     * invokes a {@link BytecodeEmitter#storelocal(int)}.
     * 
     * @see BytecodeEmitter#addLocal(String)
     * @see BytecodeEmitter#storelocal(int)
     * @param reference
     */
    public void addAndstorelocal(String reference) {
        int index = addLocal(reference);
        storelocal(index);
    }
    
    /**
     * Emits a store instruction.  Depending on the scope of the supplied reference, the
     * storage instruction will either be a <code>storelocal</code>, <code>storeouter</code> or a <code>setglobal</code>.
     * 
     * <p>
     * <ol>
     *     <li>If the supplied reference is found in the current local storage, a <code>storelocal</code> instruction is emitted.</li>
     *     <li>If not in the local storage, it will check the parent scopes looking for an {@link OuterDesc}.  If an {@link OuterDesc} is found,
     *     a <code>storeouter</code> instruction is emitted.</li>
     *     <li>Finally, if the reference is not found in the parent scopes, a <code>setglobal</code> instruction is emitted.</li>
     * </ol>
     * 
     * @see BytecodeEmitter#storelocal(int)
     * @see BytecodeEmitter#storeouter(int)
     * @see BytecodeEmitter#setglobal(String)
     * @param ref the reference name of the variable
     */
    public void store(String ref) {
        int index = getLocals().get(ref);
        
        /* this is a global */
        if ( index == -1 ) {                        
            OuterDesc upvalue = this.scopes.peek().find(ref);
            if ( upvalue == null ) {
                setglobal(ref);
            }
            else {
                Outers outers = getOuters();
                index = outers.store(upvalue);
                storeouter(index);
            }
        }
        /* Otherwise this is a local */
        else {
            storelocal(index);
        }            
    }
    
    /**
     * @return the number of instructions
     */
    public int getInstructionCount() {
        return getInstructions().getCount();
    }
    
    /**
     * @return the labels
     */
    public Labels getLabels() {
        return peek().localScope.getLabels();
    }
    
    /**
     * @return the instructions
     */
    public Instructions getInstructions() {
        return peek().localScope.getInstructions();
    }

    /**
     * @return the localScope
     */
    public EmitterScope getLocalScope() {
        return peek().localScope;
    }
    
    /**
     * @return the current instruction
     */
    private int peekInstr() {
        Instructions instrs = peek().localScope.getInstructions();
        return instrs.peekLast();
    }
    
    
    
    /**
     * Adds an instruction to this (local) scoped Asm
     * 
     * @param opcode
     * @param argx
     */
    private void linstrx(int opcode, int argx) {
        localScope.addInstr(SET_ARGx(opcode, argx));
    }
    
    /**
     * Outputs an instruction with no arguments
     * 
     * @param instruction
     */
    private void instr(int instruction) {
        peek().localScope.addInstr(instruction);
    }
    
    /**
     * Outputs an instruction with 1 (x) argument
     * 
     * @param opcode
     * @param argx (x size argument -- see {@link Opcodes}).
     */
    private void instrx(int opcode, int argx) {
        instr(SET_ARGx(opcode, argx));
    }
    
    /**
     * Outputs an instruction with 1 (x-signed) argument
     * 
     * @param opcode
     * @param argsx (x signed size argument -- see {@link Opcodes}).
     */
    private void instrsx(int opcode, int argsx) {
        instr(SET_ARGsx(opcode, argsx));
    }
    
    /**
     * Outputs an instruction with 1 argument
     * 
     * @param opcode
     * @param arg1
     */
    private void instr1(int opcode, int arg1) {
        instr(SET_ARG1(opcode, arg1));
    }
    
    /**
     * Outputs an instruction with 2 arguments
     * 
     * @param opcode
     * @param arg1
     * @param arg2
     */
    private void instr2(int opcode, int arg1, int arg2) {
        instr(SET_ARG2(SET_ARG1(opcode, arg1), arg2));
    }
    
    /**
     * Marks a Label, so it can be eventually calculated for a jmp delta
     * 
     * @param opcode
     * @param label
     */
    private void markLabel(int opcode, String label) {        
        instr(opcode); // will eventually be replaced
        getLabels().markLabel(this, label, opcode);                
    }
    
    /**
     * Constructs a new embedded {@link BytecodeEmitter} with the
     * object scope.
     */
    private void newObjectScopeEmitter() {
        newObjectScopeEmitter(0, false);
    }
    
    
    /**
     * Constructs a new embedded {@link BytecodeEmitter} with the
     * object scope.
     * 
     * @param numberOfParameters
     * @param hasVarargs 
     */
    private void newObjectScopeEmitter(int numberOfParameters, boolean hasVarargs) {

        BytecodeEmitter asm = new BytecodeEmitter(this.scopes);
        asm.start(ScopeType.OBJECT_SCOPE, numberOfParameters, hasVarargs);
        asm.setDebug(this.isDebug());

        peek().innerEmmitters.add(asm);
        this.innerEmitterStack.push(asm);
    }

    /**
     * Constructs a new embedded {@link BytecodeEmitter} with the 
     * local scope.
     * 
     * @param numberOfParameters
     * @param hasVarargs
     */
    private void newLocalScopeEmitter(int numberOfParameters, boolean hasVarargs) {

        BytecodeEmitter asm = new BytecodeEmitter(this.scopes);
        asm.start(ScopeType.LOCAL_SCOPE, numberOfParameters, hasVarargs);
        asm.setDebug(this.isDebug());

        peek().innerEmmitters.add(asm);
        this.innerEmitterStack.push(asm);
    }
    
    /**
     * Creates a label with the supplied name
     * 
     * @param name
     */
    public void label(String name) {        
        getLabels().setLabel(this, name);
    }
    
    
    /**
     * Creates a label with a sequenced name.
     * 
     * @return the label name
     */
    public String label() {
        String labelName = nextLabelName();
        label(labelName);
        
        return labelName;
    }
    
    
    /**
     * Generates the next sequenced labeled name
     * 
     * @return the labeled name
     */
    public String nextLabelName() {
        return peek().getLabels().nextLabelName();
    }
    
    /**
     * The current bytecode index of this {@link BytecodeEmitter}.  That is,
     * the index to be used to retrieve this {@link Bytecode} from {@link Bytecode#inner}
     *  
     * @return the index that references this {@link BytecodeEmitter}
     */
    public int getBytecodeIndex() {
        return peek().innerEmmitters.size();
    }
    
    /*================================================================================
     * The Assembler
     *================================================================================*/
     
    
    
    /**
     * Emits the LINE opcode.  Only enabled if the debug flags are set.  This marks
     * the line numbers in the Leola script with the associated byte code.
     * 
     * <p>
     * As an optimization, this will only emit the <code>line</code> instruction if the supplied
     * line number is greater than the last stored line number.  This helps prevent unnecessary
     * <code>line</code> instructions which would further impact performance.
     *  
     * @param line the line number in the Leola script code
     */
    public void line(int line) {
        
        if ( this.isDebug() ) {
            if ( line != peek().localScope.getCurrentLineNumber() && line != 0 
                && (getInstructionCount() >= 0 )) {

                peek().localScope.setCurrentLineNumber(line);
                
                if ( getInstructionCount()==0 || OPCODE(peekInstr()) != LINE  ) {
                    instrx(LINE, line);
                }
                else {
                    // replace the last instruction (which is a LINE)
                    // with the updated line number
                    getInstructions().setLast(SET_ARGx(LINE, line));
                }
            }
        }
    }
    
    public void loadconst(int index) {
        instrx(LOAD_CONST, index);    
        
        incrementMaxstackSize();
    }
    public void loadlocal(int index) {        
        instrx(LOAD_LOCAL, index);
        incrementMaxstackSize();
    }

    public void loadouter(int index) {
        instrx(LOAD_OUTER, index);
        incrementMaxstackSize();
    }
    
    public void loadname(int index) {
        instrx(LOAD_NAME, index);
        incrementMaxstackSize();
    }
    
    public void paramend() {
        instr(PARAM_END);
        incrementMaxstackSize();
        
        peek().localScope.activateParameterIndexes();
    }
    
    
    public void storelocal(int index) {        
        instrx(STORE_LOCAL, index);
        decrementMaxstackSize();
    }
    
    public void storeouter(int index) {
        instrx(STORE_OUTER, index);
        decrementMaxstackSize();
    }
        
    public void loadnull() {
        instr(LOAD_NULL);
        incrementMaxstackSize();
    }
    public void loadtrue() {
        instr(LOAD_TRUE);
        incrementMaxstackSize();
    }
    public void loadfalse() {
        instr(LOAD_FALSE);
        incrementMaxstackSize();
    }
        
    public void pop() {
        instr(POP);
        decrementMaxstackSize();
    }
    
    
    public void oppop() {
        boolean dupOptimization = false;
        
        /* Check to see if there was an unused expression;
         * if there was, that means there is a DUP instruction
         * that we can ignore, along with this OPPOP
         */
        Instructions instructions = getInstructions();
        int numberOfInstrs = instructions.getCount();
        if(numberOfInstrs > 1) {
            
            /* We go back two instructions because the expression
             * will DUP the expression value before it does a STORE
             */
            int instr = instructions.get(numberOfInstrs-2);                        
            if(OPCODE(instr) == DUP) {
                instructions.remove(numberOfInstrs-2);
                dupOptimization = true;
            }
        }
        
        if(!dupOptimization) {
            instr(OPPOP);
        }
        
    }
    public void dup() {
        instr(DUP);
        incrementMaxstackSize();
    }
            
    public void yield() {
        instr(YIELD);
    }
    
    public void ret() {
        instr(RET);
    }
        
    public void rotl(int n) {
        if ( n != 0 ) {
            instrx(ROTL, n);
        }
    }
    
    public void rotr(int index) {
        instrx(ROTR, index);
    }
    
    public void swap() {
        instr(SWAP);
    }
    
    public void swapn(int amount) {
        if ( amount > 0 ) {
            instrx(SWAPN, amount);
        }
    }
    
    public void tailcall(int numberOfParameters, int expandArrayIndex) {
        instr2(TAIL_CALL, numberOfParameters, expandArrayIndex);
    }
    
    public void jmp(String label) {
        markLabel(JMP, label);
    }
    public String jmp() {
        String label = nextLabelName();
        jmp(label);
        return label;
    }
    
    public void jmp(int offset) {
        if(offset != 0) {
            instrsx(JMP, offset);
        }
    }
    
    public void brk(String label) {
        jmp(label);
    }
    
    public void cont(String label) {
        jmp(label);
    }
    
    public void newobj(int nargs, int expandedArray) {
        instr2(NEW_OBJ, nargs, expandedArray);
        incrementMaxstackSize(nargs);
    }
    
    public void newarray(int initialSize) {
        instrx(NEW_ARRAY, initialSize);
        incrementMaxstackSize(initialSize);
    }
    public void newmap(int initialSize) {
        instrx(NEW_MAP, initialSize);
        incrementMaxstackSize(initialSize);
    }
    
    public void idx() {
        instr(IDX);
        decrementMaxstackSize();
    }
    public void sidx() {
        instr(SIDX);
        decrementMaxstackSize();
    }
    
    public void get() {
        instr(GET);
        decrementMaxstackSize();
    }
    public void set() {
        instr(SET);
        decrementMaxstackSize(2);
    }
    public void getk(int constIndex) {
        instrx(GETK, constIndex);
        decrementMaxstackSize();
    }
    public void getk(String stringconst) {
        int index = getConstants().store(stringconst);
        instrx(GETK, index);
        decrementMaxstackSize();
    }
    
    public void setk(int constIndex) {
        instrx(SETK, constIndex);
        decrementMaxstackSize();
    }
    public void setk(String stringconst) {
        int index = getConstants().store(stringconst);
        instrx(SETK, index);
        decrementMaxstackSize();
    }
    
    public void getnamespace(String stringconst) {
        int index = getConstants().store(stringconst);
        getnamespace(index);
    }
    public void getnamespace(int constindex) {
        instrx(GET_NAMESPACE, constindex);
        incrementMaxstackSize();
    }
    
    public void getglobal(String stringconst) {
        int index = getConstants().store(stringconst);
        getglobal(index);
    }
    
    public void setglobal(String stringconst) {
        int index = getConstants().store(stringconst);
        setglobal(index);
    }
    
    public void getglobal(int constindex) {
        instrx(GET_GLOBAL, constindex);
        incrementMaxstackSize();
    }
    
    public void setglobal(int constindex) {
        instrx(SET_GLOBAL, constindex);
        decrementMaxstackSize();
    }    
    
    public void namespacedef() {
        instrx(NAMESPACE_DEF, getBytecodeIndex());
        incrementMaxstackSize();
        
        newObjectScopeEmitter();
    }
    
    public void classdef(int numberOfInterfaces, int numberOfParameters, boolean isVarargs) {
        instrx(CLASS_DEF, numberOfInterfaces);
        incrementMaxstackSize(numberOfInterfaces);        
        
        newObjectScopeEmitter(numberOfParameters, isVarargs);
    }
    
    public void gendef(int numberOfParameters, boolean isVarargs) {
        instrx(GEN_DEF, getBytecodeIndex());        
        incrementMaxstackSize(numberOfParameters);
        
        newLocalScopeEmitter(numberOfParameters, isVarargs);
    }
    
    public void funcdef(int numberOfParameters, boolean isVarargs) {
        instrx(FUNC_DEF, getBytecodeIndex());        
        incrementMaxstackSize(numberOfParameters);
        
        newLocalScopeEmitter(numberOfParameters, isVarargs);
    }
        
    public void isa() {
        instr(IS_A);
        decrementMaxstackSize();
    }
    
    public void ifeq(String label) {
        markLabel(IFEQ, label);
        decrementMaxstackSize();
    }
    public void ifeq(int offset) {
        instrsx(IFEQ, offset);
        decrementMaxstackSize();
    }
    
    public String ifeq() {
        String labelName = nextLabelName();
        ifeq(labelName);
        
        return labelName;
    }
    
    public void invoke(int numberOfArgs, int argIndex) {
        //instr1(INVOKE, numberOfArgs);
        instr2(INVOKE, numberOfArgs, argIndex);   
        decrementMaxstackSize(numberOfArgs);
    }
    
    public void throw_() {
        instr(THROW);
        incrementMaxstackSize();
    }
    
    public void initfinally() {
        peek().localScope.activateBlocks(getInstructionCount());
        
        // this will be populated with the correct offset
        instrsx(INIT_FINALLY_BLOCK, 0); 
        
    }
    public void initfinally(int offset) {
        peek().localScope.activateBlocks();
        instrsx(INIT_FINALLY_BLOCK, offset);
    }
    public void initfinally(String label) {
        peek().localScope.activateBlocks();        
        markLabel(INIT_FINALLY_BLOCK, label);
    }
    
    public void initcatch() {
        peek().localScope.activateBlocks(getInstructionCount());
        // this will be populated with the correct offset
        instrsx(INIT_CATCH_BLOCK, 0);         
    }
    
    public void initcatch(int offset) {
        peek().localScope.activateBlocks();
        instrsx(INIT_CATCH_BLOCK, offset); 
    }
    public void initcatch(String label) {
        peek().localScope.activateBlocks();
        markLabel(INIT_CATCH_BLOCK, label);
    }
    
    public void endfinally() {                
        instr1(END_BLOCK, 2);
    }
    public void taginitfinally() {
        int startPC = peek().localScope.popBlock();
        getInstructions().set(startPC, SET_ARGsx(INIT_FINALLY_BLOCK, getInstructionCount()));
        instr1(END_BLOCK, 0);
    }
    
    public void taginitcatch() {
        int startPC = peek().localScope.popBlock();
        getInstructions().set(startPC, SET_ARGsx(INIT_CATCH_BLOCK, getInstructionCount()));
        instr1(END_BLOCK, 1);
    }
    
    public void endcatch() {            
        instr1(END_BLOCK, 0);
    }
    
    public void endblock() {
        instr1(END_BLOCK, 0);
    }
    
    
        
    /* arithmetic operators */
    public void add() {
        instr(ADD);
        decrementMaxstackSize();
    }
    public void sub() {
        instr(SUB);
        decrementMaxstackSize();
    }
    public void mul() {
        instr(MUL);
        decrementMaxstackSize();
    }
    public void div() {
        instr(DIV);
        decrementMaxstackSize();
    }
    public void mod() {
        instr(MOD);
        decrementMaxstackSize();
    }
    public void neg() {
        instr(NEG);
    }
    
//    BSL = 80,
    public void bsl() {
        instr(BSL);
        decrementMaxstackSize();
    }
//    BSR = 81,
    public void bsr() {
        instr(BSR);
        decrementMaxstackSize();
    }
//    BNOT = 82,
    public void bnot() {
        instr(BNOT);
        decrementMaxstackSize();
    }
//    XOR = 83,
    public void xor() {
        instr(XOR);
        decrementMaxstackSize();
    }
//    LOR = 84,
    public void lor() {
        instr(LOR);
        decrementMaxstackSize();
    }
//    LAND = 85,
    public void land() {
        instr(LAND);
        decrementMaxstackSize();
    }
    
    public void or() {
        instr(OR);
        decrementMaxstackSize();
    }
    public void and() {
        instr(AND);
        decrementMaxstackSize();
    }
    public void not() {
        instr(NOT);
    }
    
    public void req() {
        instr(REQ);
        decrementMaxstackSize();
    }
    
    public void rneq() {
        instr(RNEQ);
        decrementMaxstackSize();
    }
    
    public void eq() {
        instr(EQ);
        decrementMaxstackSize();
    }
    public void neq() {
        instr(NEQ);
        decrementMaxstackSize();
    }
    public void gt() {
        instr(GT);
        decrementMaxstackSize();
    }
    public void gte() {
        instr(GTE);
        decrementMaxstackSize();
    }
    public void lt() {
        instr(LT);
        decrementMaxstackSize();
    }
    public void lte() {
        instr(LTE);
        decrementMaxstackSize();
    }

    
    /**
     * Compiles the assembler into {@link Bytecode}.
     * 
     * @return the {@link Bytecode} object
     */
    public Bytecode compile() {

        int [] code = localScope.getRawInstructions();
        Bytecode bytecode = new Bytecode(code);
                    
        bytecode.numArgs = localScope.getNumArgs();
        if(localScope.hasVarargs()) {
            bytecode.setVarargs();
        }
        
        if(localScope.hasBlocks()) {
            bytecode.setBlocks();
        }        
        
        if(localScope.hasParameterIndexes()) {
            bytecode.setParamIndexes();
        }
        
        if(this.localScope.hasOuters()) {
            Outers outers = this.localScope.getOuters();
            bytecode.numOuters = outers.getNumberOfOuters();
        }
        
        
        bytecode.paramNames = new LeoString[bytecode.numArgs];
        if(this.localScope.hasLocals()) {
            Locals locals = this.localScope.getLocals();
            bytecode.numLocals = locals.getNumberOfLocals();
            
            if(bytecode.numArgs > 0) {
                for(int i = 0; i < bytecode.numArgs; i++) {
                    String ref = locals.getReference(i);
                    if(ref != null) {
                        bytecode.paramNames[i] = LeoString.valueOf(ref);
                    }
                }
            }
        }
        
        /* we only care about this for classes */
        if ( isDebug() ) {
            bytecode.setDebug();
            bytecode.debugSymbols = localScope.getDebugSymbols();
        }
                                        
        if ( this.localScope.hasConstants() ) {
            Constants constants = this.localScope.getConstants();
            bytecode.constants = constants.compile();
            bytecode.numConstants = constants.getNumberOfConstants();
        }
        else {
            bytecode.numConstants = 0;
            bytecode.constants = ArrayUtil.EMPTY_LEOOBJECTS;
        }
        
        int stacksize = this.localScope.getMaxstacksize();
        stacksize += bytecode.numArgs;
        stacksize += bytecode.numLocals; 
        stacksize += bytecode.numConstants;                
        stacksize += bytecode.numOuters;        
                
        bytecode.maxstacksize = stacksize;        
        
        
        bytecode.numInners = this.innerEmmitters.size();
        bytecode.inner = new Bytecode[bytecode.numInners];
        for(int i = 0; i < bytecode.inner.length; i++) {
            bytecode.inner[i] = this.innerEmmitters.get(i).compile();
        }
                
        return bytecode;        
    }
}

