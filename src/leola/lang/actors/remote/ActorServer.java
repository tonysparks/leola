/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.lang.actors.remote;

import java.io.IOException;
import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import leola.lang.actors.Actor;
import leola.network.ClientNetwork;
import leola.network.ServerNetwork;
import leola.network.tcp.SocketServerNetwork;
import leola.vm.Leola;
import leola.vm.exceptions.LeolaRuntimeException;
import leola.vm.types.LeoArray;
import leola.vm.types.LeoClass;
import leola.vm.types.LeoNativeClass;
import leola.vm.types.LeoNull;
import leola.vm.types.LeoObject;
import leola.vm.types.LeoScopedObject;
import leola.vm.types.LeoString;

/**
 * The server for remote clients to connect to.
 * 
 * @author Tony
 *
 */
public class ActorServer extends Actor {

	private ServerNetwork network;
	private ExecutorService executor;
	private AtomicBoolean active;
	private LeoArray clients;
		
	
	/**
	 * The remote client
	 * 
	 * @author Tony
	 *
	 */
	public static class Client {
		ClientNetwork network;
		ProxyActor actor;
		
		/**
		 * @param network
		 */
		public Client(final ActorServer server, final ClientNetwork network) {
			this.network = network;
			this.actor = new ProxyActor(server.getMessageBox(), LeoNull.LEONULL, server.getRuntime(), network);			
			server.executor.submit(new Runnable() {

				@Override
				public void run() {
					try {
						
						/* Receive messages from the remote client */
						while( server.active.get() ) {
							LeoObject msg = actor.receive();
							if ( msg.isClass() ) {
								LeoClass klass = msg.as();
								klass.addProperty(LeoString.valueOf("client"), new LeoNativeClass(Client.this));
							}
									
							server.send(msg);
						}
					}
					catch(Exception e) {
						/* TODO error handler */
						System.err.println(e);
					}
				}
				
			});
		}		
		
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return address();
		}
		
		/**
		 * Sends a message to the remote client
		 * @param msg
		 */
		public void send(LeoObject msg) {
			actor.send(msg);
		}
		
		/**
		 * Disconnects the remote client
		 */
		public void disconnect() {
			try {
				this.network.close();
			} catch (IOException e) {
			}
		}
		
		/**
		 * @return the address of the remote client
		 */
		public String address() {
			return this.network.getConnectedAddress().getHostName();
		}
	}
	
	/**
	 * @param proxypool
	 * @param runtime
	 */
	public ActorServer(Queue<LeoObject> queue, LeoObject obj, Leola runtime) {
		super(queue, obj, runtime);
		
		this.active = new AtomicBoolean(false);
		this.clients = new LeoArray(new Vector<LeoObject>());
				
		/*
		 * Infuse with the actor traits
		 */
		switch(obj.getType()) {
			case CLASS:
			case MAP:
			case NATIVE_CLASS: {
				LeoScopedObject sObj = obj.as();
				sObj.addProperty(LeoString.valueOf("clients"), clients());
				break;
			}
            default:
                throw new LeolaRuntimeException("Unsupported type!");                
		}
	}

	/**
	 * @return the connected clients
	 */
	public LeoArray clients() {
		return this.clients;
	}

	/**
	 * Start the Server in non-blocking mode
	 * 
	 * @param port
	 * @throws Exception
	 */
	public void start(final int port) throws Exception {
		_start(port);				
	}
	
	/**
	 * Starts the server, and waits for termination
	 * 
	 * @param port
	 * @throws Exception
	 */
	public void startAndWait(final int port) throws Exception {
		_start(port).get();		
	}

	private  Future<?> _start(final int port) throws Exception {
		shutdown();
		
		this.executor = Executors.newCachedThreadPool();
		return this.executor.submit(new Runnable() {

			@Override
			public void run() {
				try {
					network = new SocketServerNetwork();
					network.start(port);
					
					active.set(true);
					while(active.get()) {
						ClientNetwork client = network.accept();
						if ( client != null ) {
							clients.$add(new LeoNativeClass(new Client(ActorServer.this, client)));
							
						}
						
						Thread.yield();
					}
					
					for(LeoObject obj : clients.getArray()) {
						try {
							LeoNativeClass nClass = obj.as();
							Client client = (Client)nClass.getInstance();
							client.network.close();
						} catch(Throwable t) {}
					}
					
				}
				catch(Exception e) {
					System.err.println("Error on the server: " + e);
				}
			}
			
		});		
	}
	
	/**
	 * Shutdown the server
	 */
	public void shutdown() {
		this.active.set(false);
		if (this.executor!=null) {
			this.executor.shutdownNow();
		}
	}
	
	/* (non-Javadoc)
	 * @see leola.vm.lang.actors.Actor#kill()
	 */
	@Override
	public void kill() {
		shutdown();
		
		super.kill();
	}
	
}

