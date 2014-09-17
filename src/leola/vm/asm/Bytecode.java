/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.vm.asm;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import leola.vm.Opcodes;
import leola.vm.types.LeoClass;
import leola.vm.types.LeoFunction;
import leola.vm.types.LeoNamespace;
import leola.vm.types.LeoObject;


/**
 * Represents a list of instructions.  A {@link Bytecode} is created in Leola by defining either a {@link LeoNamespace}
 * , {@link LeoClass} or a {@link LeoFunction}.
 * 
 * @author Tony
 *
 */
public class Bytecode {	
	public static final int MAGIC_NUMBER = 0x1E01A;
	public static final int VERSION = 1;
	
	public final int[] instr;
	public       int pc;
	public final int len;
	
	public LeoObject[] constants;
	public int numConstants;
		
	public DebugSymbols debugSymbols;
	public byte debug;
	
	public int numLocals;		
	public int numOuters;
	
	public int numArgs;
	public int numInners;
	
	public boolean isVarargs;
		
	public int maxstacksize;
	
	public Bytecode[] inner;	
	
	
	/**
	 * @param instructions
	 */
	public Bytecode(int[] instructions) {
		this(instructions, 0, instructions.length);
	}

	/**
	 * @param instr
	 * @param pc
	 * @param len
	 */
	public Bytecode(int[] instr, int pc, int len) {		
		this.instr = instr;
		this.pc = pc;
		this.len = len;		
	}
	
	/**
	 * @return the index in which to start the variable arguments
	 */
	public int getVarargIndex() {
		return this.numArgs - 1;
	}
	
	/**
	 * Sets the filename in which generated this {@link Bytecode}.  Only
	 * stores this information in DEBUG mode.
	 * 
	 * @param filename
	 */
	public void setSourceFile(String filename) {
		/*
		 * TEMP HACK -- move the compiler!!!
		 */
		
		if(this.debugSymbols!=null) {
			this.debugSymbols.setSourceFile(filename);
			
			for(int i = 0; i < this.numInners; i++) {
				this.inner[i].setSourceFile(filename);
			}
		}				
	}
	
	public String getSourceFile() {
		return (this.debugSymbols!=null) ? this.debugSymbols.getSourceFile() : "";
	}
	
	/**
	 * Clones this {@link Bytecode}
	 */
	public Bytecode clone() {
		Bytecode clone = new Bytecode(instr);
		clone.constants = this.constants;
		clone.debug = this.debug;
		clone.debugSymbols = this.debugSymbols;
		clone.inner = new Bytecode[this.numInners];
		for(int i = 0; i<this.numInners;i++) {
			clone.inner[i] = this.inner[i].clone();
		}
		
		clone.maxstacksize = this.maxstacksize;
		clone.numArgs = this.numArgs;
		clone.isVarargs = this.isVarargs;		
		clone.numConstants = this.numConstants;
		clone.numInners = this.numInners;
		clone.numLocals = this.numLocals;
		clone.numOuters = this.numOuters;
				
		return clone;
	}
	
	/**
	 * Dump the contents of the bytecode
	 * 
	 * @return
	 */
	public String dump() {
		StringBuilder sb = new StringBuilder();
		return dump(sb, 0, this.pc, this.len);
	}
	
	public String dump(StringBuilder sb, int numTabs, int pc, int len) {
		
		for(int t = 0; t < numTabs; t++) sb.append("\t");
		sb.append(".locals ").append(this.numLocals).append("\n");
		
		for(int i = 0; i < this.numConstants; i++) {
			for(int t = 0; t < numTabs; t++) sb.append("\t");
			if(this.constants[i].isString())
				sb.append(".const \"").append(this.constants[i]).append("\"\t;").append(i).append("\n");
			else sb.append(".const ").append(this.constants[i]).append("\t;").append(i).append("\n");
		}
		
		dumpRaw(this, sb, numTabs, this.instr, pc, len);
		/*
		if ( inner != null && inner.length > 0 ) {
			sb.append("\n");
			
				
			for(int i = 0; i < this.numInners; i++) {
				Bytecode bc = this.inner[i];
			
				for(int t = 0; t < numTabs+1; t++) sb.append("\t");		
				sb.append(".scope ").append(i).append("\n");
				
				bc.dump(sb, numTabs + 1, bc.pc, bc.len);
				sb.append("\n");
			}
		}*/
		
		return sb.toString();
	}
	
