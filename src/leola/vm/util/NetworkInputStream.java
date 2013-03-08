/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.vm.util;

import java.io.IOException;
import java.io.InputStream;

import leola.network.Network;

/**
 * @author Tony
 *
 */
public class NetworkInputStream extends InputStream {

	private Network network;
	private ExpandableByteArrayInputStream stream;
	private byte[] buffer;
	private byte[] buffer2;
	private boolean useBuffer2;
	
	/**
	 * @param network
	 */
	public NetworkInputStream(Network network, byte[] buffer) {
		super();
		this.network = network;
		this.buffer = buffer;
		this.buffer2 = new byte[buffer.length];
		this.useBuffer2 = true;
		
		this.stream = new ExpandableByteArrayInputStream(buffer,0,0);
	}

	private int readFromNetwork() throws IOException {		
		try {
			int numberOfBytesTransferred = useBuffer2 ? this.network.recv(this.buffer2, 0, this.buffer2.length) : 
				this.network.recv(this.buffer, 0, this.buffer.length);
			
			
			this.stream.addBuffer(useBuffer2 ? buffer2 : buffer, 0, numberOfBytesTransferred);
			useBuffer2 = !useBuffer2;
			
			return numberOfBytesTransferred;
		} catch (Exception e) {
			throw new IOException(e);
		}
		
	}
	
	/* (non-Javadoc)
	 * @see java.io.InputStream#read(byte[])
	 */
	@Override
	public int read(byte[] b, int offset, int len) throws IOException {
		int numberOfBytesRead = this.stream.read(b, offset, len);
		if ( numberOfBytesRead < 0 ) {
			try {
				readFromNetwork();				
				numberOfBytesRead = read(b, offset, len);
			} catch (Exception e) {
				throw new IOException(e);
			}
		}
		
		return numberOfBytesRead;
	}
	/* (non-Javadoc)
	 * @see java.io.InputStream#read()
	 */
	@Override
	public int read() throws IOException {
		int byteRead = this.stream.read();
		if ( byteRead < 0 ) {
			try {
//				int numberOfBytesTransferred = this.network.recv(this.buffer, 0, this.buffer.length);
//				this.stream.addBuffer(buffer, 0, numberOfBytesTransferred);
				readFromNetwork();		
				
				byteRead = read();
			} catch (Exception e) {
				throw new IOException(e);
			}
		}
		
		return byteRead;
	}
	
	
}

