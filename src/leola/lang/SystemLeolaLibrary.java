/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.lang;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import leola.vm.Leola;
import leola.vm.lib.LeolaIgnore;
import leola.vm.lib.LeolaLibrary;
import leola.vm.types.LeoArray;
import leola.vm.types.LeoNamespace;

/**
 * The system core functions
 *
 * @author Tony
 *
 */
public class SystemLeolaLibrary implements LeolaLibrary {

	/**
	 * The runtime
	 */
	private Leola runtime;
	private Random random;
	
	/* (non-Javadoc)
	 * @see leola.frontend.LeolaLibrary#init(leola.frontend.Leola)
	 */
	@LeolaIgnore
	public void init(Leola runtime, LeoNamespace namespace) throws Exception {		
		this.runtime = runtime;
		this.random = new Random();
		this.runtime.putIntoNamespace(this, namespace);		
	}
			
	public final void setPath(String paths) {
		runtime.setIncludePath(paths);
	}
	
	public final void addPath(String path) throws Exception {
		File filePath = new File(path);
		runtime.getIncludePath().add(filePath);
		
		if(filePath.isFile() && filePath.getName().toLowerCase().endsWith(".jar")) {
			LangLeolaLibrary.loadJar(path);	
		}
		else {
			LangLeolaLibrary.loadJars(path);
		}
	}
	
	public final String getPath() {
		String result = "";
		List<File> lpath = runtime.getIncludePath();
		for(File file : lpath) {
			result += file.getAbsolutePath() + ";";
		}
		
		return result;
	}
	
	/**
	 * Sets the java.library.path
	 * @param path
	 */
	public final void setLibraryPath(String path) throws Exception {
		System.setProperty( "java.library.path", path );
		 
		Field fieldSysPath = ClassLoader.class.getDeclaredField( "usr_paths" );
		fieldSysPath.setAccessible( true );
		fieldSysPath.set( null, new String[] {path} );
	}
	
	public final String javapath() {
		StringBuilder sb=new StringBuilder(1024);
        ClassLoader sysClassLoader = ClassLoader.getSystemClassLoader();

        //Get the URLs
        URL[] urls = ((URLClassLoader)sysClassLoader).getURLs();

        for(int i=0; i< urls.length; i++) {
            sb.append(";").append(urls[i].getFile());
        } 
        
        return sb.toString();
	}
	
	public final int random(int max) {
		return this.random.nextInt(max);
	}
	
	public final long memused() {
		Runtime rt = Runtime.getRuntime();
		return rt.totalMemory() - rt.freeMemory();
	}
	
		
	public final void gc() {
		Runtime.getRuntime().gc();
	}
	
	public final Process execute(String application, LeoArray args, String workingdir) throws Exception {
		String[] cmds = null;
		if ( args!=null) {
			cmds = new String[args.size() + 1];
			for(int i = 0; i < args.size(); i++) {
				cmds[i+1] = args.get(i).toString();
			}						
		}
		else {
			cmds = new String[1];
		}
		
		cmds[0] = application;
		
		Process process = Runtime.getRuntime().exec(cmds, null, new File(workingdir));
		return process;
	}
	
	public final void pipe(InputStream iStream) throws Exception {
		Scanner scanner = new Scanner(iStream);
		try {
			while(scanner.hasNext()) {
				System.out.println(scanner.nextLine());
			}
		}
		finally {
			scanner.close();
		}
	}
	
	/**
	 * Sleeps
	 *
	 * @param time
	 * @throws Exception
	 */
	public final void sleep(long time) throws Exception {
		Thread.sleep(time);
	}

	/**
	 * Exits the JVM
	 *
	 * @param code
	 * @throws Exception
	 */
	public final void exit(int code) throws Exception {
		java.lang.System.exit(code);
	}
}

