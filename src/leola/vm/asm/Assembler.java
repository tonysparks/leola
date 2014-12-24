/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.vm.asm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

import leola.vm.Leola;
import leola.vm.asm.Scope.ScopeType;
import leola.vm.exceptions.LeolaRuntimeException;
import leola.vm.types.LeoDouble;
import leola.vm.types.LeoInteger;
import leola.vm.types.LeoLong;
import leola.vm.types.LeoObject;
import leola.vm.types.LeoString;

/**
 * Leola Bytecode Assembler, basically just a reader for the assemble which is directly translated to the {@link Asm} methods.  
 * 
 * @author chq-tonys
 *
 */
public class Assembler {

	private static final String USAGE = "<usage> leolaasm [-r] <assember file> \n" +
	"\t-r -- Runs the file \n";
	
	/* Ensures static initialization of
	   string/object relationship */
	@SuppressWarnings("unused")
	private static final LeoObject IGNORE = new LeoLong(0);	
	
	/**
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static final void main(String [] args) throws Exception {
		if(args.length < 1) {
			System.out.println(USAGE);
			return;
		}
		
		String filename = "";
		boolean run = false;
		
		if(args.length > 1) {
			filename = args[1];
			run = true;
		}
		else {
			filename = args[0];
		}
		
		
		Assembler asm = new Assembler();		
		Asm a = asm.parseFile(filename);
		Bytecode code = a.compile();
		
		asm.writeOutput(code, filename + ".lbc");
		
		if(run) {
			Leola runtime = new Leola();			
			try {
				LeoObject result = runtime.execute(code);
				if(result.isError()) {
					System.err.println(result);
				}
			}
			catch(LeolaRuntimeException e) {
				System.err.println(e.getLeoError());
			}
		}
	}

	/**
	 */
	public Assembler() {
		init();
	}
	
	/**
	 * Writes out the output file.
	 * 
	 * @param asm
	 * @param outputfile
	 * @throws Exception
	 */
	private void writeOutput(Bytecode bytecode, String outputfile) throws Exception {
		RandomAccessFile outputFile = new RandomAccessFile(new File(outputfile), "rw");
		try {
			bytecode.write(outputFile);
		}
		finally {
			outputFile.close();
		}
	}
	
	
	/**
	 * Parses the assembler file
	 * @param filename
	 * @param outputfile
	 * @return
	 * @throws Exception
	 */
	public Asm parseFile(String filename) throws Exception {
		return parseInput(new BufferedReader(new FileReader(new File(filename))));
	}
	
	/**
	 * Parses the input assembly.
	 * 
	 * @param reader
	 * @return the {@link Asm}
	 * @throws Exception
	 */
	public Asm parseInput(BufferedReader reader) throws Exception {		
		String line = null;
		
		Asm asm = new Asm(new Symbols());
		asm.start(ScopeType.GLOBAL_SCOPE);
		try {
			do {
				line = reader.readLine();
				if(line != null) {
					parseLine(asm, line);
				}
					
			}
			while(line != null);
		}
		finally {
			reader.close();
		}
			
		asm.end();
		return asm;
	}
	
	/**
	 * Convenience method of compiling the {@link Asm} from {@link Assembler#parseInput(BufferedReader)}
	 * 
	 * @param reader
	 * @return the compiled {@link Bytecode}
	 * @throws Exception
	 */
	public Bytecode compile(BufferedReader reader) throws Exception {
		Asm asm = parseInput(reader);
		return asm.compile();
	}

