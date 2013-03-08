/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.network;

import java.net.InetAddress;

/**
 * A {@link ServerNetwork} accepts incoming requests.  The requests are grouped by a client, and
 * is assigned a {@link ClientNetwork}.
 *
 * @author Tony
 *
 */
public interface ServerNetwork {

    /**
     * Start listening on the supplied port number.
     *
     * @param port
     * @throws Exception
     */
    public void start(int port) throws Exception;

    /**
     * This is a non-blocking call.  This returns null if no connection
     * request has been made.  If a connection request has been made, the
     * associated {@link ClientNetwork} is returned.
     * @return null if no connection, otherwise the {@link ClientNetwork}
     * @throws Exception
     */
    public ClientNetwork accept() throws Exception;


    /**
     * Disconnects the server, stops accepting incoming requests.
     *
     * @throws Exception
     */
    public void disconnect() throws Exception;


    /**
     * @return the servers address
     */
    public InetAddress getAddress();
}

