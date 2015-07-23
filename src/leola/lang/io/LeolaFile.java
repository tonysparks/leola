/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.lang.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UTFDataFormatException;

import leola.vm.Leola;
import leola.vm.types.LeoArray;
import leola.vm.types.LeoObject;
import leola.vm.types.LeoString;

/**
 * A simple wrapper around a {@link RandomAccessFile}
 * 
 * @author Tony
 *
 */
public class LeolaFile {

	private RandomAccessFile raf;
	private Leola runtime;
	
	
	/**
	 * @param raf the {@link RandomAccessFile} that has been opened
	 * @param runtime the Leola runtime 
	 */
	public LeolaFile(RandomAccessFile raf, Leola runtime) {	
		this.raf = raf;
		this.runtime = runtime;
	}	
	
	/**
	 * Executes the supplied function and always closes the file afterwards (even if
	 * an exception occurs).
	 * 
	 * @param func
	 * @throws Exception
	 */
	public void with(LeoObject func) throws Exception {
		try {
			this.runtime.execute(func, LeoObject.valueOf(this));
		}
		catch(Exception e) {
			try {this.raf.close();}
			catch(Exception ignore) {}
		}
	}
	
	/**
	 * Reads the contents of the file into the supplied {@link Buffer} according to the
	 * {@link Buffer#position()} and {@link Buffer#limit()}.
	 * 
	 * @param buffer
	 * @return the number of bytes read
	 * @throws Exception
	 */
	public final int readBuffer(Buffer buffer) throws Exception {
		int bytesRead = raf.read(buffer.getArray(), buffer.position(), buffer.limit());
		buffer.setLimit(bytesRead);
		return bytesRead;
	}
	
	
	public final int read() throws Exception {
		return raf.read();
	}
	public  final boolean readBoolean() throws Exception {
		return this.raf.readBoolean();
	}
	public  final byte readByte() throws Exception {
		return this.raf.readByte();
	}
	public  final char readChar() throws Exception {
		return this.raf.readChar();
	}
	public  final short readShort() throws Exception {
		return this.raf.readShort();
	}
	public  final int readInt() throws Exception {
		return this.raf.readInt();
	}
	public  final float readFloat() throws Exception {
		return this.raf.readFloat();
	}
	public  final double readDouble() throws Exception {
		return this.raf.readDouble();
	}
	public  final long readLong() throws Exception {
		return this.raf.readLong();
	}
	
	
    /**
     * Reads in a string from this file. The string has been encoded
     * using a
     * <a href="DataInput.html#modified-utf-8">modified UTF-8</a>
     * format.
     * <p>
     * The first two bytes are read, starting from the current file
     * pointer, as if by
     * <code>readUnsignedShort</code>. This value gives the number of
     * following bytes that are in the encoded string, not
     * the length of the resulting string. The following bytes are then
     * interpreted as bytes encoding characters in the modified UTF-8 format
     * and are converted into characters.
     * <p>
     * This method blocks until all the bytes are read, the end of the
     * stream is detected, or an exception is thrown.
     *
     * @return     a Unicode string.
     * @exception  EOFException            if this file reaches the end before
     *               reading all the bytes.
     * @exception  IOException             if an I/O error occurs.
     * @exception  UTFDataFormatException  if the bytes do not represent
     *               valid modified UTF-8 encoding of a Unicode string.
     * @see        java.io.RandomAccessFile#readUnsignedShort()
     */
	public  final String readUTF() throws Exception {
	    return this.raf.readUTF();
	}

	
    /**
     * Reads the next line of text from this file.  This method successively
     * reads bytes from the file, starting at the current file pointer,
     * until it reaches a line terminator or the end
     * of the file.  Each byte is converted into a character by taking the
     * byte's value for the lower eight bits of the character and setting the
     * high eight bits of the character to zero.  This method does not,
     * therefore, support the full Unicode character set.
     *
     * <p> A line of text is terminated by a carriage-return character
     * (<code>'&#92;r'</code>), a newline character (<code>'&#92;n'</code>), a
     * carriage-return character immediately followed by a newline character,
     * or the end of the file.  Line-terminating characters are discarded and
     * are not included as part of the string returned.
     *
     * <p> This method blocks until a newline character is read, a carriage
     * return and the byte following it are read (to see if it is a newline),
     * the end of the file is reached, or an exception is thrown.
     *
     * @return     the next line of text from this file, or null if end
     *             of file is encountered before even one byte is read.
     * @exception  IOException  if an I/O error occurs.
     */
	public  final String readLine() throws Exception {
		return this.raf.readLine();
	}
	
	
	/**
	 * Read the full contents of the file and stores it in a {@link String}.  Users
	 * of this must be aware of memory consumption, as this will put the full file
	 * into VM memory.
	 * 
	 * @return the full file contents in a {@link String}.
	 * @throws Exception
	 */
	public final String readFully() throws Exception {
		StringBuilder sb = new StringBuilder((int)this.raf.length());
		String line = null;
		do {
			line = this.raf.readLine();
			if ( line != null) {
				sb.append(line).append("\n");
			}
			
		} while(line != null);
		
		return sb.toString();
	}
	
