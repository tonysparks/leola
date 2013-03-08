/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.vm;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import leola.vm.types.LeoArray;
import leola.vm.types.LeoObject;
import leola.vm.util.LeoTypeConverter;

/**
 * Command line argument parser
 * 
 * @author Tony
 *
 */
public class Args {

	private String fileName;
	private boolean displayBytecode;
	private boolean generateBytecode;
	private boolean barebones;
	private boolean isExecuteStatement;
	private boolean isDebugMode;
	private String statement;
	private LeoObject scriptArgs;
	private int stackSize;
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
	 * @throws Exception
	 */
	public static Args parse(String ... args) throws Exception {
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
			
			LeoObject leoScriptArgs = LeoTypeConverter.convertToLeolaType(sargs);
			pargs.scriptArgs = leoScriptArgs;
		}
		
		return pargs;
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
	public boolean isExecuteStatement() {
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
}

