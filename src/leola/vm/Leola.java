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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import leola.ast.ASTNode;
import leola.frontend.ParseException;
import leola.frontend.Parser;
import leola.frontend.Scanner;
import leola.frontend.Source;
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
import leola.vm.Args.ArgsBuilder;
import leola.vm.Scope.ScopeType;
import leola.vm.compiler.Bytecode;
import leola.vm.compiler.Compiler;
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

    public static final String VERSION = "0.10.3";
    
    /**
     * Usage
     */
    private static final String USAGE =
        "Leola v" + VERSION + "\n\n" +
        "<USAGE> leola " + Args.getOptions() + " <file> [script args] \n" +
        Args.getOptionsWithDescription();

    
    private static final String LEOLA_COMPILED_EXT = "leolac";
    private static final String LEOLA_EXT = "leola";

    public static final String GLOBAL_SCOPE_NAME = "$G";

    /**
     * Create new instances of a {@link Leola} runtime 
     * via the {@link ArgsBuilder}
     * 
     * <pre>
     *   Leola runtime = Leola.builder()
     *                        .setAllowThreadLocals(false)
     *                        .setIsDebugMode(true)
     *                        .setBarebones(true)
     *                        .setSandboxed(false) 
     *                        .newRuntime();
     * 
     * </pre>
     * 
     * @return the {@link ArgsBuilder} to build a {@link Leola} runtime
     */
    public static ArgsBuilder builder() {
        return Args.builder();
    }
    
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
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        if(args.length == 0) {
            System.out.println(USAGE);
        }
        else {

            Args pargs = Args.parse(args);
            try {
                if(pargs.executeStatement()) {
                    executeStatement(pargs);
                }
                else if(pargs.isRepl()) {
                    executeRepl(pargs);
                }
                else {
                    executeScript(pargs);
                }
            }
            catch(ParseException e) {
                System.err.println(e.getMessage());
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
     * Executes the REPL
     * 
     * @param args
     * @throws Exception
     */
    private static void executeRepl(Args args) throws Exception {
        Repl repl = new Repl(new Leola(args));
        repl.execute();
    }
    
    /**
     * Finds the script file that was passed by the command line 
     * 
     * @param pargs
     * @return the {@link File}
     */
    private static File findScriptFile(Args pargs) {
        String fileName = pargs.getFileName();
        
        File file = new File(fileName);
        if(!file.exists()) {
            file = new File(System.getProperty("user.dir"), fileName);
            
            if(!file.exists()) {
               for(File dir : pargs.getIncludeDirectories()) {
                   file = new File(dir, fileName);
                   if(file.exists()) {
                       return file;
                   }
               }
            }
        }
        
        if(!file.exists()) {
            System.out.println("Unable to find '" + fileName + "'");
            System.exit(1);
        }
        
        pargs.getIncludeDirectories().add(file.getParentFile());
        
        return file;
    }
    
    /**
     * Execute or compile the supplied script
     * 
     * @param pargs
     * @throws Exception
     */
    private static void executeScript(Args pargs) throws Exception {       
        File scriptFile = findScriptFile(pargs);
        
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
     * @throws Exception
     */
    public Leola() throws LeolaRuntimeException {
        this(new Args());
    }

    /**
     * @param args
     * @throws LeolaRuntimeException
     */
    public Leola(Args args) throws LeolaRuntimeException {
        this.args = args;
        
        setIncludePath(args.getIncludeDirectories());
        this.resourceLoader = new ResourceLoader(this);
        
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
        
        Scope globalScope = new Scope(ScopeType.Namespace, null);
        this.global = new LeoNamespace(globalScope, LeoString.valueOf(GLOBAL_SCOPE_NAME));
        reset();
    }
    
    
    /**
     * <b>Use with extreme caution!</b>
     * 
     * <p>
     * This will clear out all allocated objects, effectively resetting the {@link Leola} to its initial state.
     */
    public void reset() {
        this.resourceLoader.clearCache();
        this.global.getScope().clear();
        this.global.getNamespaceDefinitions().storeNamespace(this.global);
        
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
            if (!args.isBarebones() && !isSandboxed) {
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
     * Adds a path to the paths to check for when include/require look
     * ups.
     * 
     * @param includeDirectory
     */
    public void addIncludePath(File includeDirectory) {
        if(this.includeDirectories==null) {
            this.includeDirectories = new ArrayList<File>();
        }
        this.includeDirectories.add(includeDirectory);
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
     * If this {@link Leola} instance was started from the command-line or was supplied
     * a script via the {@link Args#getFileName()}, this will return said script as a {@link File}.
     * 
     * @return the execution script that was used for this {@link Leola} instance.  This may return
     * null, if no script was used.
     */
    public File getExecutionScript() {
        String executionScript = this.args.getFileName();
        if(executionScript!=null) {
            return new File(executionScript);
        }
        
        return null;
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
     * @throws LeolaRuntimeException
     */
    public void loadLibrary(LeolaLibrary lib) throws LeolaRuntimeException {
        checkIfSandboxed(lib.getClass());
        
        lib.init(this, this.global);
    }

    /**
     * Loads a {@link LeolaLibrary} into the supplied namespace
     * 
     * @param lib
     * @param namespace
     * @throws LeolaRuntimeException
     */
    public void loadLibrary(LeolaLibrary lib, String namespace) throws LeolaRuntimeException {
        checkIfSandboxed(lib.getClass());
        
        LeoNamespace ns = getOrCreateNamespace(namespace);
        lib.init(this, ns);
    }

    /**
     * Loads a {@link LeolaLibrary}.
     *
     * @param lib
     * @throws LeolaRuntimeException
     */
    public void loadLibrary(LeolaLibrary lib, LeoNamespace namespace) throws LeolaRuntimeException {
        checkIfSandboxed(lib.getClass());
        
        lib.init(this, namespace);
    }

    /**
     * Places the natives into the supplied namespace
     *
     * @param lib
     * @param namespace
     * @throws LeolaRuntimeException
     */
    public LeoNamespace putIntoNamespace(Object lib, String namespace) throws LeolaRuntimeException {
        Scope nsScope = null;

        LeoNamespace ns = getNamespace(namespace);
        if(ns != null) {
            nsScope = ns.getScope();
        }
        else {
            nsScope = new Scope(ScopeType.Namespace, this.global.getScope());
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
     * @throws LeolaRuntimeException
     */
    public LeoNamespace putIntoNamespace(Object lib, LeoNamespace namespace) throws LeolaRuntimeException {
        Scope nsScope = namespace.getScope();
        loadNatives(nsScope, lib);
        return namespace;
    }

    /**
     * Loads a {@link LeolaLibrary}.
     *
     * @param libClass
     * @throws LeolaRuntimeException
     */
    public void loadLibrary(Class<?> libClass, LeoNamespace namespace) throws LeolaRuntimeException {
        checkIfSandboxed(libClass);

        try {
            LeolaLibrary lib = (LeolaLibrary)libClass.newInstance();
            loadLibrary(lib, namespace);
        }
        catch (InstantiationException | IllegalAccessException e) {
            throw new LeolaRuntimeException(e);
        }
        
    }

    /**
     * Loads a {@link LeolaLibrary}.
     *
     * @param libClass
     * @throws LeolaRuntimeException
     */
    public void loadLibrary(Class<?> libClass, String namespace) throws LeolaRuntimeException {
        checkIfSandboxed(libClass);
        try {    
            LeoNamespace ns = getOrCreateNamespace(namespace);
            LeolaLibrary lib = (LeolaLibrary)libClass.newInstance();
            loadLibrary(lib, ns);
        }
        catch (InstantiationException | IllegalAccessException e) {
            throw new LeolaRuntimeException(e);
        }
    }

    /**
     * Loads a {@link LeolaLibrary}.
     *
     * @param libClass
     * @throws LeolaRuntimeException
     */
    public void loadLibrary(Class<?> libClass) throws LeolaRuntimeException {
        checkIfSandboxed(libClass);
        try {    
            LeolaLibrary lib = (LeolaLibrary)libClass.newInstance();
            loadLibrary(lib);
        }
        catch (InstantiationException | IllegalAccessException e) {
            throw new LeolaRuntimeException(e);
        }
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
            ns = new LeoNamespace(new Scope(ScopeType.Namespace, this.global.getScope()), LeoString.valueOf(namespace));
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
        return this.vm.get().execute(callee,callee, code,args).throwIfError();
    }

    /**
     * Executes the supplied {@link Bytecode}
     * @param callee
     * @param code
     * @return an object of the resulting execution (always returns an object)
     * @throws LeolaRuntimeException
     */
    public LeoObject execute(LeoObject callee, Bytecode code) throws LeolaRuntimeException {
        return this.vm.get().execute(callee,callee, code).throwIfError();
    }

    /**
     * Executes the supplied {@link Bytecode}
     * @param code
     * @param args
     * @return an object of the resulting execution (always returns an object)
     * @throws LeolaRuntimeException
     */
    public LeoObject execute(Bytecode code, LeoObject[] args) throws LeolaRuntimeException {
        return this.vm.get().execute(this.global,this.global, code,args).throwIfError();
    }

    /**
     * Executes the supplied {@link Bytecode}
     * @param code
     * @return an object of the resulting execution (always returns an object)
     * @throws LeolaRuntimeException
     */
    public LeoObject execute(Bytecode code) throws LeolaRuntimeException {
        return this.vm.get().execute(this.global, this.global, code).throwIfError();
    }


    

    /**
     * Evaluates the inlined source code.
     * <pre>
     *     leola.eval("val x = 10; println(x);");
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
            try(BufferedInputStream iStream = new BufferedInputStream(new FileInputStream(file))) {
                DataInput in = new DataInputStream(iStream);
    
    
                Bytecode bytecode = Bytecode.read(ns, in);
                bytecode.setSourceFile(file);
    
                result = execute(ns, bytecode);
            }
        }
        else {
            try(Reader reader = new BufferedReader(new FileReader(file))) {
                Bytecode bytecode = compile(reader);
                bytecode.setSourceFile(file);
    
                result = execute(ns, bytecode);
            }
        }

        return result;
    }

    public LeoObject eval(Reader reader) throws Exception {
        return eval(reader, this.global);
    }

    public LeoObject eval(Reader reader, LeoNamespace namespace) throws Exception {
        Bytecode bytecode = compile(reader);
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
        try(InputStream iStream = new BufferedInputStream(new FileInputStream(scriptFile))) {
            Bytecode code = read(iStream);
            if(code != null) {
                code.setSourceFile(scriptFile);
            }
            return code;
        }
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
    public void write(File scriptFile, Bytecode bytecode) throws IOException {
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
     * @throws IOException
     */
    public void write(OutputStream oStream, Bytecode bytecode) throws IOException {

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
        code.setSourceFile(scriptFile);
        return code;
    }
    

    /**
     * Compiles the file.
     *
     * @param reader
     * @return
     * @throws Exception
     */
    public Bytecode compile(Reader reader) throws Exception {
        ASTNode program = generateAST(reader);
        
        Compiler compiler = new Compiler(this);
        return compiler.compile(program);                
    }

    /**
     * Evaluates the file.
     *
     * @param file
     * @throws Exception
     */
    public ASTNode generateAST(File file) throws Exception {
        return generateAST(new BufferedReader(new FileReader(file)));
    }

    /**
     * Reads in the inline source.
     *
     * @param inlineSource
     * @throws Exception
     */
    public ASTNode generateAST(String inlineSource) throws Exception {
        return generateAST(new BufferedReader(new StringReader(inlineSource)));
    }

    /**
     * Evaluate the stream.
     *
     * @param iStream
     * @throws Exception
     */
    public ASTNode generateAST(InputStream iStream) throws Exception {
        return generateAST(new BufferedReader(new InputStreamReader(iStream)));
    }


    /**
     * Generates an Abstract Syntax Tree from the stream.
     *
     * @param reader
     * @return the root node of the AST
     * @throws Exception
     */
    public ASTNode generateAST(Reader reader) throws Exception {
        final Source source = new Source(reader);

        Scanner scanner = new Scanner(source);
        Parser parser = new Parser(scanner);

        ASTNode program = null;
        try {
            program = parser.parse();
        }
        finally {
            source.close();
        }

        return program;
    }

   
}