	/**
	 * Reads all lines according to the same rules as {@link LeolaFile#readLine()}
	 * 
	 * @return an array containing all of the lines
	 * @throws Exception
	 */
	public  final LeoArray readLines() throws Exception {
		LeoArray result = new LeoArray();
		String line = null;
		do {
			line = this.raf.readLine();
			if ( line != null ) {
				result.add(LeoString.valueOf(line));
			}
			
		} while(line != null);
		
		return result;
	}
	
	/**
	 * Writes out bytes from the {@link Buffer}
	 *
	 * @param buffer
	 * @throws Exception
	 */
	public  final void writeBuffer(Buffer buffer) throws Exception {
		this.raf.write(buffer.getArray(), buffer.position(), buffer.remaining());
	}

	public  final void write(int b) throws Exception {
		this.raf.write(b);
	}
	public  final void writeBoolean(boolean b) throws Exception {
		this.raf.writeBoolean(b);
	}
	public  final void writeByte(byte b) throws Exception {
		this.raf.writeByte(b);
	}
	public  final void writeChar(char b) throws Exception {
		this.raf.writeChar(b);
	}
	public  final void writeShort(short b) throws Exception {
		this.raf.writeShort(b);
	}
	public  final void writeInt(int b) throws Exception {
		this.raf.writeInt(b);
	}
	public  final void writeFloat(float b) throws Exception {
		this.raf.writeFloat(b);
	}
	public  final void writeDouble(double b) throws Exception {
		this.raf.writeDouble(b);
	}
	public  final void writeLong(long b) throws Exception {
		this.raf.writeLong(b);
	}
	
    /**
     * Writes a string to the file using
     * <a href="DataInput.html#modified-utf-8">modified UTF-8</a>
     * encoding in a machine-independent manner.
     * <p>
     * First, two bytes are written to the file, starting at the
     * current file pointer, as if by the
     * <code>writeShort</code> method giving the number of bytes to
     * follow. This value is the number of bytes actually written out,
     * not the length of the string. Following the length, each character
     * of the string is output, in sequence, using the modified UTF-8 encoding
     * for each character.
     *
     * @param      line   a string to be written.
     * @exception  IOException  if an I/O error occurs.
     */
	public  final void writeUTF(String line) throws Exception {
		this.raf.writeUTF(line);
	}
	
	
	
    /**
     * Writes the string to the file as a sequence of bytes. Each
     * character in the string is written out, in sequence, by discarding
     * its high eight bits. The write starts at the current position of
     * the file pointer.  A new line is always appended.
     *
     * @param      line   a string of bytes to be written.
     * @exception  IOException  if an I/O error occurs.
     */
	public  final void writeLine(String line) throws Exception {
		this.raf.writeBytes(line);
		this.raf.writeBytes("\n");
	}

	/**
     * Writes <code>len</code> bytes from the specified byte array
     * starting at offset <code>off</code> to this file.
     *
     * @param      bytes the data.
     * @param      off   the start offset in the data.
     * @param      len   the number of bytes to write.
     * @exception  IOException  if an I/O error occurs.
     */
	public final void writeBytes(byte[] bytes, int off, int len) throws Exception {
	    this.raf.write(bytes, off, len);
	}
	
	
    /**
     * Writes <code>b.length</code> bytes from the specified byte array
     * to this file, starting at the current file pointer.
     *
     * @param      bytes the data.
     * @exception  IOException  if an I/O error occurs.
     */
	public final void writeBytes(byte[] bytes) throws Exception {
	    this.raf.write(bytes);
	}
	
	
	/**
	 * Write out each element in the supplied {@link LeoArray}, each element is
	 * treated as a string, after which a new line will be appended.
	 * 
	 * <pre>
	 *     file.writeLines( [ "a", "b", "c" ] )
	 *     // writes:
	 *     // a
	 *     // b
	 *     // c
	 * </pre>
	 * 
	 * @param array
	 * @throws Exception
	 */
	public  final void writeLines(LeoArray array) throws Exception {
		if ( array != null ) {
			for(int i = 0; i < array.size(); i++) {
				LeoObject obj = array.get(i);
				if ( obj != null ) {
					//this.raf.writeUTF(obj.toString() + "\n");
					this.raf.writeBytes(obj.toString());
					this.raf.writeBytes("\n");
				}
				else {					
					this.raf.writeBytes("\n");
//					this.raf.writeUTF("\n");
				}
			}
		}
	}
	
	/**
	 * Seeks into a this.
	 *
	 * @param pos the position in number of bytes
	 * @throws Exception
	 */
	public  final void seek(long pos) throws Exception {
		this.raf.seek(pos);
	}

	/**
	 * The current file position pointer
	 * 
	 * @see LeolaFile#seek(long)
	 * @return the file position pointer in number of bytes
	 * @throws Exception
	 */
	public final long position() throws Exception {
	    return this.raf.getFilePointer();
	}
	
	/**
	 * Closes the this.
	 *
	 * @throws Exception
	 */
	public  final void close() throws Exception {
		this.raf.close();
	}

	/**
	 * Returns the length of the file in bytes
     *
	 * @return the length of the file in number of bytes
	 * @throws Exception
	 */
	public  final long length() throws Exception {
		return this.raf.length();
	}

	/**
	 * Sets the new length of the this.
	 *
	 * @param newLength
	 * @throws Exception
	 */
	public  final void setLength(int newLength) throws Exception {
		this.raf.setLength(newLength);
	}
}