	/**
	 * Parses a line
	 * 
	 * @param asm
	 * @param line
	 * @throws Exception
	 */
	private final Asm parseLine(Asm asm, String line) throws Exception {		
		line = parseOutComment(line);
		
		if(line.length() > 0) {
		
			int opcodeIndex = line.indexOf(' ');
			
			if(opcodeIndex > -1) {
				String opcode = line.substring(0, opcodeIndex);
				Opcode o = opcodes.get(opcode.toUpperCase());
				if(o==null) {
					throw new LeolaRuntimeException("Unknown opcode: " + opcode);
				}
				
				String[] args = line.contains("\"") ? new String[] {line.substring(opcodeIndex)} 
									: line.substring(opcodeIndex).split(",");
				for(int i = 0; i < args.length; i++) args[i] = args[i].trim();
				
				o.invoke(asm, args);
			}
			/* label */
			else if (line.startsWith(":")) {
				asm.label(line.substring(1));
			}
			else {
				Opcode o = opcodes.get(line.toUpperCase());
				if(o==null) {
					throw new LeolaRuntimeException("Unknown opcode: " + line);
				}
				
				o.invoke(asm);
			}
		}
		
		return asm.peek();
	}
	
	private String parseOutComment(String line) {
		line = line.trim();
		
		int comment = line.indexOf(';');
		if(comment > -1) {
			/* Constants may have an embedded ; */
			if(line.toLowerCase().startsWith(".const")) {
				int len = line.length();
				boolean inComment = false;
				
				for(int i = ".cons".length(); i < len; i++) {
					char c = line.charAt(i);
					
					switch(c) {
						case '"': inComment = !inComment; break;
						case ';': if(!inComment) {
							line = line.substring(0, i);
							return line.trim();
						}
						default: {}
					}
				}
			}
			else {
				line = line.substring(0, comment);
				return line.trim();
			}
		}
		
		return line;
	}
	
	interface Opcode {
		void invoke(Asm asm, String ... args);
	}
	
	private String mergeString(String[] args) {		 
		String arg1 = args[0].trim();		
		if(!arg1.endsWith("\"")) {
			throw new LeolaRuntimeException("Invalid input: " + arg1);
		}
		
		String concat = arg1;
		for(int i = 1; i < args.length; i++) {
			concat += " " + args[i];
		}					
		
		String var = concat.substring(1); // remove first "
		var = var.substring(0, concat.lastIndexOf('"')-1); // remove last "						
		return (var);
	}
	
