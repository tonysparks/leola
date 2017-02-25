/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.lang;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import leola.vm.Leola;
import leola.vm.exceptions.LeolaRuntimeException;
import leola.vm.lib.LeolaIgnore;
import leola.vm.lib.LeolaLibrary;
import leola.vm.lib.LeolaMethod;
import leola.vm.lib.LeolaMethodVarargs;
import leola.vm.types.LeoArray;
import leola.vm.types.LeoDouble;
import leola.vm.types.LeoInteger;
import leola.vm.types.LeoMap;
import leola.vm.types.LeoNamespace;
import leola.vm.types.LeoNativeClass;
import leola.vm.types.LeoNull;
import leola.vm.types.LeoObject;
import leola.vm.types.LeoObject.LeoType;
import leola.vm.util.Classpath;

/**
 * The system core functions
 *
 * @author Tony
 *
 */
public class LangLeolaLibrary implements LeolaLibrary {

    /**
     * The runtime
     */
    private Leola runtime;
        
    /* (non-Javadoc)
     * @see leola.frontend.LeolaLibrary#init(leola.frontend.Leola)
     */
    @LeolaIgnore
    public void init(Leola runtime, LeoNamespace namespace) throws LeolaRuntimeException {
        this.runtime = runtime;            
        runtime.putIntoNamespace(this, namespace);
    }

        
    /**
     * Includes either a Leola script file or Jar file.
     * 
     * @param lib
     * @param namespace
     * @throws Exception
     */
    public final void include(String lib, String namespace) throws Exception {
        runtime.getResourceLoader().include(lib, namespace);
    }
    
    /**
     * Loads a {@link LeolaLibrary} file.
     * 
     * @param lib
     * @param namespace
     * @throws Exception
     */
    public final void require(String lib, String namespace) throws Exception {
        runtime.getResourceLoader().require(lib, namespace);
    }
    
    /**
     * Reloads a library
     * @param lib
     * @param namespace
     * @throws Exception
     */
    public void reload(String lib, String namespace) throws Exception {
        runtime.getResourceLoader().removeFromCache(lib);
        include(lib, namespace);
    }
    
    /**
     * Dynamically loads a jar file
     * @param jarFile
     * @throws Exception
     */
    public final static void loadJar(String jarFile) throws IOException {        
        Classpath.addFile(jarFile);
    }
    
    /**
     * Dynamically loads all the jar files located in the directory
     * 
     * @param directory
     * @throws IOException
     */
    public final static void loadJars(String directory) throws IOException {                
        File dir = new File(directory);
        if ( dir.isDirectory() ) {
            File[] jars = dir.listFiles(new FileFilter() {                
                public boolean accept(File pathname) {
                    return pathname.isFile() && pathname.getName().endsWith(".jar");
                }
            });
            
            for(File jar : jars) {
                Classpath.addFile(jar);
            }
        }
    }
    
    /**
     * Loads the URL to the classpath.
     * 
     * @param url
     * @throws IOException
     */
    public final static void loadURL(String url) throws IOException {        
        File dir = new File(url);
        Classpath.addURL(dir.toURI().toURL());
    }

    /**
     * Loads a library
     *
     * @param lib
     */
    public final void loadLibrary(String lib) throws Exception {
        runtime.errorIfSandboxed();
        runtime.loadLibrary(Class.forName(lib));
    }

    /**
     * Loads a class
     *
     * @param className
     * @param namespace
     * @throws Exception
     */
    @LeolaMethod(alias="import")
    public final void __import(String className, String namespace) throws Exception {
        runtime.errorIfSandboxed();
        if(namespace != null && !namespace.equals("")) {
            runtime.loadStatics(runtime.getOrCreateNamespace(namespace), Class.forName(className));
        }
        else {
            runtime.loadStatics(Class.forName(className));
        }
    }

        
    /**
     * Evaluates the expression.
     *
     * @param expr
     * @return the result of evaluation
     * @throws Exception
     */
    public final LeoObject eval(String expr) throws Exception {
        LeoObject result = runtime.eval(expr);

        return result;
    }
    
    /**
     * Reads a line from sys in
     *
     * TODO - move to io
     * @return the line read from system in
     */
    @SuppressWarnings("resource")
    public final String readln() {
        String result = null;        
        Scanner s = new Scanner(java.lang.System.in);        
        try {
            if ( s.hasNextLine() ) {
                result = s.nextLine();
            }
        }
        finally {
        //    s.close(); DO NOT close because this will close SysIn
        }

        return result;
    }
    
    private void printf(PrintStream out, Object x, LeoObject ... args) {
        if(args!=null) {
            int len = args.length;
            Object[] params = new Object[len];                      
            for(int i = 0; i < len; i++) {
                params[i] = args[i].getValue();
            }
            out.printf(x.toString(), params);
        }
        else {
            out.printf(x.toString());
        }
    }
    
    /**
     * Print format to system error
     * 
     * @param x
     * @param args
     */
    @LeolaMethodVarargs
    public final void eprintf(Object x, LeoObject ...args) {
        printf(System.err, x, args);
    }

    /**
     * Print format
     * 
     * @param x the string to be formatted 
     * @param args the format arguments
     */
    @LeolaMethodVarargs
    public final void printf(Object x, LeoObject ... args) {
        printf(System.out, x, args);
    }
    
    /**
     * Prints to system error with a new line
     *
     * @param x
     */
    public final void eprintln(Object x) {
        System.err.println(x);
    }
    
