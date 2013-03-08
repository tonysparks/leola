/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.vm.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Allows to append byte[] to the input stream for buffering large data sets.
 * 
 * @author Tony
 *
 */
public class ExpandableByteArrayInputStream extends InputStream {

    /**
     * Underlying byte array input stream
     */
    private ByteArrayInputStream iStream;
    
    /**
     * Byte buffers to read from next
     */
    private Queue<ByteBuffer> bufferQueue;
    
    /**
     * @param b
     * @param off
     * @param len
     */
    public ExpandableByteArrayInputStream(byte[] b, int off, int len) {
        this.iStream = new ByteArrayInputStream(b, off, len);
        this.bufferQueue = new LinkedList<ByteBuffer>();
    }
    
    /**
     * @param b
     */
    public ExpandableByteArrayInputStream(byte[] b) {
        this(b, 0, b.length);
    }
    
    /**
     * Buffer to add
     * @param b
     */
    public void addBuffer(byte[] b) {
        ByteBuffer buffer = ByteBuffer.wrap(b);
        
        this.bufferQueue.add(buffer);
    }
    
    /**
     * Adds a buffer to the {@link InputStream} 
     * @param b
     * @param off
     * @param len
     */
    public void addBuffer(byte[] b, int off, int len) {
        ByteBuffer buffer = ByteBuffer.wrap(b, off, len);
        
        this.bufferQueue.add(buffer);
    }
    
    
    /* (non-Javadoc)
     * @see java.io.InputStream#read()
     */
    @Override
    public int read() throws IOException {
        int result = 0;
        
        int b = this.iStream.read();
        if ( b == -1 ) {
            if ( this.bufferQueue.isEmpty() ) {
                result = -1; /* End of the queued buffers */
            }
            else {
                ByteBuffer buf = this.bufferQueue.poll();
                this.iStream = new ByteArrayInputStream(buf.array(), buf.position(), buf.remaining() );
                
                /* recursively read from the updated iStream */
                result = read();
            }
        }
        else {
            result = b;
        }
        
        return result;
    }
        
    /*
     * (non-Javadoc)
     * @see java.io.InputStream#read(byte[], int, int)
     */
    @Override
    public int read(byte buffer[], int off, int len) throws IOException {
        int result = 0;
        
        int bytesRead = this.iStream.read(buffer, off, len);        
        if ( bytesRead == -1 ) {
            if ( this.bufferQueue.isEmpty() ) {
                result = -1; /* End of the queued buffers */
            }
            else {
                ByteBuffer buf = this.bufferQueue.poll();
                this.iStream = new ByteArrayInputStream(buf.array(), buf.position(), buf.remaining() );
                
                /* recursively read from the updated iStream */
                result = read(buffer, off, len);
            }
        }
        else {
            result = bytesRead;
        }
        
        return result;
    }

}

