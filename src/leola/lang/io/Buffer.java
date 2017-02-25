/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.lang.io;

import java.nio.ByteBuffer;



/**
 * Wrapper around {@link ByteBuffer}.
 * 
 * @author Tony
 *
 */
public class Buffer {
    
    /**
     * Buffer
     */
    private ByteBuffer buffer;
    
    /**
     * @param size
     */
    public Buffer(int size) {
        this.buffer = ByteBuffer.allocate(size);
    }

    
    public void putByte(Number n) {
        this.buffer.put( n.byteValue() );
    }
    
    public byte readByte() {
        return this.buffer.get();
    }
    
    public void putShort(Number n) {
        this.buffer.putShort( n.shortValue() );
    }
    
    public short readShort() {
        return this.buffer.getShort();
    }
    
    /**
     * @return reads the byte as a string
     */
    public String readString() {
        return (char)this.buffer.get() + "";
    }
    
    public char readChar() {
        return (char)this.buffer.get();
    }
    
    public void putBuffer(Buffer buffer) {
        this.buffer.put(buffer.buffer);
    }
    
    public void putString(String str) {
        this.buffer.put(str.getBytes());
    }
    
    public int position() {
        return this.buffer.position();
    }
    
    public void setPosition(int pos) {
        this.buffer.position(pos);
    }
    
    public int capacity() {
        return this.buffer.capacity();
    }
    
    public void clear() {
        this.buffer.clear();
    }
    
    public void rewind() {
        this.buffer.rewind();
    }
    
    public int remaining() {
        return this.buffer.remaining();
    }
    
    public byte[] getArray() {
        return this.buffer.array();        
    }
    
    public int limit() {
        return this.buffer.limit();
    }
    
    public void setLimit(int limit) {
        this.buffer.limit(limit);
    }
    
    public void mark() {
        this.buffer.limit(this.buffer.position());
    }
}

