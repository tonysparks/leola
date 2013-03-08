/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.lang;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import leola.vm.Leola;
import leola.vm.lib.LeolaIgnore;
import leola.vm.lib.LeolaLibrary;
import leola.vm.lib.LeolaMethod;
import leola.vm.types.LeoArray;
import leola.vm.types.LeoDouble;
import leola.vm.types.LeoInteger;
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
	public void init(Leola runtime, LeoNamespace namespace) throws Exception {
		this.runtime = runtime;			
		runtime.putIntoNamespace(this, namespace);
	}

		
	/**
	 * Includes either a Leola script file or Jar file.
	 * 
	 * @param lib
	 * @throws Exception
	 */
	public final void include(String lib, String namespace) throws Exception {
		runtime.getResourceLoader().include(lib, namespace);
	}
	
	/**
	 * Loads a {@link LeolaLibrary} file.
	 * 
	 * @param lib
	 * @throws Exception
	 */
	public final void require(String lib, String namespace) throws Exception {
		runtime.getResourceLoader().require(lib, namespace);
	}
	
	/**
	 * Reloads a library
	 * @param lib
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
		runtime.loadLibrary(Class.forName(lib));
	}

	/**
	 * Loads a class
	 *
	 * @param interpreter
	 * @param className
	 * @throws Exception
	 */
	@LeolaMethod(alias="import")
	public final void __import(String className) throws Exception {
		runtime.loadStatics(Class.forName(className));
	}

		
	/**
	 * Evaluates the expression.
	 *
	 * @param leola
	 * @param expr
	 * @return
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
	 * @return
	 */
	public final String readln() {
		String result = null;
		Scanner s = new Scanner(java.lang.System.in);		
		if ( s.hasNextLine() ) {
			result = s.nextLine();
		}

		return result;
	}

	public final void printf(Object x, LeoObject args) {
		if( args.isArray() ) {
			LeoArray array = args.as();
			
			int len = array.size();
			Object[] params = new Object[len];						
			for(int i = 0; i < len; i++) {
				params[i] = array.get(i).getValue();
			}
			
			System.out.printf(x.toString(), params);
		}
		else {
			System.out.printf(x.toString(), args.getValue());
		}
	}
	
	/**
	 * Prints to the console with a new line
	 *
	 * @param x
	 */
	public final void println(Object x) {
		java.lang.System.out.println(x);
	}

	/**
	 * Prints to the console.
	 *
	 * @param x
	 */
	public final void print(Object x) {
		java.lang.System.out.print(x);
	}

	/**
	 * Transforms the supplied object into a number
	 * @param x
	 * @return
	 */
	public final double toNumber(Object x) {
		String str = x.toString();
		return Double.parseDouble(str);
	}

	/**
	 * Transforms the supplied object into a string
	 * @param x
	 * @return
	 */
	public final String toString(Object x) {
		return x.toString();
	}
	
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
	 * @param optional fill function
	 * @return
	 */
	public final LeoArray newArray(int size, LeoObject func) {		
		LeoArray result = new LeoArray(size);
		if( func != null ) {
			for(int i = 0; i < size; i++ ) {			
				result.add(this.runtime.execute(func, LeoInteger.valueOf(i)));
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
	 * @return
	 */
	public final LeoNativeClass newThread(final LeoObject function) {		
		Thread thread = new Thread(new Runnable() {
			public void run() {
				try {
					runtime.execute(function);
				}
				catch (Throwable t) {
					t.printStackTrace();
				}
			}
		});

		return new LeoNativeClass(thread);
	}
	
	public final LeoNativeClass newScheduler(int poolSize) {
		return new LeoNativeClass(new Scheduler(runtime, poolSize));
	}

	/**
	 * 
	 * @author chq-tonys
	 *
	 */
	public static class Scheduler {
		private ScheduledExecutorService executorService;
		private Leola runtime;
		Scheduler(Leola runtime, int poolSize) {
			this.executorService = Executors.newScheduledThreadPool(poolSize);
			this.runtime = runtime;
		}
		
		public void shutdown() {			
			this.executorService.shutdownNow();
		}
		
		public void schedule(long timeMsec, final LeoObject function) {
			this.executorService.schedule(new Callable<Void>() {

				public Void call() throws Exception {
					try {
						runtime.execute(function);
					}
					catch (Throwable t) {
						t.printStackTrace();
					}
					
					return null;
				}
			
				
			}, timeMsec, TimeUnit.MILLISECONDS);
		}
		
		public void repeat(long delay, final LeoObject function) {
			this.executorService.scheduleWithFixedDelay(new Runnable() {

				public void run() {
					try {
						runtime.execute(function);
					}
					catch (Throwable t) {
						t.printStackTrace();
					}
				}
			
				
			}, delay, delay, TimeUnit.MILLISECONDS);			
		}
		
	}	
}