    /**
     * Prints to system out with a new line
     *
     * @param x
     */
    public final void println(Object x) {
        java.lang.System.out.println(x);
    }
    
    /**
     * Prints to system error.
     * 
     * @param x
     */
    public final void eprint(Object x) {
        System.err.print(x);
    }

    /**
     * Prints to system out.
     *
     * @param x
     */
    public final void print(Object x) {
        java.lang.System.out.print(x);
    }

    /**
     * Transforms the supplied object into a number
     * @param x
     * @return the double
     */
    public final double toNumber(Object x) {
        String str = x.toString();
        return Double.parseDouble(str);
    }

    /**
     * Transforms the supplied object into a string
     * @param x
     * @return the string
     */
    public final String toString(Object x) {
        return x.toString();
    }
    
    
    /**
     * Converts the string into byte[]
     * 
     * @param str
     * @return the byte[]
     */
    public final LeoNativeClass toBytes(String str) {
        return new LeoNativeClass(byte[].class, str.getBytes());
    }

    /**
     * @param x
     * @return an integer representation
     */
    public final int toInt(Object x) {
        Double d = toNumber(x);
        return d.intValue();
    }
    
    /**
     * @param x
     * @return a long representation
     */
    public final long toLong(Object x) {
        Number number = toNumber(x);
        return number.longValue();
    }
    
    /**
     * @param x
     * @return a double representation
     */
    public final double toDouble(Object x) {
        return toNumber(x);
    }
    
    /**
     * Synchronizes the supplied {@link LeoObject} function
     * 
     * @param function
     * @return the result of invoking the function
     */
    @LeolaMethod(alias="synchronized")
    public final LeoObject _synchronized(LeoObject function) {
        synchronized (function) {
            return function.xcall();
        }
    }
    
    /**
     * Converts the {@link Collection} into a {@link LeoArray}
     * 
     * @param list
     * @return the {@link LeoArray}
     */
    public final LeoArray toArray(Collection<Object> list) {
        return LeoArray.toArray(list);
    }
    
    
    /**
     * Converts the java {@link Map} into a {@link LeoMap} object.
     * 
     * @param map
     * @return the {@link LeoMap}
     */
    public final LeoMap toMap(Map<Object, Object> map) {
        return LeoMap.toMap(map);
    }
    
    /**
     * Converts the object to a character
     * 
     * @param x
     * @return the character
     */
    public final char toChar(Object x) {
        if ( x instanceof String) {
            return ((String) x).charAt(0);
        }
        else if ( x instanceof LeoObject ) {
            LeoObject obj = (LeoObject)x;
            if ( obj.isOfType(LeoType.STRING)) {
                return obj.toString().charAt(0);
            }
            if ( obj.isOfType(LeoType.INTEGER)) {
                return ((LeoInteger)obj).asChar();
            }
            if ( obj.isOfType(LeoType.REAL)) {
                return ((LeoDouble)obj).asChar();
            }
        }

        throw new IllegalArgumentException("Not a valid char: " + x);
    }



    /**
     * Constructs a new Array
     * @param size
     * @param func optional fill function
     * @return the new array
     */
    public final LeoArray newArray(int size, LeoObject func) {        
        LeoArray result = new LeoArray(size);
        if( func != null ) {
            for(int i = 0; i < size; i++ ) {            
                result.add(func.xcall(LeoInteger.valueOf(i)));
            }
        }
        else {
            for(int i = 0; i < size; i++ ) {            
                result.add(LeoNull.LEONULL);
            }
        }
        return result;        
    }

    
    /**
     * Returns a new thread with the supplied {@link LeoObject} serving
     * as the thread runnable.
     *
     * @param function
     * @return the thread class
     */
    public final LeoNativeClass newThread(final LeoObject function, String name, Boolean daemon) {        
        Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    function.xcall();
                }
                catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }, name!=null?name:"leola-user-thread");

        if(daemon != null) {
            thread.setDaemon(daemon);
        }
        
        return new LeoNativeClass(thread);
    }
    
    
    /**
     * Creates a new {@link Scheduler}
     * 
     * @param poolSize
     * @return the {@link Scheduler}
     */
    public final LeoNativeClass newScheduler(int poolSize) {
        return new LeoNativeClass(new Scheduler(poolSize));
    }

    /**
     * 
     * @author Tony
     *
     */
    public static class Scheduler {
        private ScheduledExecutorService executorService;
        Scheduler(int poolSize) {
            this.executorService = Executors.newScheduledThreadPool(poolSize);
        }
        
        /**
         * Terminates the scheduler
         */
        public void shutdown() {            
            this.executorService.shutdownNow();
        }
        
        /**
         * Schedules the function
         * 
         * @param timeMsec
         * @param function
         */
        public void schedule(long timeMsec, final LeoObject function) {
            this.executorService.schedule(new Callable<Void>() {

                public Void call() throws Exception {
                    try {
                        function.xcall();
                    }
                    catch (Throwable t) {
                        t.printStackTrace();
                    }
                    
                    return null;
                }
            
                
            }, timeMsec, TimeUnit.MILLISECONDS);
        }
        
        
        /**
         * Repeatedly executes the function
         * 
         * @param delay
         * @param function
         */
        public void repeat(long delay, final LeoObject function) {
            this.executorService.scheduleWithFixedDelay(new Runnable() {

                public void run() {
                    try {
                        function.xcall();
                    }
                    catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            
                
            }, delay, delay, TimeUnit.MILLISECONDS);            
        }
        
    }    
}

