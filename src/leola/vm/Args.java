/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.vm;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import leola.vm.exceptions.LeolaRuntimeException;
import leola.vm.lib.LeolaLibrary;
import leola.vm.types.LeoArray;
import leola.vm.types.LeoObject;

/**
 * {@link Leola} VM arguments
 * 
 * @author Tony
 *
 */
public class Args {

	
	/**
	 * A builder for {@link Args}.
	 * 
	 * <p>
	 * Example usage:
	 * <pre>
	 * Args args = new ArgsBuilder().setFileName("something.leola").setStackSize(1024).build();
	 * Leola runtime = new Leola(args);
	 * </pre>
	 * 
	 * @author Tony
	 *
	 */
	public static class ArgsBuilder {
		
		private Args args;
		
		/**
		 */
		public ArgsBuilder() {
			this.args = new Args();
		}
		
		/**
		 * The file name of the executed script.  This is used
		 * for debugging purposes.
		 * 
		 * @param fileName
		 * @return the {@link ArgsBuilder} for method chaining
		 */
		public ArgsBuilder setFileName(String fileName) {
			args.setFileName(fileName);
			return this;
		}
		
		
		/**
		 * Displays the generated bytecode to System.out
		 * Defaults to false
		 * 
		 * @param displayBytecode
		 * @return the {@link ArgsBuilder} for method chaining
		 */
		public ArgsBuilder setDisplayBytecode(boolean displayBytecode) {
			args.setDisplayBytecode(displayBytecode);
			return this;
		}
		
		
		/**
		 * Compiles to Leola bytecode and does not interpret the code.
		 * Defaults to false
		 * 
		 * @param generateBytecode
		 * @return the {@link ArgsBuilder} for method chaining
		 */
		public ArgsBuilder setGenerateBytecode(boolean generateBytecode) {
			args.setGenerateBytecode(generateBytecode);
			return this;
		}
		
		
		/**
		 * Does not load any auxilary libraries.  
		 * Defaults to false
		 * 
		 * @param barebones
		 * @return the {@link ArgsBuilder} for method chaining
		 */
		public ArgsBuilder setBarebones(boolean barebones) {
			args.setBarebones(barebones);
			return this;
		}
		
		
		/**
		 * Sets to interpret an in bound string.
		 * Defaults to false
		 * 
		 * @param executeStatement
		 * @see ArgsBuilder#setStatement(String)
		 * 
		 * @return the {@link ArgsBuilder} for method chaining
		 */
		public ArgsBuilder setExecuteStatement(boolean executeStatement) {
			args.setExecuteStatement(executeStatement);
			return this;
		}
		
		
		/**
		 * Enables debugging symbols.  Use this for better compiler/interpreter
		 * error messages.  This does slow down execution so this should only
		 * be enabled during development.
		 * 
		 * Defaults to false
		 * @param debugMode
		 * @return the {@link ArgsBuilder} for method chaining
		 */
		public ArgsBuilder setIsDebugMode(boolean debugMode) {
			args.setDebugMode(debugMode);
			return this;
		}
		
		
		/**
		 * This enables the usage of creating a stack per Thread.  This allows
		 * for safe concurrent code execution from the VM's perspective.  You
		 * would only want to disable this if memory is a limiting factor or
		 * if you do not have an easy means for managing the Threads (which could
		 * cause memory leaks).
		 * 
		 * Defaults to true.
		 * @param allowThreadLocals
		 * @return the {@link ArgsBuilder} for method chaining
		 */
		public ArgsBuilder setAllowThreadLocals(boolean allowThreadLocals) {
			args.enableVMThreadLocal(allowThreadLocals);
			return this;
		}
		
		
		/**
		 * A statement to be executed immediately.
		 * 
		 * @param statement
		 * @return the {@link ArgsBuilder} for method chaining
		 */
		public ArgsBuilder setStatement(String statement) {
			args.setStatement(statement);
			return this;
		}
		
		
		/**
		 * Arguments passed to the runtime -- a kin to program 
		 * arguments.
		 * 
		 * @param scriptArgs
		 * @return the {@link ArgsBuilder} for method chaining
		 */
		public ArgsBuilder setScriptArguments(LeoObject scriptArgs) {
			args.setScriptArgs(scriptArgs);
			return this;
		}

		/**
		 * The VM stack size.
		 * 
		 * Defaults to {@link VM#DEFAULT_STACKSIZE}
		 * 
		 * @param stackSize
		 * @return the {@link ArgsBuilder} for method chaining
		 */
		public ArgsBuilder setStackSize(int stackSize) {
			args.setStackSize(stackSize);
			return this;
		}
		
