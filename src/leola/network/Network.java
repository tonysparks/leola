/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.network;

import java.io.Closeable;


/**
 * A {@link Network} provides a means for transmitting bytes to and from remote computers.
 *
 * @author Tony
 *
 */
public interface Network extends Closeable {

    /**
     * Max packet size
     */
    public static int MAX_PACKET_SIZE = 1024 * 4;

    /**
     * Send raw data
     *
     * @param data
     * @param off
     * @param len
     * @throws Exception
     */
    public void send(byte[] data, int off, int len) throws Exception;


    /**
     * Receives data.
     *
     * @param data
     * @param off
     * @param len
     * @return number of bytes actually received
     * @throws Exception
     */
    public int recv(byte[] data, int off, int len) throws Exception;

}

