/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.vm.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import leola.frontend.EvalException;
import leola.vm.Leola;
import leola.vm.exceptions.LeolaRuntimeException;
import leola.vm.lib.LeolaLibrary;


/**
 * Loads external resources based on the current working directory and the include path of the {@link Leola} runtime.
 * 
 * @author Tony
 *
 */
public class ResourceLoader {

	private static final Set<String> NATIVE_LIB = new HashSet<String>();
	static {
		NATIVE_LIB.add("so");
		NATIVE_LIB.add("a");
		NATIVE_LIB.add("dll");
	}
	
	/**
	 * Cache of loaded resources
	 */
	private Set<String> cache;
	
	/**
	 * The runtime
	 */
	private Leola runtime;
	
	/**
	 * @param runtime
	 */
	public ResourceLoader(Leola runtime) {
		this.runtime = runtime;
		this.cache = Collections.synchronizedSet(new HashSet<String>()); 
	}
	
	/**
	 * Clears all cached resources (allowing them
	 * to be reloaded)
	 */
	public void clearCache() {
		this.cache.clear();
	}
	
	/**
	 * Removes the supplied resource from the cache (allowing it
	 * to be reloaded)
	 * 
	 * @param resource
	 */
	public void removeFromCache(String resource) {
		this.cache.remove(resource);
	}

	
	/**
	 * Loads the resource.
	 * 
	 * @param runtime
	 * @param interpreter
	 * @param resource
	 * @param loadLibrary
	 * @throws EvalException
	 */
	private void loadResource(Leola runtime, String resource, boolean loadLibrary, boolean isFirstLevel, String namespace) throws LeolaRuntimeException {
		try {
			if ( ! this.cache.contains(resource + ":" + namespace) ) {
				
				/** first try loading this as a LeolaLibrary */
				if(!tryLoadingLibrary(runtime, resource, namespace)) {													
					File libFile = resolveName(runtime, resource, true);				
					String ext = getExtension(libFile.getName());
					
					/* this is directory - so load any libs if possible */
					if (libFile.isDirectory() && isFirstLevel) {
						String[] jarFiles = libFile.list(new FilenameFilter() {						
							public boolean accept(File dir, String name) {						
								return name.toLowerCase().endsWith("jar");
							}
						});
						
						for(String file : jarFiles) {
							loadResource(runtime, libFile.getAbsolutePath() + "/" + file, loadLibrary, false, namespace);
						}
						
					}
					/* this is a leola script file */
					else if ( ext.endsWith("leola") || ext.endsWith("leolac") ) {
						runtime.eval(libFile, namespace);
					}
					else {
						
						/* load the jar file */
						if (ext.endsWith("jar") ) {
							Classpath.addFile(libFile);
							
							if ( loadLibrary ) { 
								loadLibrary(runtime, libFile, namespace);
							}
						}
						
						else if (NATIVE_LIB.contains(ext) || ext.equals("") ) {
							System.loadLibrary(resource.replace(ext, ""));
						}
					}	
				}
				/* if this was successfully loaded, add it to the cache */
				this.cache.add(resource + ":" + namespace);
			}
		}
		catch(Exception e) {
			throw new LeolaRuntimeException(e);
		}
	}
	
	/**
	 * Includes the file, it does not look for a {@link LeolaLibrary}.
	 * 
	 * <p>Uses the global namespace
	 * 
	 * @param resource
	 * @throws Exception
	 */
	public void include(String resource) throws LeolaRuntimeException {
		include(this.runtime, resource, Leola.GLOBAL_SCOPE_NAME);
	}

	/**
	 * Includes the file, it does not look for a {@link LeolaLibrary}.
	 * 
	 * @param resource
	 * @param namespace
	 * @throws Exception
	 */
	public void include(String resource, String namespace) throws LeolaRuntimeException {
		include(this.runtime, resource, namespace);
	}
	
	/**
	 * Includes the file, it does not look for a {@link LeolaLibrary}.
	 * 
	 * @param resource
	 * @param namespace
	 * @throws Exception
	 */
	public void include(Leola runtime, String resource, String namespace) throws LeolaRuntimeException {
		loadResource(runtime, resource, false, true, namespace);
	}

