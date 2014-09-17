/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.lang.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import leola.vm.Leola;
import leola.vm.lib.LeolaIgnore;
import leola.vm.lib.LeolaLibrary;
import leola.vm.types.LeoNamespace;


/**
 * The Input/Output Library
 * 
 * @author Tony
 *
 */
public class IOLeolaLibrary implements LeolaLibrary {

	private Leola runtime;

	
	/* (non-Javadoc)
	 * @see leola.frontend.LeolaLibrary#init(leola.frontend.Leola)
	 */
	@LeolaIgnore
	public void init(Leola leola, LeoNamespace namespace) throws Exception {		
		this.runtime = leola;
		this.runtime.putIntoNamespace(this, namespace);
	}

	public boolean isDirectory(String filename) {
		return new File(filename).isDirectory();
	}
	
	public boolean isFile(String filename) {
		return new File(filename).isFile();
	}
	
	public boolean rename(String filename, String newname) {
		return new File(filename).renameTo(new File(newname));
	}
	
	/**
	 * List the contents of the directory
	 * @param dir
	 * @return a list of all the files in the supplied directory
	 */
	public String[] list(String dir) {
		return new File(dir).list();
	}
	
	/**
	 * List the contents of the directory (with the supplied path included
	 * in the name).
	 * @param dir
	 * @return
	 */
	public String[] listFiles(String dir) {
		File[] files = new File(dir).listFiles();
		String[] contents = new String[files.length];
		for(int i = 0; i < contents.length; i++) {
			contents[i] = files[i].getAbsolutePath();
		}
		
		return contents;
	}
	
	/**
	 * The current working directory
	 * @return the current working directory
	 */
	public static String pwd() {
		return System.getProperty("user.dir");
	}
	
	public void $includePath(String newPath) {		
		String[] paths = newPath.split(";");
		List<File> newIncludePath = new ArrayList<File>(paths.length);
		for(String path : paths) {
			newIncludePath.add(new File(path));
		}			
		
		this.runtime.setIncludePath(newIncludePath);
	}
	
	public static final boolean delete(String filename) throws Exception {
		File f = new File(filename);
		return ( f.delete() );
	}
	
	public static final void copy(String filename, String destination) throws Exception {
		copy(filename, destination, false);
	}
	
	public static final void xcopy(String filename, String destination) throws Exception {
		copy(filename, destination, true);
	}
	
	private static final void copy(String filename, String destination, boolean force) throws Exception {
		File src = new File(filename);
		if ( ! src.exists() ) {
			throw new IOException(filename + " doesn't exist!");
		}
		
		File dst = new File(destination);
		if ( dst.exists() ) {
			if ( force ) {
				if ( dst.delete() ) {
					throw new IOException("Unable to forcefully delete: " + destination);
				}
			}
			else {
				throw new IOException(destination + " already exists!");
			}
		}
		
		FileInputStream istream = new FileInputStream(src);
		FileOutputStream ostream = new FileOutputStream(dst);
		
		FileChannel dstChannel = ostream.getChannel();
		FileChannel srcChannel = istream.getChannel();
		try {
			long totalBytesTransferred = 0;
			long totalNeededForTransfer = srcChannel.size();
			while( totalBytesTransferred < totalNeededForTransfer ) {
				totalBytesTransferred += srcChannel.transferTo(totalBytesTransferred, totalNeededForTransfer-totalBytesTransferred, dstChannel);
			}
		}
		finally {
			try { srcChannel.close(); } catch(Exception e) {}
			try { dstChannel.close(); } catch(Exception e) {}
		}
	}
	
	public static final void move(String filename, String destination) throws Exception {
		move(filename, destination, false);
	}
	
	public static final void xmove(String filename, String destination) throws Exception {
		move(filename, destination, true);
	}
	
	private static final void move(String filename, String destination, boolean force) throws Exception {
		File src = new File(filename);
		if ( ! src.exists() ) {
			throw new IOException(filename + " doesn't exist!");
		}
		
		File dst = new File(destination);
		if ( dst.exists() ) {
			if ( force ) {
				if ( dst.delete() ) {
					throw new IOException("Unable to forcefully delete: " + destination);
				}
			}
			else {
				throw new IOException(destination + " already exists!");
			}
		}
		
		if ( ! src.renameTo(dst) ) {
			throw new IOException("Unable to move: " + filename + " to: " + destination );
		}
	}
	
	/**
	 * Allocates a new memory buffer.
	 * @param capacity
	 * @return
	 */
	public Buffer newBuffer(int capacity) {
		return new Buffer(capacity);
	}

	/**
	 * Opens a file.  Standard modes are "r" for open as read only, and "rw" for open as 
	 * read-write.
	 * 
	 * @param filename
	 * @param mode - @see {@link RandomAccessFile} for valid mode values.
	 * @return a file handle
	 * @throws Exception
	 */
	public LeolaFile fopen(String filename, String mode) throws Exception {				
		RandomAccessFile raf = new RandomAccessFile(new File(filename), mode);
		return new LeolaFile(raf, this.runtime);		
	}	
}

