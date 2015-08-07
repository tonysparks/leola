/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.vm;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.List;

import leola.ast.ASTNode;
import leola.frontend.ExceptionHandler;
import leola.frontend.LeolaParser;
import leola.frontend.LeolaScanner;
import leola.frontend.ParseException;
import leola.frontend.Parser;
import leola.frontend.Scanner;
import leola.frontend.Source;
import leola.frontend.Token;
import leola.frontend.events.SyntaxErrorEvent;
import leola.frontend.events.SyntaxErrorListener;
import leola.frontend.listener.EventDispatcher;
import leola.frontend.tokens.LeolaErrorCode;
import leola.lang.ArrayLeolaLibrary;
import leola.lang.CollectionsLeolaLibrary;
import leola.lang.DateLeolaLibrary;
import leola.lang.DebugLeolaLibrary;
import leola.lang.LangLeolaLibrary;
import leola.lang.MapLeolaLibrary;
import leola.lang.ReflectionLeolaLibrary;
import leola.lang.StringLeolaLibrary;
import leola.lang.SystemLeolaLibrary;
import leola.lang.io.IOLeolaLibrary;
import leola.lang.sql.SqlLeolaLibrary;
import leola.vm.compiler.Bytecode;
import leola.vm.compiler.BytecodeEmitter;
import leola.vm.compiler.BytecodeGeneratorVisitor;
import leola.vm.compiler.EmitterScopes;
import leola.vm.debug.DebugListener;
import leola.vm.exceptions.LeolaRuntimeException;
import leola.vm.lib.LeolaLibrary;
import leola.vm.types.LeoNamespace;
import leola.vm.types.LeoNull;
import leola.vm.types.LeoObject;
import leola.vm.types.LeoScopedObject;
import leola.vm.types.LeoString;
import leola.vm.util.ResourceLoader;

/**
 * The Leola Programming Language runtime.  This can be either executed as a stand-alone application or by embedding in a Java application.
 *
 * @author Tony
 *
 */
public class Leola {

	/**
	 * Usage
	 */
	private static final String USAGE =
		"<USAGE> leola " + Args.getOptions() + " <file> [script args] \n" +
		Args.getOptionsWithDescription();

	private static final String LEOLA_COMPILED_EXT = "leolac";
	private static final String LEOLA_EXT = "leola";

	public static final String GLOBAL_SCOPE_NAME = "$G";

	/**
	 * Converts the supplied Java Object into a {@link LeoObject} equivalent
	 * 
	 * @param javaObject
	 * @return the {@link LeoObject} equivalent of the supplied Java Object
	 */
	public static LeoObject toLeoObject(Object javaObject) {
		return LeoObject.valueOf(javaObject);
	}
	
	/**
	 * Runs the {@link Leola} runtime as a stand alone application
	 *
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		if ( args.length == 0 ) {
			System.out.println(USAGE);
		}
		else {

			Args pargs = Args.parse(args);
			try {
    			if ( pargs.executeStatement()) {
    				executeStatement(pargs);
    			}
    			else {
    				executeScript(pargs);
    			}
			}
			catch(ParseException e) {
			    // the exception handlers will display this
			}
			catch(LeolaRuntimeException e) {
			    System.err.println(e.getLeoError());    
			}
		}

	}

	/**
	 * Executes statement the command line statement
	 * 
	 * @param pargs
	 * @throws Exception
	 */
	private static void executeStatement(Args pargs) throws Exception {
        Leola runtime = new Leola(pargs);
        Bytecode code = runtime.compile(new BufferedReader(
                                                new StringReader(pargs.getStatement())));

        if ( pargs.displayBytecode()) {
            System.out.println(code.dump());
        }
             
        LeoObject result = runtime.execute(code);
        if(result.isError()) {
            System.err.println(result);
        }
        else {
            System.out.println(result);
        }

	}
	
