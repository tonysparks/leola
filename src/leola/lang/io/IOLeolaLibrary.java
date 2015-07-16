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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.List;

import leola.vm.Leola;
import leola.vm.lib.LeolaIgnore;
import leola.vm.lib.LeolaLibrary;
import leola.vm.types.LeoNamespace;


/**
 * The Input/Output Library, handles file manipulation operations
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
	
	/**
	 * Copies a source file to the destination.  This will error out if the destination already exists.
	 * 
	 * @param source the source file name
	 * @param destination the destination file name
	 * @throws Exception
	 */
	public static final void copy(String source, String destination) throws Exception {
		copy(source, destination, false);
	}
	
	
	/**
	 * Copies a source file to the destination, overriding the destination file if it already
	 * exists.
	 * 
	 * @param source
	 * @param destination
	 * @throws Exception
	 */
	public static final void xcopy(String source, String destination) throws Exception {
		copy(source, destination, true);
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
		try {
    		FileOutputStream ostream = new FileOutputStream(dst);
    		try {
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
    		finally {
    		    ostream.close();
    		}
		}
		finally {
		    istream.close();
		}
	}
	
	
	/**
	 * Moves the source file to the destination.  This will error out
	 * if the destination file already exists.
	 * 
	 * @param source
	 * @param destination
	 * @throws Exception
	 */
	public static final void move(String source, String destination) throws Exception {
		move(source, destination, false);
	}
	
	
	/**
	 * Moves the source file to the destination, overriding the destination file
	 * if it already exists.
	 * 
	 * @param source
	 * @param destination
	 * @throws Exception
	 */
	public static final void xmove(String source, String destination) throws Exception {
		move(source, destination, true);
	}
	
	private static final void move(String source, String destination, boolean force) throws Exception {
		File src = new File(source);
		if ( ! src.exists() ) {
			throw new IOException(source + " doesn't exist!");
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
			throw new IOException("Unable to move: " + source + " to: " + destination );
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
	
	/**
	 * Creates a new {@link FileInputStream}
	 * 
	 * @param filename
	 * @return the {@link FileInputStream}
	 * @throws Exception
	 */
	public InputStream newFileInputStream(String filename) throws Exception {
	    return new FileInputStream(filename);
	}
	
	
	/**
	 * Creates a new {@link FileOutputStream}
	 * 
	 * @param filename
	 * @param append if open and start appending to the file.  Defaults to false.
	 * @return the {@link FileOutputStream}
	 * @throws Exception
	 */
	public OutputStream newFileOutputStream(String filename, Boolean append) throws Exception {
	    return new FileOutputStream(filename, append!=null ? append : false);
	}
	
	/**
	 * Converts the string into a {@link File}
	 * 
	 * @param filename
	 * @return the {@link File}
	 */
	public File file(String filename) {
	    return new File(filename);
	}
	
	/**
     * The default buffer size
     */
    public static final int DEFAULT_BUFFER_SIZE = 2048 * 2;
    
    /**
     * Reads in a file and pipes to the supplied {@link OutputStream}.
     * 
     * @param file
     * @param oStream
     * @return
     * @throws IOException
     */
    public static long readFile(File file, OutputStream oStream) throws IOException {
        FileInputStream iStream = new FileInputStream(file);        
        FileChannel fileChannel = iStream.getChannel();
        
        long bytesRead = 0;    
        try {
            WritableByteChannel target = Channels.newChannel(oStream);
            while(bytesRead < fileChannel.size()) {
                bytesRead += fileChannel.transferTo(0, fileChannel.size(), target);
            }
        }
        finally {
            iStream.close();
        }
        
        
        return bytesRead;
    }
    
    
    /**
     * Writes out a file to disk from the supplied {@link InputStream}.
     * 
     * @param file
     * @param iStream
     * @return
     * @throws IOException
     */
    public static long writeFile(File file, InputStream iStream) throws IOException {
        FileOutputStream oStream = new FileOutputStream(file);
        FileChannel fileChannel = oStream.getChannel();
        FileLock lock = fileChannel.lock();
        
        long bytesRead = 0;
        long totalBytesRead = 0;
        try {   
            try {
                do {
                    bytesRead = fileChannel.transferFrom(Channels.newChannel(iStream), totalBytesRead, DEFAULT_BUFFER_SIZE);
                    totalBytesRead += bytesRead;
                }
                while( bytesRead > 0);
            }
            finally {
                if ( lock != null ) {
                    lock.release();      
                }
            }
                
        }
        finally {
            oStream.close();
        }
        
        return totalBytesRead;
    }
        
    /**
     * Copy the input into the output.
     * 
     * @param iStream
     * @param oStream
     * @param bufferSize - size of the buffer
     * @return number of bytes copied.
     * 
     * @throws RepositoryException
     */
    public static long streamCopy(InputStream iStream, OutputStream oStream, final int bufferSize) throws IOException {
        
        byte[] buffer = new byte[bufferSize];
        long size = streamCopy(iStream, oStream, buffer);       
        
        return size;
    }

    /**
     * Copy the input into the output.
     * 
     * @param iStream
     * @param oStream
     * @param buffer - buffer to be used
     * @return number of bytes copied.
     * 
     * @throws RepositoryException
     */
    public static long streamCopy(InputStream iStream, OutputStream oStream, byte[] buffer) throws IOException {        
        long size = 0;
        int length = 0;
               
        while( (length = iStream.read(buffer)) >= 0 ) {
            oStream.write(buffer, 0, length);
            size += length;
        }
        
        oStream.flush();
        
        
        return size;
    }

}

