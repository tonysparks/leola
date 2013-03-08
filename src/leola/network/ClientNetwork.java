/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.network;

import java.net.InetAddress;

/**
 * A {@link ClientNetwork} represents a remote client connected to another computer.
 *
 * @author Tony
 *
 */
public interface ClientNetwork extends Network {

    /**
     * Connect to a remote server.
     *
     * @param remoteServer
     * @param port
     * @throws Exception
     */
    public void connect(InetAddress remoteServer, int port) throws Exception;

    /**
     * @return the remote clients address
     */
    public InetAddress getConnectedAddress();
}

