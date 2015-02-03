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
import java.io.StringReader;
import java.util.ArrayList;
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
import leola.frontend.events.ParserSummaryEvent;
import leola.frontend.events.ParserSummaryListener;
import leola.frontend.events.SourceLineEvent;
import leola.frontend.events.SourceLineListener;
import leola.frontend.events.SyntaxErrorEvent;
import leola.frontend.events.SyntaxErrorListener;
import leola.frontend.listener.EventDispatcher;
import leola.frontend.tokens.LeolaErrorCode;
import leola.lang.ArrayLeolaLibrary;
import leola.lang.DateLeolaLibrary;
import leola.lang.DebugLeolaLibrary;
import leola.lang.LangLeolaLibrary;
import leola.lang.MapLeolaLibrary;
import leola.lang.ReflectionLeolaLibrary;
import leola.lang.StringLeolaLibrary;
import leola.lang.SystemLeolaLibrary;
import leola.lang.actors.ActorLibrary;
import leola.lang.collection.CollectionsLeolaLibrary;
import leola.lang.io.IOLeolaLibrary;
import leola.lang.sql.SqlLeolaLibrary;
import leola.vm.asm.AsmEmitter;
import leola.vm.asm.Bytecode;
import leola.vm.asm.Scope;
import leola.vm.asm.Symbols;
import leola.vm.compiler.BytecodeGenerator;
import leola.vm.debug.DebugListener;
import leola.vm.exceptions.LeolaRuntimeException;
import leola.vm.lib.LeolaLibrary;
import leola.vm.types.LeoNamespace;
import leola.vm.types.LeoNull;
import leola.vm.types.LeoObject;
import leola.vm.types.LeoScopedObject;
import leola.vm.util.LeoTypeConverter;
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
		return LeoTypeConverter.convertToLeolaType(javaObject);
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

			if ( pargs.isExecuteStatement()) {

				List<File> includeDirectories = pargs.getIncludeDirectories();
				Leola runtime = new Leola(pargs);
				runtime.setIncludePath(includeDirectories);

				Bytecode code = runtime.compile(new BufferedReader(new StringReader(pargs.getStatement())));

				/* display the bytecode */
				if ( pargs.displayBytecode()) {
					System.out.println(code.dump());
				}
								
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
			else {
				String file = pargs.getFileName();
				boolean isCompiled = file.endsWith(LEOLA_COMPILED_EXT);

				File scriptFile = new File(file);
				// add the files directory to the path..
				List<File> includeDirectories = pargs.getIncludeDirectories();
				includeDirectories.add(new File(scriptFile.getParent()));

				Leola runtime = new Leola(pargs);
				runtime.setIncludePath(includeDirectories);


				Bytecode code = null;
				if ( !isCompiled ) {
					try {
						code = runtime.compile(new BufferedReader(new FileReader(scriptFile)));
						code.setSourceFile(scriptFile.getName());
					}
					catch(ParseException e) {
						return;	/* let the syntax handler display the error */
					}
				}
				else {
					BufferedInputStream iStream = new BufferedInputStream(new FileInputStream(scriptFile));
					DataInput in = new DataInputStream(iStream);
					code = Bytecode.read(runtime.getGlobalNamespace(), runtime.getSymbols(), in);
					code.setSourceFile(scriptFile.getName());
				}


				/* display the bytecode */
				if ( pargs.displayBytecode()) {
					System.out.println(code.dump());
				}


				if (! isCompiled && pargs.generateBytecode()) {
					String bytecodeFileName = file + ((file.endsWith(LEOLA_EXT))
														? "c" : "." + LEOLA_COMPILED_EXT);

					File pFile = new File(bytecodeFileName);

					FileOutputStream fStream = new FileOutputStream(pFile);
					if ( pFile.exists() ) {
						fStream.getChannel().truncate(0);
					}

					BufferedOutputStream oStream = new BufferedOutputStream(fStream);
					DataOutput output = new DataOutputStream(oStream);

					code.write(output);
					oStream.flush();
					oStream.close();
				}
				else {
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
	 * Symbols
	 */
	private Symbols symbols;

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

		SourceMessageListener sourceListener = new SourceMessageListener();
		this.eventDispatcher.addEventListener(SourceLineEvent.class, sourceListener);

		ParserMessageListener parserListener = new ParserMessageListener();
		this.eventDispatcher.addEventListener(SyntaxErrorEvent.class, parserListener);
		this.eventDispatcher.addEventListener(ParserSummaryEvent.class, parserListener);

		this.includeDirectories = new ArrayList<File>();
		this.resourceLoader = new ResourceLoader(this);



		this.symbols = new Symbols();
		Scope globalScope = this.symbols.getGlobalScope(); /* unsure the global scope */
		this.global = new LeoNamespace(globalScope, GLOBAL_SCOPE_NAME);
		globalScope.getNamespaceDefinitions().storeNamespace(GLOBAL_SCOPE_NAME, this.global);

		if(args.isAllowThreadLocal()) {
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
		
		putGlobal("$args", args.getScriptArgs());
		putGlobal("this", this.global);

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
				loadLibrary(new ActorLibrary(), "act");
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
	 * @return the symbols
	 */
	public Symbols getSymbols() {
		return symbols;
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
	 * Loads the static methods of the native class into the global {@link Scope}
	 * @param aClass
	 */
	public void loadStaticsGlobal(Class<?> aClass) {
		loadStatics(this.symbols.getGlobalScope(), aClass);
	}

	/**
	 * Loads the objects methods into the global {@link Scope}
	 * @param jObject
	 */
	public void loadNativesGlobal(Object jObject) {
		loadNatives(this.symbols.getGlobalScope(), jObject);
	}

	/**
	 * Loads the objects methods into the supplied {@link Scope}
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
	 * Loads the static methods of the native class into the current {@link Scope}
	 * @param aClass
	 */
	public void loadStatics(Class<?> aClass) {
		loadStatics(this.symbols.peek(), aClass);
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
	 * Throws a {@link LeolaRuntimeException} if currently in sandboxed mode.
	 * 
	 * This is an internal API used for error checking other components as a convienience method.
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
	 * Loads a {@link LeolaLibrary}.
	 *
	 * @param lib
	 * @throws Exception
	 */
	public void loadLibrary(LeolaLibrary lib) throws Exception {
		checkIfSandboxed(lib.getClass());
		
		lib.init(this, this.global);
	}

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
			nsScope = getSymbols().newObjectScope();
			ns = new LeoNamespace(nsScope, namespace);
			getSymbols().peek().getNamespaceDefinitions().storeNamespace(namespace, ns);

			// TODO: Do we want the namespaces accessible?
			// putGlobal(namespace, ns);
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
	public void putGlobal(String reference, Object value) {
		put(this.symbols.getGlobalScope(), reference, value);
	}

	/**
	 * Places the object into a specific {@link Scope}
	 *
	 * @param scope
	 * @param reference
	 * @param value
	 */
	public void put(Scope scope, String reference, Object value) {
		scope.storeObject(reference, LeoTypeConverter.convertToLeolaType(value));
	}

	/**
	 * Places an object into the current scope.
	 * @param reference
	 * @param value
	 */
	public void put(String reference, Object value) {
		put(this.symbols.peek(), reference, value);
	}

	/**
	 * Gets a {@link LeoObject} by reference from the global {@link Scope}.
	 *
	 * @param reference
	 * @return the {@link LeoObject}, or null if not found.
	 */
	public LeoObject getGlobal(String reference) {
		return get( this.symbols.getGlobalScope(), reference);
	}

	/**
	 * Gets a {@link LeoObject} by reference.
	 * @param reference
	 * @return the {@link LeoObject}, or null if not found.
	 */
	public LeoObject get(String reference) {
		LeoObject result = this.symbols.getObject(reference);
		return result;
	}

	/**
	 * Retrieves the namespace or creates it if it isn't found
	 * @param namespace
	 * @return
	 */
	public LeoNamespace getOrCreateNamespace(String namespace) {
		LeoNamespace ns = namespace != null ? this.getNamespace(namespace) : this.global;
		if(ns == null) {
			ns = new LeoNamespace(getSymbols().newObjectScope(), namespace);
			getSymbols().peek().getNamespaceDefinitions().storeNamespace(namespace, ns);
			//putGlobal(namespace, ns);
		}
		return ns;
	}

	/**
	 * Attempts to lookup a {@link LeoNamespace}.
	 * @param namespace
	 * @return the {@link LeoNamespace} object, or null if not found
	 */
	public LeoNamespace getNamespace(String namespace) {
		return this.symbols.lookupNamespace(namespace);
	}

	/**
	 * @return the global namespace
	 */
	public LeoNamespace getGlobalNamespace() {
		return this.global;
	}


	/**
	 * Gets a {@link LeoObject} by reference from a specific {@link Scope}.
	 * @param scope
	 * @param reference
	 * @return the {@link LeoObject}, or null if not found
	 */
	public LeoObject get(Scope scope, String reference) {
		return scope.getObject(reference);
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
		return function.call(this.vm.get(), arg1);
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
		return function.call(this.vm.get(), arg1, arg2);
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
		return function.call(this.vm.get(), arg1, arg2, arg3);
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
		return function.call(this.vm.get(), arg1, arg2, arg3, arg4);
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
		return function.call(this.vm.get(), arg1, arg2, arg3, arg4, arg5);
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
		return function.call(this.vm.get(), args);
	}

	/**
	 * Executes the function
	 *
	 * @param function
	 * @return an object of the resulting execution (always returns an object)
	 * @throws LeolaRuntimeException
	 */
	public LeoObject execute(LeoObject function) throws LeolaRuntimeException {
		return function.call(this.vm.get());
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
		boolean isCompiled = file.getName().endsWith(LEOLA_COMPILED_EXT);
		if(isCompiled) {
			BufferedInputStream iStream = new BufferedInputStream(new FileInputStream(file));
			DataInput in = new DataInputStream(iStream);


			Bytecode bytecode = Bytecode.read(ns, getSymbols(), in);
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
		BytecodeGenerator gen = new BytecodeGenerator(this, this.symbols);
		program.visit(gen);
		AsmEmitter asm = gen.getAsm();
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

	private static final String SOURCE_LINE_FORMAT = "%03d %s";
	private boolean printSource = false;

    /**
     * Listener for source messages.
     */
    private class SourceMessageListener implements SourceLineListener
    {
        /**
         * Called by the source whenever it produces a message.
         * @param message the message.
         */
        public void onEvent(leola.frontend.events.SourceLineEvent event)
        {
        	if ( printSource ) {
	        	System.out.println(String.format(SOURCE_LINE_FORMAT,
	                    event.getLineNumber(), event.getLine()));
        	}
        }
    }

    private static final String PARSER_SUMMARY_FORMAT =
        "\n%,20d source lines." +
        "\n%,20d syntax errors." +
        "\n%,20.2f seconds total parsing time.\n";

    /**
     * Listener for parser messages.
     */
    private class ParserMessageListener
    	implements SyntaxErrorListener
    			  , ParserSummaryListener
    {
    	/* (non-Javadoc)
    	 * @see leola.frontend.events.ParserSummaryListener#onEvent(leola.frontend.events.ParserSummaryEvent)
    	 */
    	public void onEvent(ParserSummaryEvent evnt) {
    		if ( printSource ) {
	            System.out.printf(PARSER_SUMMARY_FORMAT,
	                    evnt.getTotalLines(), evnt.getErrorCount(),
	                    evnt.getElapsedTime());
    		}
    	}

    	/* (non-Javadoc)
    	 * @see leola.frontend.events.SyntaxErrorListener#onEvent(leola.frontend.events.SyntaxErrorEvent)
    	 */
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
                flagBuffer.append(" [at line: ").append(lineNumber).append(" \"").append(tokenText)
                    .append("\"]");
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
        public void errorToken(Token token, Parser parser, LeolaErrorCode errorCode) {
        	eventDispatcher.sendNow(new SyntaxErrorEvent(this, parser.getSource(), token, errorCode.toString()));
            throw new ParseException(errorCode,
                token.getText() + " errored because of : " + errorCode + " at line: " + token.getLineNumber() + " at " + token.getPosition());
        }


        public void onException(Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}

