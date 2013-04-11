/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.network.tcp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import leola.network.ClientNetwork;

/**
 * @author Tony
 *
 */
public class SocketClientNetwork implements ClientNetwork {

    /**
     * The clients socket
     */
    private SocketChannel socketChannel;

    /**
     * The attempted connection address
     */
    private InetAddress attemptedServerAddress;

    /**
     * @param socketChannel - the socket channel to use, this assumes
     * the supplied socketChannel is already connected to a remote socket.
     */
    public SocketClientNetwork(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    /**
     * Use the {@link ClientNetwork#connect(InetAddress, int)} to connect to a
     * remote socket.
     */
    public SocketClientNetwork() {
    }


    /*
     * (non-Javadoc)
     * @see leola.network.ClientNetwork#connect(java.net.InetAddress, int)
     */
    public void connect(InetAddress remoteServer, int port) throws Exception {
        close();

        this.attemptedServerAddress = remoteServer;

        InetSocketAddress address = new InetSocketAddress(remoteServer, port);

        /* Attempt to connect to the remote server */
        this.socketChannel = SocketChannel.open(address);
        this.socketChannel.configureBlocking(true);

        Socket socket = this.socketChannel.socket();
        socket.setTcpNoDelay(false); /* Turn off Nagle's shitty algorithm */
    }


    /*
     * (non-Javadoc)
     * @see java.io.Closeable#close()
     */
    public void close() throws IOException {
        if ( this.socketChannel != null && this.socketChannel.isConnected() ) {
            /* Stop blocking so we can shutdown */
            this.socketChannel.configureBlocking(false);
            this.socketChannel.close();

            Socket socket = this.socketChannel.socket();
            if ( socket != null ) {
            //    socket.shutdownInput();
            //    socket.shutdownOutput();
                socket.close();
            }


        }

    }

    /*
     * (non-Javadoc)
     * @see leola.network.Network#recv(byte[], int, int)
     */
    public int recv(byte[] data, int off, int len) throws Exception {
        ByteBuffer buffer = ByteBuffer.wrap(data, off, len);

        /* Attempt to read the buffer length */
        int bytesRead = this.socketChannel.read(buffer);

        return (bytesRead);
    }

    /*
     * (non-Javadoc)
     * @see leola.network.Network#send(byte[], int, int)
     */
    public void send(byte[] data, int off, int len) throws Exception {
        ByteBuffer buffer = ByteBuffer.wrap(data, off, len);

        /* Attempt to write to the remote server */
        int bytesWritten = 0;
        while (bytesWritten < len) {
            bytesWritten += this.socketChannel.write(buffer);
        }
    }

    /*
     * (non-Javadoc)
     * @see leola.network.ClientNetwork#getConnectedAddress()
     */
    public InetAddress getConnectedAddress() {
        InetAddress result = null;
        if ( this.socketChannel != null ) {
            Socket socket = this.socketChannel.socket();
            if ( socket != null ) {
                result = socket.getInetAddress();
            }
        }

        /* Attempt to use the actual connected address.  If not
         * connected use the attempted connected address
         */
        return result==null ?
                this.attemptedServerAddress :
                result;
    }
}