	/**
	 * Loads either a Script, Jar file (if jar it looks for a {@link LeolaLibrary}).  It
	 * first looks on the application path, then on the include path.
	 * 
	 * <p>Uses the global namespace
	 * 
	 * @param resource
	 * @throws IOException
	 */
	public void require(String resource) throws LeolaRuntimeException {
		require(this.runtime, resource, Leola.GLOBAL_SCOPE_NAME);
	}
	
	/**
	 * Loads either a Script, Jar file (if jar it looks for a {@link LeolaLibrary}).  It
	 * first looks on the application path, then on the include path.
	 * 
	 * @param resource
	 * @param namespace to use
	 * @throws IOException
	 */
	public void require(String resource, String namespace) throws LeolaRuntimeException {
		require(this.runtime, resource, namespace);
	}
	
	/**
	 * Loads either a Script, Jar file (if jar it looks for a {@link LeolaLibrary}).  It
	 * first looks on the application path, then on the include path.
	 * 
	 * @param resource
	 * @throws IOException
	 */
	public void require(Leola runtime, String resource, String namespace) throws LeolaRuntimeException {
		loadResource(runtime, resource, true, true, namespace);
	}
	
	/**
	 * Attempts to resolve the name
	 * @param lib
	 * @return
	 * @throws Exception
	 */
	private File resolveName(Leola runtime, String lib, boolean withExt) throws Exception {
		File libFile = new File(lib);
				
		if ( ! libFile.isAbsolute() ) {
			File wd = runtime.getWorkingDirectory();
			List<File> dirs = runtime.getIncludePath();
			
			int i = 0;			
			for( libFile = new File( wd.getAbsolutePath() + "/" + lib);
				! libFile.exists() && i < dirs.size();
				libFile = new File( dirs.get(i++).getAbsolutePath() + "/" + lib) ) {				
			}
			
			if ( ! libFile.exists() /*|| ! libFile.isFile()*/ ) {
				if(withExt) {
					// prefer the leolac version of the file
					libFile = resolveName(runtime, lib + ".leolac", false);
					
					if ( ! libFile.exists() || ! libFile.isFile()) {
						libFile = resolveName(runtime, lib + ".leola", false);
					}
				}
		
				if ( withExt && ( !libFile.exists() /*|| !libFile.isFile() */) ) {
					throw new IOException(lib + " was not found!");
				}
			}
		}
						
		return libFile;
	}
	
	/**
	 * loads the jar and attempts to load any {@link LeolaLibrary}s.
	 * 
	 * @param runtime
	 * @param file
	 * @throws Exception
	 */
	private void loadLibrary(Leola runtime, File file, String namespace) throws Exception {		
	    JarInputStream jarFile = new JarInputStream(new FileInputStream (file));
	    JarEntry jarEntry;

	    boolean loaded = false;
	    while(true) {
	    	jarEntry = jarFile.getNextJarEntry();
	      
	    	if(jarEntry == null) {
	    		break;
	    	}
	    	
	    	String className = jarEntry.getName().replaceAll("/", "\\.").replaceAll("\\.class", "");
	    	if ( className.endsWith("LeolaLibrary")) {
	    		loaded = tryLoadingLibrary(runtime, className, namespace) || loaded;
	    	}
	    }
	    
//	    if (! loaded ) {
//	    	throw new IOException("No *LeolaLibrary found in: " + file.getName());
//	    }
	}
	
	/**
	 * Attempts to load the {@link LeolaLibrary}
	 * @param runtime
	 * @param className
	 * @return
	 */
	private boolean tryLoadingLibrary(Leola runtime, String className, String namespace) throws Exception {
		boolean loaded = false;
		
		Class<?> lib = null;
		try {
			lib = Class.forName(className);
			if ( ClassUtil.doesImplement(lib, LeolaLibrary.class) ) {
				runtime.loadLibrary(lib, namespace);
				loaded = true;
			}
		}
		catch(Exception e) {			
		}
				
		return loaded;
	}
	
	
    private String getExtension(String filename) {
        if (filename == null) {
            return null;
        }
        int index = filename.lastIndexOf(".");
        if (index == -1) {
            return "";
        } else {
            return filename.substring(index + 1);
        }
    }
        
}