		/**
		 * Sets the maximum stack size for this VM.
		 * 
		 * @param maxStackSize
		 * @return the {@link ArgsBuilder} for method chaining
		 */
		public ArgsBuilder setMaxStackSize(int maxStackSize) {
		    args.setMaxStackSize(maxStackSize);
		    return this;
		}
		
		/**
		 * Sets the VM to sandboxed mode.  In sandboxed mode, all 
		 * access to Java classes are disabled and importing {@link LeolaLibrary}s
		 * is also disabled.
		 * Defaults to disabling Sandboxed mode.
		 * 
		 * @param isSandboxed
		 * @return the {@link ArgsBuilder} for method chaining 
		 */
		public ArgsBuilder setSandboxed(boolean isSandboxed) {
			args.setSandboxed(isSandboxed);
			return this;
		}
		
		/**
		 * Directories to be included on scanning for include and require
		 * statements
		 * 
		 * @param directories
		 * @return the {@link ArgsBuilder} for method chaining
		 */
		public ArgsBuilder setIncludeDirectories(List<File> directories) {
			args.setIncludeDirectories(directories);
			return this;
		}
		
		/**
		 * Builds the {@link Args} structure with the configuration
		 * of the {@link ArgsBuilder}.
		 * 
		 * @return the {@link Args}
		 */
		public Args build() {
			return args;
		}
	}
	
	private String fileName;
	private boolean displayBytecode;
	private boolean generateBytecode;
	private boolean barebones;
	private boolean isExecuteStatement;
	private boolean isDebugMode;
	private boolean allowThreadLocals;
	private boolean isSandboxed;
	private String statement;
	private LeoObject scriptArgs;
	private int stackSize;
	private int maxStackSize;	
	private List<File> includeDirectories = new ArrayList<File>();
	
	/**
	 * Available arguments
	 */
	private static final String[][] args =
	{
		{ "b", "Outputs bytecode" },
		{ "d", "Displays generated bytecode" },
		{ "g", "Enables debug mode" },
		{ "s", "Does not include any libraries" },
		{ "r", "Executes a supplied statement"	},
		{ "x", "Sets the stack size. Ex. x=1024 "	},
		{ "mx", "Sets the max stack size. Ex. mx=1000000" },
		{ "t", "Disables allocating a VM per thread. "	},
		{ "cp", "Path names to be included on include, require look ups.  Use a ';' as " +
					"a path separater. \n\t\t Ex. \"cp=C:/My Documents/libs;C:/leola/libs\" " },
	};
	
	/**
	 * @return the list of available options
	 */
	public static String getOptions() {
		StringBuilder sb = new StringBuilder(100);
		for(int i = 0; i < args.length; i++) {
			sb.append(args[i][0]);			
		}		
		return sb.toString();
	}
	
	/**
	 * @return a string denoting all of the command line options
	 */
	public static String getOptionsWithDescription() {
		StringBuilder sb = new StringBuilder(100);
		for(int i = 0; i < args.length; i++) {
			sb.append("\t").append(args[i][0])
			  .append("\t").append(args[i][1]);
			
			sb.append("\n");
		}
		
		return sb.toString();
	}
	
	/**
	 * Parses the argument list
	 * @param args
	 * @return
	 * @throws LeolaRuntimeException
	 */
	public static Args parse(String ... args) throws LeolaRuntimeException {
		Args pargs = new Args();
		
		int startProgramArgs = 0;
		int i = 0;
		for(; i < args.length; i++) {
			String arg = args[i];
			if ( arg.startsWith("b") ) {
				pargs.generateBytecode = true;
			}
			else if ( arg.startsWith("d")) {
				pargs.displayBytecode = true;
			}
			else if ( arg.startsWith("s")) {
				pargs.barebones = true;
			}
			else if (arg.startsWith("g")) {
				pargs.isDebugMode = true;
			}
			else if (arg.startsWith("t")) {
				pargs.allowThreadLocals = false;
			}				
			else if ( arg.startsWith("cp=") ) {
				String[] paths = arg.replace("cp=", "").split(";");
				for(String path : paths) {
					pargs.includeDirectories.add(new File(path));
				}
			}
			else if ( arg.startsWith("x=") ) {
				String value = arg.replace("x=", "");
				pargs.stackSize = Integer.parseInt(value);
			}
			else if ( arg.startsWith("mx=") ) {
                String value = arg.replace("mx=", "");
                pargs.maxStackSize = Integer.parseInt(value);
            }
			else if ( arg.startsWith("r") ) {
				pargs.isExecuteStatement = true;
				pargs.statement = "";
				for(int j = i + 1; j < args.length; j++) {
					pargs.statement += " " + args[j];
				}
				
				break;
			}
			else {
				pargs.fileName = arg;
				startProgramArgs = i;
				break;
			}
		}
		
		i=0; // account for file name argument
		
		if ( startProgramArgs < args.length && startProgramArgs > -1 ) {
			String[] sargs = new String[args.length-startProgramArgs];			
			System.arraycopy(args, startProgramArgs, sargs, 0, sargs.length);
			
			LeoObject leoScriptArgs = LeoObject.valueOf(sargs);
			pargs.scriptArgs = leoScriptArgs;
		}
		
		return pargs;
	}
	
