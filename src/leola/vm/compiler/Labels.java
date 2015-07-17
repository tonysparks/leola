/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.vm.compiler;

import static leola.vm.Opcodes.SET_ARGsx;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A means for managing {@link Label} generation.  In general, {@link Label}s are created
 * and stored in byte code before the final jump address is known.  Upon knowledge of their
 * jump address, they must be updated with the jump address information.
 * 
 * @author Tony
 *
 */
public class Labels {

    private Map<String, Label> labels;
    private int labelIndex;
    
    /**
     */
    public Labels() {
        this.labels = new HashMap<String, Label>();
        this.labelIndex = 0;
    }

    /**
     * @param labelName
     * @return true if the label exists; false otherwise
     */
    public boolean hasLabel(String labelName) {
        return this.labels.containsKey(labelName);
    }
    
    
    /**
     * Stores the label
     * 
     * @param labelName
     * @param label
     */
    public void storeLabel(String labelName, Label label) {
        this.labels.put(labelName, label);
    }
    
    
    /**
     * @param labelName
     * @return the Label if it exists, null otherwise
     */
    public Label getLabel(String labelName) {
        return this.labels.get(labelName);
    }
    
    /**
     * @return a collection of all the stored {@link Label}s
     */
    public Collection<Label> labels() {
        return this.labels.values();
    }
    
    /**
     * If the label exists, it will invoke the {@link Label#mark(int)} on the existing {@link Label}.
     * If the label does not exist, it will create it and associate it with the supplied {@link BytecodeEmitter} and
     * invoke the {@link Label#mark(int)} on it.
     * 
     * @param asm
     * @param labelName
     * @param opcode
     */
    public void markLabel(BytecodeEmitter asm, String labelName, int opcode) {
        if(!hasLabel(labelName)) {
            storeLabel(labelName, new Label(asm));
        }
        
        getLabel(labelName).mark(opcode);
    }
    
    
    /**
     * If the label exists, it will invoke the {@link Label#set()} on the existing {@link Label}.
     * If the label does not exist, it will create it and associate it with the supplied {@link BytecodeEmitter} and
     * invoke the {@link Label#set()} on it.
     * 
     * @param asm
     * @param labelName
     */
    public void setLabel(BytecodeEmitter asm, String labelName) {
        if(!hasLabel(labelName)) {
            storeLabel(labelName, new Label(asm));
        }
        
        getLabel(labelName).set();
    }
    
    /**
     * Generates the next sequenced labeled name
     * 
     * @return the labeled name
     */
    public String nextLabelName() {
        String labelName = ":" + this.labelIndex++;
        return labelName;
    }
    
    
    /**
     * Reconciles the labels
     * 
     * @param instructions the list of instructions that contain the jump labels
     */
    public void reconcileLabels(Instructions instructions) {
        for(Label label : labels()) {            
            for(long l : label.getDeltas()) {
                int instrIndex = (int)(l >> 32);
                int opcode = (int)((l << 32) >> 32);
                int delta = label.getLabelInstructionIndex() - instrIndex - 1;
                int instr = SET_ARGsx(opcode,  delta);   
                
                instructions.set(instrIndex, instr);
            }
        }
    }
}
