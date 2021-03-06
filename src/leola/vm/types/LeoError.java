/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.vm.types;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Stack;

/**
 * An Error object
 * 
 * @author Tony
 *
 */
public class LeoError extends LeoObject {

    private LeoObject message;
    private int lineNumber;
    private String sourceFile;
    
    private Stack<LeoError> stackTrace;
    
    
    public LeoError(LeoObject message, int lineNumber) {
        super(LeoType.ERROR);
        this.message = message;
        this.lineNumber = lineNumber;
        this.stackTrace = new Stack<LeoError>();
    }
    
    /**
     * @param message
     */
    public LeoError(LeoObject message) {
        this(message, -1);
    }
    
    public LeoError(Exception e) {
        this(e.getMessage());
    }
    
    public LeoError(String msg) {
        this(LeoString.valueOf(msg));
    }
    public LeoError(String msg, int lineNumber) {
        this(LeoString.valueOf(msg), lineNumber);
    }
    public LeoError(int lineNumber) {
        this(LeoString.valueOf(""), lineNumber);
    }
    
    public LeoError() {
        this(LeoNull.LEONULL);
    }
    
    public void addStack(LeoError error) {
        this.stackTrace.add(error);
    }
    
    /**
     * @return the message
     */
    public LeoObject getMessage() {
        return message;
    }
    
    /**
     * @param lineNumber the lineNumber to set
     */
    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }
    
    /**
     * @param sourceFile the sourceFile to set
     */
    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }
    
    /**
     * @return the sourceFile
     */
    public String getSourceFile() {
        return sourceFile;
    }
    
    /**
     * @return the lineNumber
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /* (non-Javadoc)
     * @see leola.vm.types.LeoObject#isError()
     */
    @Override
    public boolean isError() {
        return true;
    }
    
    /* (non-Javadoc)
     * @see leola.vm.types.LeoObject#toString()
     */
    @Override
    public String toString() {
        final String INDENT = "   ";
        StringBuilder sb = new StringBuilder();
        sb.append("Error");
        
        if(this.sourceFile!=null) {
            sb.append(" <").append(this.sourceFile).append("> ");
        }
        
        /*if(lineNumber > 0) sb.append("Error: root cause on line: ").append(lineNumber);
        else sb.append("Error: root cause: ");*/
        
        if(lineNumber > 0) {
            sb.append("@ line: ").append(lineNumber);
        }
        
        sb.append(" >> ").append(this.message);
        sb.append("\n");
        
        int tab = 0;
        int size = stackTrace.size();
        for(int i = 0; i < size; i++) {                        
            LeoError error = stackTrace.get(i);            
            if(error.getLineNumber()>0) {
                if(error.sourceFile!=null) {
                    sb.append("+-Thrown from ")
                            .append("<").append(error.sourceFile).append("> @ line: ")
                            .append(error.lineNumber);
                }
                else {
                    sb.append("+-Thrown from line: ").append(error.lineNumber);
                }
            }
            else {
                sb.append("+-Thrown: ");
            }
            String message = error.message.isNull() ? "" : error.message.toString();
            
            if ( i < size-1 ) {
                sb.append("\n");
                for(int j = 0; j < tab; j++) sb.append(INDENT);
                sb.append("|\n");
                for(int j = 0; j < tab; j++) sb.append(INDENT);            
                sb.append("+--");
            }
            
            if( message!=null && !"".equals(message) ) {
                sb.append(">>>").append(message);        
            }
            tab++;
        }
        return sb.toString();
    }
        
    /* (non-Javadoc)
     * @see leola.vm.types.LeoObject#$eq(leola.vm.types.LeoObject)
     */
    @Override
    public boolean $eq(LeoObject other) {
        if(this == other) {
            return true;
        }
        
        if(other == null) {
            return false;
        }
        
        if(getMessage().equals(other)) {
            return true;
        }
        
        return false;
    }

    /* (non-Javadoc)
     * @see leola.vm.types.LeoObject#$lt(leola.vm.types.LeoObject)
     */
    @Override
    public boolean $lt(LeoObject other) {
        return false;
    }

    /* (non-Javadoc)
     * @see leola.vm.types.LeoObject#$gt(leola.vm.types.LeoObject)
     */
    @Override
    public boolean $gt(LeoObject other) {
        return false;
    }

    /* (non-Javadoc)
     * @see leola.vm.types.LeoObject#getValue()
     */
    @Override
    public Object getValue() {
        return this;
    }
    
    /* (non-Javadoc)
     * @see leola.vm.types.LeoObject#clone()
     */
    @Override
    public LeoObject clone() {
        return new LeoError(this.message.clone(), this.lineNumber);
    }

    /* (non-Javadoc)
     * @see leola.vm.types.LeoObject#write(java.io.DataOutput)
     */
    @Override
    public void write(DataOutput out) throws IOException {
        out.write(this.getType().ordinal());
        this.message.write(out);
        out.writeInt(this.lineNumber);
    }
    

    /**
     * Reads from the {@link DataInput} stream, constructing a {@link LeoObject}
     * 
     * @param in
     * @return the {@link LeoObject}
     * @throws IOException
     */
    public static LeoError read(LeoObject env, DataInput in) throws IOException {
        LeoObject message = LeoObject.read(env, in);
        int lineNumber = in.readInt();
        return new LeoError(message, lineNumber);
    }

}