	/**
	 * Execute or compile the supplied script
	 * 
	 * @param pargs
	 * @throws Exception
	 */
	private static void executeScript(Args pargs) throws Exception {
	    File scriptFile = new File(pargs.getFileName());
        pargs.getIncludeDirectories()
             .add(new File(scriptFile.getParent()));

        Leola runtime = new Leola(pargs);
        
        boolean isCompiled = runtime.hasLeolaCompiledExtension(scriptFile);
        Bytecode code = !isCompiled ?
                          runtime.compile(scriptFile) :
                          runtime.read(scriptFile);

        if ( pargs.displayBytecode()) {
            System.out.println(code);
        }


        if (! isCompiled && pargs.generateBytecode()) {
            runtime.write(runtime.toLeolaCompiledFile(scriptFile), code);
        }
        else {

            LeoObject result = runtime.execute(code);
            if(result.isError()) {
                System.err.println(result);
            }
        }
	}
	
	/**
	 * A means for retrieving a VM instance
	 * 
	 * @author Tony
	 *
	 */
	private static interface VMReference {
		public VM get();
	}
	
	/**
	 * Varargs
	 */
	private Args args;

	/**
	 * Include directories
	 */
	private List<File> includeDirectories;

	/**
	 * Event Dispatcher
	 */
	private EventDispatcher eventDispatcher;

	/**
	 * Resource loader
	 */
	private ResourceLoader resourceLoader;

	/**
	 * Global namespace
	 */
	private LeoNamespace global;

	/**
	 * Debug listener
	 */
	private DebugListener debugListener;


	/**
	 * Local thread variable for the VM
	 */	
	private VMReference vm;	

	/**
	 * The exception handler
	 */
	private ExceptionHandler exceptionHandler;
	