	private final Map<String, Opcode> opcodes = new HashMap<String, Assembler.Opcode>();	
	private void init() {
		
		/* Pseudo opcodes */
		opcodes.put(".CONST", new Opcode() {			
			public void invoke(Asm asm, String...  args) {
				String arg1 = args[0].trim();
				if( arg1.startsWith("\"") ) {
					String var = mergeString(args);										
					asm.getConstants().store(LeoString.valueOf(var));
				}
				else {
					if(arg1.startsWith("0x")) {
						if(arg1.length() > 10) {
							asm.getConstants().store(new LeoLong(Long.parseLong(arg1)));
						}
						else {
							asm.getConstants().store(LeoInteger.valueOf(Integer.parseInt(arg1)));
						}
					}
					else if ( arg1.contains(".")) {
						asm.getConstants().store(LeoDouble.valueOf(Double.parseDouble(arg1)));
					}
					else if ( arg1.contains("L") ) {
						asm.getConstants().store(new LeoLong(Long.parseLong(arg1)));
					}	
					else {
						asm.getConstants().store(LeoInteger.valueOf(Integer.parseInt(arg1)));
					}
				}
			}
		});
		
		opcodes.put(".BEGIN", new Opcode() {			
			public void invoke(Asm asm, String...  args) {
				asm.start(ScopeType.OBJECT_SCOPE);
			}
		});
		
		opcodes.put(".END", new Opcode() {			
			public void invoke(Asm asm, String...  args) {
				asm.end();
			}
		});
		
		opcodes.put(".LOCALS", new Opcode() {			
			public void invoke(Asm asm, String...  args) {
				asm.allocateLocals(Integer.parseInt(args[0]));
			}
		});
		
		/* Store operations */
		opcodes.put("LOAD_CONST", new Opcode() {			
			public void invoke(Asm asm, String...  args) {
				asm.loadconst(Integer.parseInt(args[0]));
			}
		});
		opcodes.put("LOAD_LOCAL", new Opcode() {
			public void invoke(Asm asm, String...  args) {
				asm.loadlocal(Integer.parseInt(args[0]));
			}
		});
		opcodes.put("LOAD_OUTER", new Opcode() {			
			public void invoke(Asm asm, String...  args) {
				asm.loadouter(Integer.parseInt(args[0]));
			}
		});
		opcodes.put("LOAD_NAME", new Opcode() {            
            public void invoke(Asm asm, String...  args) {
                asm.loadname(Integer.parseInt(args[0]));
            }
        });
		opcodes.put("PARAM_END", new Opcode() {            
            public void invoke(Asm asm, String...  args) {
                asm.paramend();
            }
        });
		opcodes.put("xLOAD_OUTER", new Opcode() {			
			public void invoke(Asm asm, String...  args) {
				//asm.def(numberOfParameters)
			}
		});
		opcodes.put("xLOAD_LOCAL", new Opcode() {			
			public void invoke(Asm asm, String...  args) {
			}
		});
		opcodes.put("xLOAD_SCOPE", new Opcode() {			
			public void invoke(Asm asm, String...  args) {
			}
		});		
		opcodes.put("LOAD_NULL", new Opcode() {			
			public void invoke(Asm asm, String...  args) {
				asm.loadnull();
			}
		});
		opcodes.put("LOAD_TRUE", new Opcode() {			
			public void invoke(Asm asm, String...  args) {
				asm.loadtrue();
			}
		});
		opcodes.put("LOAD_FALSE", new Opcode() {			
			public void invoke(Asm asm, String...  args) {
				asm.loadfalse();
			}
		});
		opcodes.put("STORE_LOCAL", new Opcode() {			
			public void invoke(Asm asm, String...  args) {
				asm.storelocal(Integer.parseInt(args[0]));
			}
		});
		opcodes.put("STORE_OUTER", new Opcode() {			
			public void invoke(Asm asm, String...  args) {
				asm.storeouter(Integer.parseInt(args[0]));
			}
		});
		
		/* stack operators */
		opcodes.put("SHIFT", new Opcode() {			
			public void invoke(Asm asm, String...  args) {
				asm.shift(Integer.parseInt(args[0]));
			}
		});
		opcodes.put("POP", new Opcode() {			
			public void invoke(Asm asm, String...  args) {
				asm.pop();
			}
		});
		opcodes.put("OPPOP", new Opcode() {			
			public void invoke(Asm asm, String...  args) {
				asm.oppop();
			}
		});
		opcodes.put("DUP", new Opcode() {			
			public void invoke(Asm asm, String...  args) {
				asm.dup();
			}
		});
		opcodes.put("RET", new Opcode() {			
			public void invoke(Asm asm, String...  args) {
				asm.ret();
			}
		});
		opcodes.put("MOV", new Opcode() {			
			public void invoke(Asm asm, String...  args) {
				asm.mov();
			}
		});
		opcodes.put("MOVN", new Opcode() {			
			public void invoke(Asm asm, String...  args) {
				asm.movn(Integer.parseInt(args[0]));
			}
		});
		opcodes.put("SWAP", new Opcode() {			
			public void invoke(Asm asm, String...  args) {
				asm.swap(Integer.parseInt(args[0]));
			}
		});
		opcodes.put("JMP", new Opcode() {			
			public void invoke(Asm asm, String...  args) {
				String label = args[0];
				try {
					asm.jmp(Integer.parseInt(args[0]));
				}
				catch(NumberFormatException e) {
					asm.jmp(label);
				}
			}
		});
		opcodes.put("INVOKE", new Opcode() {			
			public void invoke(Asm asm, String...  args) {
				if(args.length>1) {
					asm.invoke(Integer.parseInt(args[0]), Integer.parseInt(args[0])>0);
				}
				else {
					asm.invoke(Integer.parseInt(args[0]));
				}
			}
		});
		opcodes.put("TAIL_CALL", new Opcode() {			
			public void invoke(Asm asm, String...  args) {
				asm.tailcall(Integer.parseInt(args[0]));
			}
		});
		
		opcodes.put("NEW_NAMESPACE", new Opcode() {			
			public void invoke(Asm asm, String...  args) {
				asm.newnamespace();
			}
		});
		opcodes.put("NEW", new Opcode() {			
			public void invoke(Asm asm, String...  args) {
				asm.newobj(Integer.parseInt(args[0]));
			}
		});
		opcodes.put("NEW_ARRAY", new Opcode() {			
			public void invoke(Asm asm, String...  args) {
				asm.newarray(Integer.parseInt(args[0]));
			}
		});
		opcodes.put("NEW_MAP", new Opcode() {			
			public void invoke(Asm asm, String...  args) {
				asm.newmap(Integer.parseInt(args[0]));
			}
		});
		opcodes.put("DEF", new Opcode() {			
			public void invoke(Asm asm, String...  args) {
				/* second parameter is to denote var args */
				asm.def(Integer.parseInt(args[0]), args.length > 1);
			}
		});
		opcodes.put("GEN", new Opcode() {			
			public void invoke(Asm asm, String...  args) {
				/* second parameter is to denote var args */
				asm.gen(Integer.parseInt(args[0]), args.length > 1);
			}
		});
		
        opcodes.put("YIELD", new Opcode() {          
            public void invoke(Asm asm, String...  args) {
                asm.yield();
            }
        });		
		opcodes.put("IS_A", new Opcode() {			
			public void invoke(Asm asm, String...  args) {
				asm.isa();
			}
		});
		opcodes.put("IF", new Opcode() {			
			public void invoke(Asm asm, String...  args) {
				String label = args[0];
				try {
					asm.ifeq(Integer.parseInt(label)); 
				}
				catch(NumberFormatException e) {
					asm.ifeq(label);
				}
				
			}
		});
		opcodes.put("BREAK", new Opcode() {			
			public void invoke(Asm asm, String...  args) {
				asm.brk(args[0]);
			}
		});
		opcodes.put("CONTINUE", new Opcode() {			
			public void invoke(Asm asm, String...  args) {
				asm.cont(args[0]);
			}
		});
		opcodes.put("CLASS_DEF", new Opcode() {			
			public void invoke(Asm asm, String...  args) {
				asm.classdef(Integer.parseInt(args[0]));
			}
		});
		opcodes.put("THROW", new Opcode() {			
			public void invoke(Asm asm, String...  args) {
				asm.throw_();
			}
		});

		/* object access */
		opcodes.put("GET", new Opcode() {			
			public void invoke(Asm asm, String...  args) {
				asm.get();
			}
		});
		opcodes.put("SET", new Opcode() {			
			public void invoke(Asm asm, String...  args) {
				asm.set();
			}
		});
		opcodes.put("GET_GLOBAL", new Opcode() {			
			public void invoke(Asm asm, String...  args) {
				String arg1 = args[0];
				if(arg1.startsWith("\"")) {
					String var = mergeString(args);
					asm.getglobal(var);
				}
				else {
					asm.getglobal(Integer.parseInt(args[0])); 
				}
				
			}
		});
		opcodes.put("SET_GLOBAL", new Opcode() {			
			public void invoke(Asm asm, String...  args) {
				String arg1 = args[0];
				if(arg1.startsWith("\"")) {
					String var = mergeString(args);
					asm.getglobal(var);
				}
				else {
					asm.setglobal(Integer.parseInt(args[0]));
				}
			}
		});

		/* arithmetic operators */
		opcodes.put("ADD", new Opcode() {			
			public void invoke(Asm asm, String...  args) {
				asm.add();
			}
		});
		opcodes.put("SUB", new Opcode() {			
			public void invoke(Asm asm, String...  args) {
				asm.sub();
			}
		});
		opcodes.put("MUL", new Opcode() {			
			public void invoke(Asm asm, String...  args) {
				asm.mul();
			}
		});
		opcodes.put("DIV", new Opcode() {			
			public void invoke(Asm asm, String...  args) {
				asm.div();
			}
		});
		opcodes.put("MOD", new Opcode() {			
			public void invoke(Asm asm, String...  args) {
				asm.mod();
			}
		});
		opcodes.put("NEG", new Opcode() {			
			public void invoke(Asm asm, String...  args) {
				asm.neg();
			}
		});
		opcodes.put("BSL", new Opcode() {			
			public void invoke(Asm asm, String...  args) {
				asm.bsl();
			}
		});
		opcodes.put("BSR", new Opcode() {			
			public void invoke(Asm asm, String...  args) {
				asm.bsr();
			}
		});
		opcodes.put("BNOT", new Opcode() {			
			public void invoke(Asm asm, String...  args) {
				asm.bnot();
			}
		});
		opcodes.put("XOR", new Opcode() {			
			public void invoke(Asm asm, String...  args) {
				asm.xor();
			}
		});
		opcodes.put("LOR", new Opcode() {			
			public void invoke(Asm asm, String...  args) {
				asm.lor();
			}
		});
		opcodes.put("LAND", new Opcode() {			
			public void invoke(Asm asm, String...  args) {
				asm.land();
			}
		});
		opcodes.put("OR", new Opcode() {			
			public void invoke(Asm asm, String...  args) {
				asm.or();
			}
		});
		opcodes.put("AND", new Opcode() {			
			public void invoke(Asm asm, String...  args) {
				asm.and();
			}
		});
		opcodes.put("NOT", new Opcode() {			
			public void invoke(Asm asm, String...  args) {
				asm.not();
			}
		});
		opcodes.put("REQ", new Opcode() {			
			public void invoke(Asm asm, String...  args) {
				asm.req();
			}
		});
		opcodes.put("EQ", new Opcode() {			
			public void invoke(Asm asm, String...  args) {
				asm.eq();
			}
		});
		opcodes.put("NEQ", new Opcode() {			
			public void invoke(Asm asm, String...  args) {
				asm.neq();
			}
		});
		opcodes.put("GT", new Opcode() {			
			public void invoke(Asm asm, String...  args) {
				asm.gt();
			}
		});
		opcodes.put("GTE", new Opcode() {			
			public void invoke(Asm asm, String...  args) {
				asm.gte();
			}
		});
		opcodes.put("LT", new Opcode() {			
			public void invoke(Asm asm, String...  args) {
				asm.lt();
			}
		});
		opcodes.put("LTE", new Opcode() {			
			public void invoke(Asm asm, String...  args) {
				asm.lte();
			}
		});
		opcodes.put("LINE", new Opcode() {			
			public void invoke(Asm asm, String...  args) {
				asm.line(Integer.parseInt(args[0]));
			}
		});
        opcodes.put("INIT_FINALLY", new Opcode() {          
            public void invoke(Asm asm, String...  args) {
                asm.initfinally();
            }
        });		
        opcodes.put("INIT_ON", new Opcode() {          
            public void invoke(Asm asm, String...  args) {
                asm.initon();
            }
        });
        opcodes.put("END_ON", new Opcode() {          
            public void invoke(Asm asm, String...  args) {
                asm.endon();
            }
        });        
        opcodes.put("END_FINALLY", new Opcode() {          
            public void invoke(Asm asm, String...  args) {
                asm.endfinally();
            }
        });        
        opcodes.put("END_BLOCK", new Opcode() {          
            public void invoke(Asm asm, String...  args) {
                asm.endblock();
            }
        });        
	}
}