	public static void dumpRaw(Bytecode bytecode, StringBuilder sb, int numTabs, int[] instr, int pc, int len) {
		List<Integer> visited = new ArrayList<Integer>(bytecode.numInners);
		for(int i = pc; i < len; i++) {
			int code = instr[i];
			
			int iopcode = Opcodes.OPCODE(code);
			String opcode = Opcodes.op2str(iopcode);
			
			for(int t = 0; t < numTabs; t++) sb.append("\t");
			switch(iopcode) {
				case Opcodes.INVOKE: {
					String arg1 = Integer.toString(Opcodes.ARG1(code));
					String arg2 = Integer.toString(Opcodes.ARG2(code));
					if( "0".equals(arg2)) {
						sb.append(String.format("%-18s %-6s \t\t; %-6s ", opcode, arg1, i));
					}
					else {
						sb.append(String.format("%-18s %-6s %-6s \t; %-6s ", opcode, arg1, arg2, i));	
					}
					break;
				}
				case Opcodes.DEF: {
					//String arg1 = Integer.toString(Opcodes.ARGx(code));
					//String arg2 = Integer.toString(Opcodes.ARG2(code));										
					int inner = Opcodes.ARGx(code);
					Bytecode bc = bytecode.inner[inner];
					sb.append(String.format("%-18s %-6s \t; %-6s ", opcode, bc.numArgs, i));
					
					visited.add(inner);
					
					sb.append("\n");
					//for(int t = 0; t < numTabs+1; t++) sb.append("\t");		
					//sb.append(".scope ").append(i).append("\n");
					
					bc.dump(sb, numTabs + 1, bc.pc, bc.len);
					for(int t = 0; t < numTabs; t++) sb.append("\t");
					sb.append(".end\n");					               
					               
					break;
				}
				case Opcodes.CLASS_DEF: {
					
				}
				case Opcodes.TAIL_CALL: {
					String arg1 = Integer.toString(Opcodes.ARG1(code));														
					sb.append(String.format("%-18s %-6s \t\t; %-6s ", opcode, arg1, i));
					break;
				}
				default: {
					String argx = Integer.toString(Opcodes.ARGx(code));														
					sb.append(String.format("%-18s %-6s \t\t; %-6s ", opcode, argx, i));							
				}
			}
			sb.append("\n");
		}	
				
		if ( bytecode.inner != null && bytecode.inner.length > 0 ) {
			sb.append("\n");
			
				
			for(int i = 0; i < bytecode.numInners; i++) {
				if(visited.contains(i)) {
					continue;
				}
				
				Bytecode bc = bytecode.inner[i];
			
				for(int t = 0; t < numTabs; t++) sb.append("\t");		
				sb.append("; scope ").append(i).append("\n");
				
				bc.dump(sb, numTabs + 1, bc.pc, bc.len);
				
				for(int t = 0; t < numTabs; t++) sb.append("\t");		
				sb.append(".end ; scope ").append(i).append("\n");
			}
		}
	}
	
	/**
	 * Writes the {@link Bytecode} out to a stream.
	 * 
	 * @param out
	 * @throws IOException
	 */
	public void write(DataOutput out) throws IOException {
		out.writeInt(MAGIC_NUMBER);	/* magic number */
		out.writeInt(VERSION); /* the version */
		
		switch(VERSION) {
			case 1: {
				writeVersion1(out); /* notice the 2 */
				break;	
			}
			
			default: {
				throw new IOException("Unsupported version: " + VERSION);
			}
		}
	}

	/**
	 * Writes out Version 2
	 * @param out
	 * @throws IOException
	 */
	private void writeVersion1(DataOutput out) throws IOException {				
		out.writeInt(this.len); /* length */
		for(int i = this.pc; i < this.len; i++) {
			out.writeInt(this.instr[i]);
		}
		
		out.writeInt(this.maxstacksize);
				
		if ( constants != null ) {
			out.writeInt(this.numConstants);
			for(int i = 0; i < this.numConstants; i++) {
				constants[i].write(out);			
			}
		}
		else {
			out.writeInt(0);
		}
		
		out.writeInt(this.numArgs);
		out.writeBoolean(this.isVarargs);		
		out.writeInt(this.numOuters);
		out.writeInt(this.numLocals);
		
		/* write out the var names if this is a class */
		out.writeByte(this.debug);
		if(this.debug>0) {
			this.debugSymbols.write(out);
		}
		
		if ( this.inner != null ) {
			out.writeInt(this.inner.length);
			for(int i = 0; i < this.inner.length; i++ ) {
				this.inner[i].write(out);
			}
		}
		else {
			out.writeInt(0);
		}
	}
	
	
	/**
	 * Reads from the {@link DataInput} stream, constructing the appropriate {@link Bytecode}
	 * 
	 * @param in
	 * @return the {@link Bytecode}
	 * @throws IOException
	 */
	public static Bytecode read(LeoObject env, Symbols symbols, DataInput in) throws IOException {
		int magic = in.readInt();
		if ( magic != MAGIC_NUMBER ) {
			throw new IllegalArgumentException
				("The magic number doesn't match 0x" + Integer.toHexString(MAGIC_NUMBER) +" : 0x" + Integer.toHexString(magic));
		}
		
		int version = in.readInt();
		Bytecode code = null;
		switch(version) {
			case 1: {
				code = readVersion1(env, symbols, in);
				break;
			}
			default: {
				throw new IOException("Illegal version: " + version);
			}
		}
		
		return code;
	}
	
	/**
	 * Reads Version 1
	 * @param symbols
	 * @param in
	 * @return
	 * @throws IOException
	 */
	private static Bytecode readVersion1(LeoObject env, Symbols symbols, DataInput in) throws IOException {
		int len = in.readInt();
		int[] instr = new int[len];
		for(int i = 0; i < len; i++) {
			instr[i] = in.readInt();
		}
		
		Bytecode result = new Bytecode(instr);
		result.maxstacksize = in.readInt();		
		
		result.numConstants = in.readInt();		
		result.constants = new LeoObject[result.numConstants];
		for(int i = 0; i < result.numConstants; i++) {
			LeoObject obj = LeoObject.read(env, symbols, in);	
			result.constants[i] = obj;
		}
		
		result.numArgs = in.readInt();		
		result.isVarargs = in.readBoolean();
		result.numOuters = in.readInt();				
		result.numLocals = in.readInt();		
		result.debug = in.readByte();
		if( result.debug > 0 ) {
			result.debugSymbols = DebugSymbols.read(in);
		}		
				
		result.numInners = in.readInt();
		result.inner = new Bytecode[result.numInners];
		for(int i = 0; i < result.numInners; i++ ) {			
			Bytecode code = Bytecode.read(env, symbols, in);			
			result.inner[i] = code;			
		}				
		
		return result;
	}
	
}
	

