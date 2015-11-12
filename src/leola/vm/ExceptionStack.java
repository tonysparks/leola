/*
 * see license.txt
 */
package leola.vm;

import java.util.Stack;

/**
 * Keeps track of the program counters for jumping during try/catch/finally blocks.
 * 
 * <p>
 * This keeps an internal {@link Stack} of the INIT_*_BLOCKS which contain a program counter to
 * the jumping END_BLOCK instruction set (which depending on the type will execute a CATCH or FINALLY
 * block statements).
 * 
 * @author Tony
 *
 */
public class ExceptionStack {

    private Stack<Long> blockStack;
    
    /**
     */
    public ExceptionStack() {
        this.blockStack = new Stack<Long>();
    }

    /**
     * @return true if there are currently no try statements pushed
     * on to the stack.
     */
    public boolean isEmpty() {
        return this.blockStack.isEmpty();
    }
    
    /**
     * Push a Try block with a Finally statement on to the stack
     * 
     * @param pc the program counter (instruction index) to jump
     * to
     */
    public void pushFinally(int pc) {
        long instr = pc;
        instr = (instr << 32) | 1;
                
        this.blockStack.add(instr);
    }
    
    
    /**
     * Push a Try block with a Catch statement on to the stack
     * 
     * @param pc the program counter (instruction index) to jump
     * to
     */
    public void pushCatch(int pc) {
        long instr = pc;
        instr = (instr << 32) | 0;
                
        this.blockStack.add(instr);
    }
    
    
    /**
     * @return peek at the top of the stack to see if there is 
     * a Finally block to be executed
     */
    public boolean peekIsFinally() {
        if(!this.blockStack.isEmpty()) {
            long instr = this.blockStack.peek();
            return (instr << 32) > 0;
        }
        
        return false;
    }

    /**
     * @return peek at the top of the stack to see if there is 
     * a Catch block to be executed
     */
    public boolean peekIsCatch() {
        if(!this.blockStack.isEmpty()) {
            long instr = this.blockStack.peek();
            return (instr << 32) == 0;
        }
        
        return false;
    }
    
    
    /**
     * @return the program counter at the top of the stack
     */
    public int peekAddress() {
        long instr = this.blockStack.peek();
        return  (int) (instr >> 32);
    }
    
    
    /**
     * Removes the top of the Stack
     */
    public void pop() {
        this.blockStack.pop();
    }
}
