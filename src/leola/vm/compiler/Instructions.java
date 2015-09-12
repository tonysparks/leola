/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.vm.compiler;


/**
 * Set of opcode instructions used for building {@link Bytecode}
 * 
 * @author Tony
 *
 */
public class Instructions {

    private int[] instructions;
    private int count;
    
    /**
     */
    public Instructions() {
        this(128);
    }
    
    /**
     * @param size the default size of the underlying int[]
     */
    public Instructions(int size) {
        this(new int[size], 0);
    }
    
    
    /**
     * @param instrs
     * @param count
     */
    public Instructions(int[] instrs, int count) {
        this.instructions = instrs;
        this.count = count;
    }

    /**
     * Ensure that the backing array is big enough for the additional
     * instructions
     * 
     * @param additional number of additional instructions we want to
     * add
     */
    private void ensureCapacity(int additional) {
        if( (count + additional) < 0) {
            throw new IllegalArgumentException("Integer overflow, too big of an array");
        }
        
        /* if we need to expand the array, let's go
         * ahead and do so.
         */
        if( count + additional > instructions.length ) {
            int desiredSize = count + additional;
            int newLength = instructions.length;
            while(desiredSize > newLength) {
                newLength *= 2;
                
                /* if we overflowed, check
                 * and see if the max length 
                 * is appropriate
                 */
                if(newLength < 0) {
                    if(Integer.MAX_VALUE >= desiredSize) {
                        newLength = Integer.MAX_VALUE;
                        break;
                    }
                    else {
                        throw new IllegalArgumentException("Integer overflow, too big of an array");
                    }
                }
            }
            
            int[] tmp = new int[newLength];
            System.arraycopy(instructions, 0, tmp, 0, count);
            
            instructions = tmp;
        }
    }
    
    
    /**
     * Adds the instruction
     * 
     * @param instruction
     */
    public void add(int instruction) {
        ensureCapacity(1);
        
        this.instructions[this.count++] = instruction;
    }
    
    /**
     * Sets the instruction at a specific index
     * 
     * @param index
     * @param instruction
     */
    public void set(int index, int instruction) {
        this.instructions[index] = instruction;
    }
    
    /**
     * Replaces the last instruction with the supplied one
     * @param instruction
     */
    public void setLast(int instruction) {
        this.instructions[this.count-1] = instruction;
    }
    
    /**
     * Check to see if the supplied index is a valid instruction
     * 
     * @param index
     * @return true if valid, false otherwise
     */
    public boolean hasIndex(int index) {
        return index >= 0 && index < this.count;
    }
    
    /**
     * Get the instruction at the supplied index
     * @param index
     * @return the instruction
     */
    public int get(int index) {
        return this.instructions[index];
    }
    
    /**
     * Peeks at the last added instruction
     * @return the last inserted instruction
     */
    public int peekLast() {
        return this.instructions[count-1];
    }
    
    /**
     * Removes an instruction
     * 
     * @param index
     */
    public void remove(int index) {
        if(count > 0) {
            for(int i = index; i < count-1; i++) {
                instructions[i] = instructions[i+1];
            }
            count--;
        }
    }
    
    /**
     * Removes the instructions starting from the startIndex and up to and 
     * including the <code>startIndex + length</code>.
     * 
     * <pre>
     *  // [0,1,2,3,4]
     *   removeRange( 0, 0 ) // result: [1,2,3,4]
     *   
     *   // [0,1,2,3,4]
     *   removeRange( 0, 2 ) // result: [3,4]
     * </pre>
     * 
     * @param startIndex the starting index to start removing from
     * @param length the number of instructions to remove from the starting index
     */
    public void removeRange(int startIndex, int length) {                
        if(count > 0) {
            for(int i = startIndex; i + length < count-1; i++) {
                instructions[i] = instructions[i+length+1];
            }
            count -= length + 1;
        }
    }
    
    /**
     * @return the count
     */
    public int getCount() {
        return count;
    }
    
    /**
     * @return the instructions
     */
    public int[] getInstructions() {
        return instructions;
    }
    
    /**
     * Truncates the underlying int[] to fit the {@link Instructions#getCount()} size.
     * 
     * @return the truncated int[]
     */
    public int[] truncate() {
        if(count != this.instructions.length) {
            int[] result = new int[count];
            System.arraycopy(instructions, 0, result, 0, count);
            
            return result;
        }
        return this.instructions;
    }
}

