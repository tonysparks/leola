/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.vm.compiler;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Stack;


/**
 * {@link DebugSymbols} contains information regarding local variables within a particular scope.
 * 
 * @author Tony
 *
 */
public class DebugSymbols {
    
    /**
     * Local variable
     * 
     * @author Tony
     *
     */
	private static class LocalVar {
	    
	    /**
	     * The reference/name of the variable
	     */
		String symbol;
		
		/**
		 * The starting and ending program counter
		 * that encompasses the scope.  That is, this
		 * local variable only lives within these 
		 * instructions
		 */
		int startpc, endpc;
		
		LocalVar(String symbol, int startpc, int endpc) {		
			this.symbol = symbol;
			this.startpc = startpc;
			this.endpc = endpc;
		}
		
		
	}
	
	/**
	 * All of the local variables for
	 * a {@link Bytecode}
	 */
	private LocalVar[] locvars;
	
	/**
	 * Number of lexical scopes within a
	 * {@link Bytecode}
	 */
	private Stack<Integer> scopes;
	
	/**
	 * The actual number of local
	 * variables
	 */
	private int index;
	
	private String source;
	private String sourceFile;
	
	/**
	 * 
	 */
	public DebugSymbols() {
		this.scopes = new Stack<Integer>();
		this.locvars = new LocalVar[10];
		this.index = 0;
	}
	
	public DebugSymbols(LocalVar[] vars) {
		this.locvars = vars;
		this.index = vars.length;
		this.scopes = new Stack<Integer>();
	}
	
	/**
	 * @return the source
	 */
	public String getSource() {
		return source;
	}
	
	/**
	 * @param source the source to set
	 */
	public void setSource(String source) {
		this.source = source;
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
	 * Stores a symbol starting at an instruction location (program counter).
	 * 
	 * @param reference
	 * @param startpc
	 * @return the index at which the symbol is stored
	 */
	public int store(String reference, int startpc) {
	    /* Expand the array if we have reached our local limit
	     */
		if ( index >= this.locvars.length ) {			
			LocalVar[] newarray = new LocalVar[locvars.length  << 1];
			System.arraycopy(this.locvars, 0, newarray, 0, this.locvars.length);
			this.locvars = newarray;
		}
		
		this.locvars[index] = new LocalVar(reference, startpc, -1);
		return this.index++;
	}
	
	
	/**
	 * Starts a new scope for local variables
	 * 
	 * @param startpc
	 */
	public void startScope(int startpc) {
		scopes.push(startpc);
	}
	
	
	/**
	 * Ends the lexical scope for the local variable
	 * 
	 * @param endpc
	 */
	public void endScope(int endpc) {
		int pc = scopes.pop();
		for (int i = 0; i < locvars.length; i++) {
			if(locvars[i]!=null) {
				if (locvars[i].endpc < 0 && locvars[i].startpc >= pc) {  
					locvars[i].endpc = endpc;
			    }
			}
		}
	}
	
	/**
	 * Retrieves a symbol for a local variable
	 * 
	 * @param index
	 * @param pc the instruction location (program counter)
	 * @return the reference name at the supplied index and program counter
	 */
	public String getSymbol(int index, int pc) {		  
		for (int i = 0; i < this.index && locvars[i].startpc <= pc; i++) {
		    
		    /* is variable active? */
			if (pc < locvars[i].endpc || locvars[i].endpc == -1 ) {  
				index--;
				if (index < 0)
					return locvars[i].symbol;
		    }
		}
		return null;  /* not found */
	}
	
	
	/**
	 * @return the number of debug symbols
	 */
	public int getSize() {
		return this.index;
	}
	
	/**
	 * Reads the from {@link DataInput} stream and constructs a {@link DebugSymbols}.
	 * 
	 * @param input
	 * @return
	 * @throws IOException
	 */
	public static DebugSymbols read(DataInput input) throws IOException {		
		int len = input.readInt();
		LocalVar[] vars = new LocalVar[len];
		for(int i = 0; i < len; i++) {
			int strLen = input.readInt();
			byte[] s = new byte[strLen];
			input.readFully(s);
			String symbol = new String(s);
			
			int startpc = input.readInt();
			int endpc = input.readInt();
			vars[i] = new LocalVar(symbol, startpc, endpc);			
		}
		
		return new DebugSymbols(vars);
	}
	
	/**
	 * Serializes the {@link DebugSymbols}
	 * @param output
	 * @throws IOException
	 */
	public void write(DataOutput output) throws IOException {
		output.writeInt(this.index);
		for(int i = 0; i < this.index; i++) {
			LocalVar var = this.locvars[i];
			output.writeInt(var.symbol.length());
			output.write(var.symbol.getBytes());
			
			output.writeInt(var.startpc);
			output.writeInt(var.endpc);
		}
	}
}
