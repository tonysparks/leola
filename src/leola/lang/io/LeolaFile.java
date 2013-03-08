/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.lang.io;

import java.io.RandomAccessFile;

import leola.vm.Leola;
import leola.vm.types.LeoArray;
import leola.vm.types.LeoNativeClass;
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
	 * @param raf
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
			this.runtime.execute(func, new LeoNativeClass(this));
		}
		catch(Exception e) {
			try {this.raf.close();}
			catch(Exception ignore) {}
		}
	}
	
	public final int readBuffer(Buffer buffer) throws Exception {
		int bytesRead = raf.read(buffer.getArray());
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
	 * Reads a line.
	 *
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public  final String readString() throws Exception {
	    return this.raf.readUTF();
	}

	public  final String readLine() throws Exception {
//	    return this.raf.readUTF();
		return this.raf.readLine();
	}
	
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
	 * Reads all lines
	 * @param file
	 * @return an array containing all of the lines
	 * @throws Exception
	 */
	public  final LeoArray readLines() throws Exception {
		LeoArray result = new LeoArray();
		String line = null;
		do {
//			line = this.raf.readUTF();
			line = this.raf.readLine();
			if ( line != null ) {
				result.$add(LeoString.valueOf(line));
			}
			
		} while(line != null);
		
		return result;
	}
	
	/**
	 * Writes out bytes.
	 *
	 * @param file
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
	 * Writes out a line
	 *
	 * @param file
	 * @param line
	 * @throws Exception
	 */
	public  final void writeString(String line) throws Exception {
		this.raf.writeUTF(line);
	}
	
	public  final void writeLine(String line) throws Exception {
		//this.raf.writeUTF(line + "\n");		
		this.raf.writeBytes(line);
		this.raf.writeBytes("\n");
	}

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
	 * @param pFile
	 * @param pos
	 * @throws Exception
	 */
	public  final void seek(long pos) throws Exception {
		this.raf.seek(pos);
	}

	/**
	 * Closes the this.
	 *
	 * @param file
	 * @throws Exception
	 */
	public  final void close() throws Exception {
		this.raf.close();
	}

	/**
	 * Returns the length of the file in bytes
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public  final long length() throws Exception {
		return this.raf.length();
	}

	/**
	 * Sets the new length of the this.
	 *
	 * @param file
	 * @param newLength
	 * @throws Exception
	 */
	public  final void setLength(int newLength) throws Exception {
		this.raf.setLength(newLength);
	}
}