	/**
	 * @throws Exception
	 */
	public Leola() throws Exception {
		this(new Args());
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public Leola(Args args) throws Exception {
		this.args = args;
		this.eventDispatcher = new EventDispatcher();
		this.exceptionHandler = new DefaultExceptionHandler();

		ParserMessageListener parserListener = new ParserMessageListener();
		this.eventDispatcher.addEventListener(SyntaxErrorEvent.class, parserListener);
		
		setIncludePath(args.getIncludeDirectories());
		this.resourceLoader = new ResourceLoader(this);
		
		Scope globalScope = new Scope(null);
		this.global = new LeoNamespace(globalScope, LeoString.valueOf(GLOBAL_SCOPE_NAME));
		globalScope.getNamespaceDefinitions().storeNamespace(this.global);

		if(args.allowThreadLocal()) {
			this.vm = new VMReference() {				
				private ThreadLocal<VM> vm = new ThreadLocal<VM>() {		
					@Override
					protected VM initialValue() {
						return new VM(Leola.this);
					}
				};
				
				@Override
				public VM get() {				
					return vm.get();
				}
			};
			
			this.vm.get();
		}
		else {
			this.vm = new VMReference() {
				private VM vm = new VM(Leola.this);
				
				@Override
				public VM get() {				
					return this.vm;
				}
			};
		}		
		
		put("$args", args.getScriptArgs());
		put("this", this.global);

		/* allow default system libraries to be loaded */
		boolean isSandboxed = args.isSandboxed();
		args.setSandboxed(false);
		
		try {
			loadLibrary(new LangLeolaLibrary());
			loadLibrary(new StringLeolaLibrary(), "str");
			loadLibrary(new MapLeolaLibrary(), "map");
			loadLibrary(new ArrayLeolaLibrary(), "array");
			loadLibrary(new DateLeolaLibrary(), "date");
			loadLibrary(new CollectionsLeolaLibrary());
	
			
			// AUX libraries
			if ( ! args.isBarebones() && !isSandboxed) {
				loadLibrary(new IOLeolaLibrary(), "io");
				loadLibrary(new SqlLeolaLibrary(), "db");
				loadLibrary(new SystemLeolaLibrary(), "sys");
				loadLibrary(new DebugLeolaLibrary(), "debug");
				loadLibrary(new ReflectionLeolaLibrary(), "reflect");			
			}
		}
		finally {
			args.setSandboxed(isSandboxed);
		}
	}
	
	/**
	 * In Sandboxed mode, all 
	 * access to Java classes are disabled and importing {@link LeolaLibrary}s
	 * is also disabled.
	 * @return true if in Sandboxed mode, false otherwise
	 */
	public boolean isSandboxed() {
		return args.isSandboxed();
	}
	
	/**
	 * @return the varargs
	 */
	public Args getArgs() {
		return args;
	}

	/**
	 * @return the resourceLoader
	 */
	public ResourceLoader getResourceLoader() {
		return resourceLoader;
	}

	/**
	 * @return the includeDirectories
	 */
	public List<File> getIncludePath() {
		return includeDirectories;
	}

	/**
	 * Sets the path
	 *
	 * @param includeDirectories
	 */
	public void setIncludePath(List<File> includeDirectories) {
		this.includeDirectories = includeDirectories;
	}

	/**
	 * Sets the include path
	 * @param paths
	 */
	public void setIncludePath(String paths) {
		this.includeDirectories.clear();

		String[] apaths = paths.split(";");
		for(String path : apaths) {
			this.includeDirectories.add(new File(path));
		}
	}

	/**
	 * @param debugListener the debugListener to set
	 */
	public void setDebugListener(DebugListener debugListener) {
		this.debugListener = debugListener;
	}

	/**
	 * @return the debugListener
	 */
	public DebugListener getDebugListener() {
		return debugListener;
	}

	/**
	 * @return the current working directory
	 */
	public File getWorkingDirectory() {
		return new File(System.getProperty("user.dir"));
	}
	
	/**
     * Determines if the supplied {@link File} has the Leola script
     * file extension.
     * 
     * @param file
     * @return true if the {@link File} is named as a Leola script file
     */
    public boolean hasLeolaExtension(File file) {
        return file.getName().endsWith(LEOLA_EXT);
    }
    
    
    /**
     * Determines if the supplied {@link File} has the Leola compiled script
     * file extension.
     * 
     * @param file
     * @return true if the {@link File} is named as a Leola compiled script file
     */
    public boolean hasLeolaCompiledExtension(File file) {
        return file.getName().endsWith(LEOLA_COMPILED_EXT);
    }
    
    
    /**
     * Creates a new {@link File} based on the supplied {@link File},
     * converts it into a Leola compiled script file extension.
     * 
     * @param file
     * @return a {@link File} that has the Leola comiled script File extension.
     */
    public File toLeolaCompiledFile(File file) {
        String bytecodeFileName = file.getName() 
                + ((file.getName().endsWith(LEOLA_EXT)) 
                        ? "c" : "." + LEOLA_COMPILED_EXT);

        return new File(bytecodeFileName);
    }
    
    /**
     * Throws a {@link LeolaRuntimeException} if currently in sandboxed mode.
     * 
     * This is an internal API used for error checking other components as a convenience method.
     */
    public void errorIfSandboxed() {
        if(isSandboxed()) {
            throw new LeolaRuntimeException("Sandboxed mode is enabled, access restricted.");
        }
    }
    
    private void checkIfSandboxed(Class<?> lib) {
        if(isSandboxed()) {
            throw new LeolaRuntimeException("Sandboxed mode is enabled, can not load library: " + lib.getSimpleName());
        }
    }



	/**
	 * Loads the objects methods into the global {@link Scope}
	 * @param jObject
	 */
	public void loadNatives(Object jObject) {
		loadNatives(this.global.getScope(), jObject);
	}

	/**
	 * Loads the objects methods into the supplied {@link LeoScopedObject}
	 * 
	 * @param scope
	 * @param jObject
	 */
	public void loadNatives(LeoScopedObject scope, Object jObject) {
		scope.getScope().loadNatives(jObject);
	}

	/**
	 * Loads the objects methods into the supplied {@link Scope}
	 * @param scope
	 * @param jObject
	 */
	public void loadNatives(Scope scope, Object jObject) {
		scope.loadNatives(jObject);
	}

	/**
     * Loads the static methods of the native class into the global {@link Scope}
     * @param aClass
     */
    public void loadStatics(Class<?> aClass) {
        loadStatics(this.global.getScope(), aClass);
    }

    /**
     * Loads the static methods of the native class into the supplied {@link LeoScopedObject}
     * 
     * @param scope
     * @param aClass
     */
    public void loadStatics(LeoScopedObject scope, Class<?> aClass) {
        loadStatics(scope.getScope(), aClass);
    }
    
	/**
	 * Loads the static methods of the native class into the supplied {@link Scope}
	 *
	 * @param scope
	 * @param aClass
	 */
	public void loadStatics(Scope scope, Class<?> aClass) {
		scope.loadStatics(aClass);	
	}


	
	/**
	 * Loads a {@link LeolaLibrary} into the global {@link Scope}
	 *
	 * @param lib
	 * @throws Exception
	 */
	public void loadLibrary(LeolaLibrary lib) throws Exception {
		checkIfSandboxed(lib.getClass());
		
		lib.init(this, this.global);
	}

	/**
	 * Loads a {@link LeolaLibrary} into the supplied namespace
	 * 
	 * @param lib
	 * @param namespace
	 * @throws Exception
	 */
	public void loadLibrary(LeolaLibrary lib, String namespace) throws Exception {
		checkIfSandboxed(lib.getClass());
		
		LeoNamespace ns = getOrCreateNamespace(namespace);
		lib.init(this, ns);
	}

	/**
	 * Loads a {@link LeolaLibrary}.
	 *
	 * @param lib
	 * @throws Exception
	 */
	public void loadLibrary(LeolaLibrary lib, LeoNamespace namespace) throws Exception {
		checkIfSandboxed(lib.getClass());
		
		lib.init(this, namespace);
	}

	/**
	 * Places the natives into the supplied namespace
	 *
	 * @param lib
	 * @param namespace
	 * @throws Exception
	 */
	public LeoNamespace putIntoNamespace(Object lib, String namespace) throws Exception {
		Scope nsScope = null;

		LeoNamespace ns = getNamespace(namespace);
		if(ns != null) {
			nsScope = ns.getScope();
		}
		else {
			nsScope = new Scope(this.global.getScope());
			ns = new LeoNamespace(nsScope, LeoString.valueOf(namespace));
			this.global.getNamespaceDefinitions().storeNamespace(ns);
		}

		loadNatives(nsScope, lib);

		return ns;
	}

	/**
	 * Places the natives into the supplied namespace
	 *
	 * @param lib
	 * @param namespace
	 * @throws Exception
	 */
	public LeoNamespace putIntoNamespace(Object lib, LeoNamespace namespace) throws Exception {
		Scope nsScope = namespace.getScope();
		loadNatives(nsScope, lib);
		return namespace;
	}

	/**
	 * Loads a {@link LeolaLibrary}.
	 *
	 * @param libClass
	 * @throws Exception
	 */
	public void loadLibrary(Class<?> libClass, LeoNamespace namespace) throws Exception {
		checkIfSandboxed(libClass);
		
		LeolaLibrary lib = (LeolaLibrary)libClass.newInstance();
		loadLibrary(lib, namespace);
	}

	/**
	 * Loads a {@link LeolaLibrary}.
	 *
	 * @param libClass
	 * @throws Exception
	 */
	public void loadLibrary(Class<?> libClass, String namespace) throws Exception {
		checkIfSandboxed(libClass);
		
		LeoNamespace ns = getOrCreateNamespace(namespace);
		LeolaLibrary lib = (LeolaLibrary)libClass.newInstance();
		loadLibrary(lib, ns);
	}

	/**
	 * Loads a {@link LeolaLibrary}.
	 *
	 * @param libClass
	 * @throws Exception
	 */
	public void loadLibrary(Class<?> libClass) throws Exception {
		checkIfSandboxed(libClass);
		
		LeolaLibrary lib = (LeolaLibrary)libClass.newInstance();
		loadLibrary(lib);
	}

	/**
	 * Places the object in the global {@link Scope}.
	 *
	 * @param reference
	 * @param value
	 */
	public void put(String reference, Object value) {
		put(this.global.getScope(), reference, value);
	}

	/**
	 * Places the object into a specific {@link Scope}
	 *
	 * @param scope
	 * @param reference
	 * @param value
	 */
	public void put(Scope scope, String reference, Object value) {
		scope.storeObject(reference, LeoObject.valueOf(value));
	}

	/**
     * Places the object into a specific {@link LeoScopedObject}
     *
     * @param scope
     * @param reference
     * @param value
     */
    public void put(LeoScopedObject scope, String reference, Object value) {
        put(scope.getScope(), reference, value);
    }
    
    /**
     * Depending on the configuration, this will return the active (if configured to
     * do so, the {@link ThreadLocal} or simply just a shared instance) of {@link VM}.
     * 
     * @see Args#allowThreadLocal()
     * 
     * @return the active {@link VM}
     */
    public VM getActiveVM() {
        return this.vm.get();
    }
    
	/**
	 * Gets a {@link LeoObject} by reference from the global {@link Scope}.
	 *
	 * @param reference
	 * @return the {@link LeoObject}, or null if not found.
	 */
	public LeoObject get(String reference) {
		return get( this.global.getScope(), reference);
	}

	
	/**
     * Gets a {@link LeoObject} by reference from a specific {@link Scope}.
     * 
     * @param scope
     * @param reference
     * @return the {@link LeoObject}, or null if not found
     */
    public LeoObject get(Scope scope, String reference) {
        return scope.getObject(reference);
    }

    /**
     * Gets a {@link LeoObject} by reference from a specific {@link LeoScopedObject}.
     * 
     * @param scope
     * @param reference
     * @return the {@link LeoObject}, or null if not found
     */
    public LeoObject get(LeoScopedObject scope, String reference) {
        return get(scope.getScope(), reference);
    }
    
	/**
	 * Retrieves the namespace or creates it if it isn't found
	 * 
	 * @param namespace
	 * @return the {@link LeoNamespace}
	 */
	public LeoNamespace getOrCreateNamespace(String namespace) {
		LeoNamespace ns = namespace != null ? this.getNamespace(namespace) : this.global;
		if(ns == null) {
			ns = new LeoNamespace(new Scope(this.global.getScope()), LeoString.valueOf(namespace));
			this.global.getScope().getNamespaceDefinitions().storeNamespace(ns);
		}
		return ns;
	}

	/**
	 * Attempts to lookup a {@link LeoNamespace}.
	 * @param namespace
	 * @return the {@link LeoNamespace} object, or null if not found
	 */
	public LeoNamespace getNamespace(String namespace) {
		return this.global.getScope().lookupNamespace(LeoString.valueOf(namespace));
	}

	/**
	 * @return the global namespace
	 */
	public LeoNamespace getGlobalNamespace() {
		return this.global;
	}


	
	/**
	 * Executes the supplied {@link Bytecode}
	 * @param callee
	 * @param code
	 * @param args
	 * @return an object of the resulting execution (always returns an object)
	 * @throws LeolaRuntimeException
	 */
	public LeoObject execute(LeoObject callee, Bytecode code, LeoObject[] args) throws LeolaRuntimeException {
		return this.vm.get().execute(callee,callee, code,args);
	}

	/**
	 * Executes the supplied {@link Bytecode}
	 * @param callee
	 * @param code
	 * @return an object of the resulting execution (always returns an object)
	 * @throws LeolaRuntimeException
	 */
	public LeoObject execute(LeoObject callee, Bytecode code) throws LeolaRuntimeException {
		return this.vm.get().execute(callee,callee, code);
	}

	/**
	 * Executes the supplied {@link Bytecode}
	 * @param code
	 * @param args
	 * @return an object of the resulting execution (always returns an object)
	 * @throws LeolaRuntimeException
	 */
	public LeoObject execute(Bytecode code, LeoObject[] args) throws LeolaRuntimeException {
		return this.vm.get().execute(this.global,this.global, code,args);
	}

	/**
	 * Executes the supplied {@link Bytecode}
	 * @param code
	 * @return an object of the resulting execution (always returns an object)
	 * @throws LeolaRuntimeException
	 */
	public LeoObject execute(Bytecode code) throws LeolaRuntimeException {
		return this.vm.get().execute(this.global, this.global, code);
	}


	/**
	 * Executes the function
	 *
	 * @param function
	 * @param arg1
	 * @return an object of the resulting execution (always returns an object)
	 * @throws LeolaRuntimeException
	 */
	public LeoObject execute(LeoObject function, LeoObject arg1) throws LeolaRuntimeException {
		return function.call(arg1);
	}

	/**
	 * Executes the function
	 *
	 * @param function
	 * @param arg1
	 * @param arg2
	 * @return an object of the resulting execution (always returns an object)
	 * @throws LeolaRuntimeException
	 */
	public LeoObject execute(LeoObject function, LeoObject arg1, LeoObject arg2) throws LeolaRuntimeException {
		return function.call(arg1, arg2);
	}

	/**
	 * Executes the function
	 *
	 * @param function
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @return an object of the resulting execution (always returns an object)
	 * @throws LeolaRuntimeException
	 */
	public LeoObject execute(LeoObject function, LeoObject arg1, LeoObject arg2, LeoObject arg3) throws LeolaRuntimeException {
		return function.call(arg1, arg2, arg3);
	}


	/**
	 * Executes the function
	 *
	 * @param function
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @param arg4
	 * @return an object of the resulting execution (always returns an object)
	 * @throws LeolaRuntimeException
	 */
	public LeoObject execute(LeoObject function, LeoObject arg1, LeoObject arg2, LeoObject arg3, LeoObject arg4) throws LeolaRuntimeException {
		return function.call(arg1, arg2, arg3, arg4);
	}


	/**
	 * Executes the function
	 *
	 * @param function
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @param arg4
	 * @param arg5
	 * @return an object of the resulting execution (always returns an object)
	 * @throws LeolaRuntimeException
	 */
	public LeoObject execute(LeoObject function, LeoObject arg1, LeoObject arg2, LeoObject arg3, LeoObject arg4, LeoObject arg5) throws LeolaRuntimeException {
		return function.call(arg1, arg2, arg3, arg4, arg5);
	}

	/**
	 * Executes the function
	 *
	 * @param function
	 * @param args
	 * @return an object of the resulting execution (always returns an object)
	 * @throws LeolaRuntimeException
	 */
	public LeoObject execute(LeoObject function, LeoObject[] args) throws LeolaRuntimeException {
		return function.call(args);
	}

	/**
	 * Executes the function
	 *
	 * @param function
	 * @return an object of the resulting execution (always returns an object)
	 * @throws LeolaRuntimeException
	 */
	public LeoObject execute(LeoObject function) throws LeolaRuntimeException {
		return function.call();
	}


	/**
	 * Evaluates the inlined source code.
	 * <pre>
	 * 	leola.eval("val x = 10; println(x);");
	 * </pre>
	 *
	 * @param inlineSource
	 * @return
	 * @throws Exception
	 */
	public LeoObject eval(String inlineSource) throws Exception {
		return eval(new BufferedReader(new StringReader(inlineSource)));
	}

	public LeoObject eval(InputStream iStream) throws Exception {
		return eval(new BufferedReader(new InputStreamReader(iStream)) );
	}

	/**
	 * Checks the file extension, if it ends in "leolac" it will treat it as a
	 * compiled script and attempt to evaluate the bytecode.
	 *
	 * @param file
	 * @return the resulting {@link LeoObject}
	 * @throws Exception
	 */
	public LeoObject eval(File file) throws Exception {
		return eval(file, Leola.GLOBAL_SCOPE_NAME);
	}


	/**
	 * Checks the file extension, if it ends in "leolac" it will treat it as a
	 * compiled script and attempt to evaluate the bytecode.
	 *
	 * @param file
	 * @param namespace -- if the namespace isn't found, a new one is created
	 * @return the resulting {@link LeoObject}
	 * @throws Exception
	 */
	public LeoObject eval(File file, String namespace) throws Exception {
		LeoNamespace ns = getOrCreateNamespace(namespace);

		LeoObject result = LeoNull.LEONULL;
		boolean isCompiled = hasLeolaCompiledExtension(file);
		if(isCompiled) {
			BufferedInputStream iStream = new BufferedInputStream(new FileInputStream(file));
			DataInput in = new DataInputStream(iStream);


			Bytecode bytecode = Bytecode.read(ns, in);
			bytecode.setSourceFile(file.getName());

			result = execute(bytecode);
		}
		else {
			Bytecode bytecode = compile(new BufferedReader(new FileReader(file)), this.exceptionHandler);
			bytecode.setSourceFile(file.getName());

			result = execute(ns, bytecode);
		}

		return result;
	}

	public LeoObject eval(BufferedReader reader) throws Exception {
		Bytecode bytecode = compile(reader, this.exceptionHandler);
		LeoObject result = execute(bytecode);
		return result;
	}

	public LeoObject eval(BufferedReader reader, LeoNamespace namespace) throws Exception {
		Bytecode bytecode = compile(reader, this.exceptionHandler);
		LeoObject result = execute(namespace, bytecode);
		return result;
	}

	/**
	 * Reads the {@link Bytecode} from the {@link File}
	 * 
	 * @param scriptFile
	 * @return the {@link Bytecode}
	 * @throws Exception
	 */
	public Bytecode read(File scriptFile) throws Exception {
	    Bytecode code = read(new BufferedInputStream(new FileInputStream(scriptFile)));
	    if(code != null) {
	        code.setSourceFile(scriptFile.getName());
	    }
	    
	    return code;
	}
	
	
	/**
	 * Reads the {@link Bytecode} from the {@link InputStream}
	 * 
	 * @param iStream
	 * @return the {@link Bytecode}
	 * @throws Exception
	 */
	public Bytecode read(InputStream iStream) throws Exception {
	    try {
            DataInput in = new DataInputStream(iStream);            
            Bytecode code = Bytecode.read(getGlobalNamespace(), in);            
            return code;
	    }
	    finally {
	        if(iStream != null) {
	            iStream.close();
	        }
	    }
	}

	/**
	 * Writes out the {@link Bytecode} to the {@link File}
	 * 
	 * @param scriptFile
	 * @param bytecode
	 * @throws Exception
	 */
	public void write(File scriptFile, Bytecode bytecode) throws Exception {
        FileOutputStream fStream = null;
        try {
            fStream = new FileOutputStream(scriptFile);

            if (scriptFile.exists()) {
                fStream.getChannel().truncate(0);
            }

            write(new BufferedOutputStream(fStream), bytecode);
        }
        finally {
            if (fStream != null) {
                fStream.close();
            }
        }
	}
	
	/**
	 * Writes the {@link Bytecode} out to the {@link OutputStream}
	 * 
	 * @param oStream
	 * @param bytecode
	 * @throws Exception
	 */
	public void write(OutputStream oStream, Bytecode bytecode) throws Exception {

        try {
            DataOutput output = new DataOutputStream(oStream);
            bytecode.write(output);
            oStream.flush();
        }
        finally {
            if (oStream != null) {
                oStream.close();
            }
        }
	    
	}
	
	/**
	 * Compiles the supplied script file
	 * 
	 * @param scriptFile
	 * @return the {@link Bytecode}
	 * @throws Exception
	 */
	public Bytecode compile(File scriptFile) throws Exception {
	    Bytecode code = compile(new BufferedReader(new FileReader(scriptFile)));
        code.setSourceFile(scriptFile.getName());
        return code;
	}
	
	/**
	 * Compiles the file.
	 *
	 * @param reader
	 * @return
	 * @throws Exception
	 */
	public Bytecode compile(BufferedReader reader) throws Exception {
		return compile(reader, this.exceptionHandler);
	}

	/**
	 * Compiles the file.
	 *
	 * @param reader
	 * @return
	 * @throws Exception
	 */
	public Bytecode compile(BufferedReader reader, ExceptionHandler exceptionHandler) throws Exception {
		ASTNode program = generateAST(reader, exceptionHandler);
		BytecodeGeneratorVisitor gen = new BytecodeGeneratorVisitor(this, new EmitterScopes());
		program.visit(gen);
		BytecodeEmitter asm = gen.getAsm();
		Bytecode bytecode = asm.compile();

		return bytecode;
	}

	/**
	 * Evaluates the file.
	 *
	 * @param file
	 * @throws Exception
	 */
	public ASTNode generateAST(File file) throws Exception {
		return generateAST(new BufferedReader(new FileReader(file)), this.exceptionHandler);
	}

	/**
	 * Reads in the inline source.
	 *
	 * @param inlineSource
	 * @throws Exception
	 */
	public ASTNode generateAST(String inlineSource) throws Exception {
		return generateAST(new BufferedReader(new StringReader(inlineSource)), this.exceptionHandler);
	}

	/**
	 * Evaluate the stream.
	 *
	 * @param iStream
	 * @throws Exception
	 */
	public ASTNode generateAST(InputStream iStream) throws Exception {
		return generateAST(new BufferedReader(new InputStreamReader(iStream)), this.exceptionHandler );
	}


    /**
     * Generates an Abstract Syntax Tree from the stream.
     *
     * @param reader
     * @return the root node of the AST
     * @throws Exception
     */
    public ASTNode generateAST(BufferedReader reader, ExceptionHandler exceptionHandler) throws Exception {
        final Source source = new Source(this.eventDispatcher, reader);

        Scanner scanner = new LeolaScanner(source);
        Parser parser = new LeolaParser(scanner, exceptionHandler);

        ASTNode program = null;
    	try {
    		program = parser.parse();
    	}
    	finally {
    		source.close();
    	}

        return program;
    }

    /**
     * Listener for parser messages.
     */
    private class ParserMessageListener implements SyntaxErrorListener {
    	
        @Override
    	public void onEvent(SyntaxErrorEvent event) {
            int lineNumber = event.getLineNumber();
            int position = event.getPosition();
            String tokenText = event.getTokenText();
            String errorMessage = event.getErrorMessage();
            Source source = event.getSourceCode();

            int spaceCount = /*PREFIX_WIDTH + */position;
            String currentLine = source.getCurrentLine();
            StringBuilder flagBuffer = new StringBuilder(currentLine != null ? currentLine : "");
            flagBuffer.append("\n");

            // Spaces up to the error position.
            for (int i = 1; i < spaceCount; ++i) {
                flagBuffer.append(' ');
            }

            // A pointer to the error followed by the error message.
            flagBuffer.append("^\n*** ").append(errorMessage);

            // Text, if any, of the bad token.
            if (tokenText != null) {
                flagBuffer.append(" [at line: ")
                          .append(lineNumber)
                          .append(" '").append(tokenText).append("']");
            }

            System.err.println(flagBuffer.toString());
    	}
    }

    /**
     * Default exception handler
     *
     * @author Tony
     *
     */
    private class DefaultExceptionHandler implements ExceptionHandler {
        private int errorCount;
        
        @Override
        public int getErrorCount() {
            return errorCount;
        }
        
        @Override
        public void errorToken(Token token, Parser parser, LeolaErrorCode errorCode) {
            errorCount++;
            
        	eventDispatcher.sendNow(new SyntaxErrorEvent(this, parser.getSource(), token, errorCode.toString()));
            throw new ParseException(errorCode,
                token.getText() + " errored because of : " + errorCode + " at line: " + token.getLineNumber() + " at " + token.getPosition());
        }


        @Override
        public void onException(Exception e) {
            errorCount++;
            
            throw new LeolaRuntimeException(e);
        }
    }
}

