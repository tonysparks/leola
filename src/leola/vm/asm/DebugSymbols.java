/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.vm.asm;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Stack;


/**
 * {@link DebugSymbols} contains information regarding local variables within a particular scope.
 * 
 * @author chq-tonys
 *
 */
public class DebugSymbols {
	static class LocVar {
		String symbol;
		int startpc, endpc;
		
		LocVar(String symbol, int startpc, int endpc) {		
			this.symbol = symbol;
			this.startpc = startpc;
			this.endpc = endpc;
		}
		
		
	}
	
	private LocVar[] locvars;
	private int index;
	private Stack<Integer> scopes;
	
	private String source;
	private String sourceFile;
	
	/**
	 * 
	 */
	public DebugSymbols() {
		this.scopes = new Stack<Integer>();
		this.locvars = new LocVar[10];
		this.index = 0;
	}
	
	public DebugSymbols(LocVar[] vars) {
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
	
	public int store(String reference, int startpc) {
		if ( index >= this.locvars.length ) {			
			LocVar[] newarray = new LocVar[locvars.length  << 1];
			System.arraycopy(this.locvars, 0, newarray, 0, this.locvars.length);
			this.locvars = newarray;
		}
		
		this.locvars[index] = new LocVar(reference, startpc, -1);
		return this.index++;
	}
	
	public void startScope(int startpc) {
		scopes.push(startpc);
	}
	
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
	 * Retrieves a symbol
	 * @param index
	 * @return
	 */
	public String getSymbol(int index, int pc) {		  
		for (int i = 0; i < this.index && locvars[i].startpc <= pc; i++) {
			if (pc < locvars[i].endpc || locvars[i].endpc == -1 ) {  /* is variable active? */
				index--;
				if (index < 0)
					return locvars[i].symbol;
		    }
		}
		return null;  /* not found */
	}
	
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
		LocVar[] vars = new LocVar[len];
		for(int i = 0; i < len; i++) {
			int strLen = input.readInt();
			byte[] s = new byte[strLen];
			input.readFully(s);
			String symbol = new String(s);
			
			int startpc = input.readInt();
			int endpc = input.readInt();
			vars[i] = new LocVar(symbol, startpc, endpc);			
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
			LocVar var = this.locvars[i];
			output.writeInt(var.symbol.length());
			output.write(var.symbol.getBytes());
			
			output.writeInt(var.startpc);
			output.writeInt(var.endpc);
		}
	}
}