	/**
	 * 
	 */
	public Args() {
		this.allowThreadLocals=true;
		this.maxStackSize = Integer.MAX_VALUE;
	}
	
	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}
	
	/**
	 * @return the displayBytecode
	 */
	public boolean displayBytecode() {
		return displayBytecode;
	}
	
	/**
	 * @return the generateBytecode
	 */
	public boolean generateBytecode() {
		return generateBytecode;
	}
	
	/**
	 * @return the stackSize
	 */
	public int getStackSize() {
		return stackSize;
	}
	
	/**
	 * @return the barebones
	 */
	public boolean isBarebones() {
		return barebones;
	}
	
	/**
	 * @return the isDebugMode
	 */
	public boolean isDebugMode() {
		return isDebugMode;
	}
	
	/**
	 * @return the isExecuteStatement
	 */
	public boolean executeStatement() {
		return isExecuteStatement;
	}
	
	/**
	 * @return the statement
	 */
	public String getStatement() {
		return statement;
	}
	
	/**
	 * @return the scripts arguments
	 */
	public LeoObject getScriptArgs() {
		if(this.scriptArgs==null) return new LeoArray(0);
		return this.scriptArgs;
	}
		
	/**
	 * @return the includeDirectories
	 */
	public List<File> getIncludeDirectories() {
		return includeDirectories;
	}

	/**
	 * Default is true.  
	 * 
	 * @see #enableVMThreadLocal(boolean)
	 * @return true if the use of thread locals is enabled
	 */
	public boolean allowThreadLocal() {
		return this.allowThreadLocals;
	}
	
	/**
	 * @return the isSandboxed
	 */
	public boolean isSandboxed() {
		return isSandboxed;
	}
	
	/**
	 * @param isSandboxed the isSandboxed to set
	 */
	public void setSandboxed(boolean isSandboxed) {
		this.isSandboxed = isSandboxed;
	}
	
	/**
	 * @param fileName the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @param displayBytecode the displayBytecode to set
	 */
	public void setDisplayBytecode(boolean displayBytecode) {
		this.displayBytecode = displayBytecode;
	}

	/**
	 * @param generateBytecode the generateBytecode to set
	 */
	public void setGenerateBytecode(boolean generateBytecode) {
		this.generateBytecode = generateBytecode;
	}

	/**
	 * @param barebones the barebones to set
	 */
	public void setBarebones(boolean barebones) {
		this.barebones = barebones;
	}

	/**
	 * @param isExecuteStatement the isExecuteStatement to set
	 */
	public void setExecuteStatement(boolean isExecuteStatement) {
		this.isExecuteStatement = isExecuteStatement;
	}

	/**
	 * @param statement the statement to set
	 */
	public void setStatement(String statement) {
		this.statement = statement;
	}

	/**
	 * @param scriptArgs the scriptArgs to set
	 */
	public void setScriptArgs(LeoObject scriptArgs) {
		this.scriptArgs = scriptArgs;
	}

	/**
	 * @param stackSize the stackSize to set
	 */
	public void setStackSize(int stackSize) {
		this.stackSize = stackSize;
	}
	
	/**
     * @return the maxStackSize
     */
    public int getMaxStackSize() {
        return maxStackSize;
    }
    
    /**
     * @param maxStackSize the maxStackSize to set
     */
    public void setMaxStackSize(int maxStackSize) {
        this.maxStackSize = maxStackSize;
    }

	/**
	 * @param includeDirectories the includeDirectories to set
	 */
	public void setIncludeDirectories(List<File> includeDirectories) {
		this.includeDirectories = includeDirectories;
	}		
	
	/**
	 * @param isDebugMode the isDebugMode to set
	 */
	public void setDebugMode(boolean isDebugMode) {
		this.isDebugMode = isDebugMode;
	}
	
	/**
	 * When this is disabled, the Leola runtime will not spawn
	 * a {@link VM} instance per thread in which the runtime is invoked
	 * on.  This property is enabled by default to allow for better
	 * multi-threaded support.  
	 * 
	 * @param allow
	 */
	public void enableVMThreadLocal(boolean allow) {
		this.allowThreadLocals = allow;
	}
}

