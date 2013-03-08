/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.network.tcp;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import leola.network.ClientNetwork;
import leola.network.ServerNetwork;

/**
 * Uses a non-blocking {@link ServerSocketChannel} as its implementation.
 *
 * @author Tony
 *
 */
public class SocketServerNetwork implements ServerNetwork {

    /**
     * The server socket
     */
    private ServerSocketChannel serverSocket;

    /* (non-Javadoc)
     * @see com.expd.arch.depot.repository.fileserver.network.ServerNetwork#accept()
     */
    public ClientNetwork accept() throws Exception {
        ClientNetwork clientNetwork = null;

        /* This is non-blocking, so if someone is connecting,
         * we create a new ClientNetwork for them.
         */
        SocketChannel socketChannel = this.serverSocket.accept();
        if ( socketChannel != null ) {
            clientNetwork = new SocketClientNetwork(socketChannel);
        }

        return clientNetwork;
    }

    /* (non-Javadoc)
     * @see com.expd.arch.depot.repository.fileserver.network.ServerNetwork#disconnect()
     */
    public void disconnect() throws Exception {
        if ( this.serverSocket != null && this.serverSocket.isOpen() ) {
            this.serverSocket.close();

            ServerSocket socket = this.serverSocket.socket();
            if ( socket != null ) {
                socket.close();
            }
        }
    }

    /* (non-Javadoc)
     * @see com.expd.arch.depot.repository.fileserver.network.ServerNetwork#start(int)
     */
    public void start(int port) throws Exception {
        this.serverSocket = ServerSocketChannel.open();

        /* Make this channel non-blocking */
        this.serverSocket.configureBlocking(false);

        /* Bind to the supplied port */
        ServerSocket socket = this.serverSocket.socket();
        socket.bind(new InetSocketAddress(port));
    }

    /* (non-Javadoc)
     * @see com.expd.arch.depot.repository.fileserver.network.ServerNetwork#getAddress()
     */
    public InetAddress getAddress() {
        InetAddress address = null;

        if ( this.serverSocket != null ) {
            ServerSocket socket = this.serverSocket.socket();
            if ( socket != null ) {
                address = socket.getInetAddress();
            }
        }
        return address;
    }

}

