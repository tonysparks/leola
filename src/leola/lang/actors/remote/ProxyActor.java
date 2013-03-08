/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.lang.actors.remote;

import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import leola.lang.actors.Actor;
import leola.network.ClientNetwork;
import leola.vm.ClassDefinitions;
import leola.vm.Leola;
import leola.vm.asm.Symbols;
import leola.vm.exceptions.LeolaRuntimeException;
import leola.vm.types.LeoClass;
import leola.vm.types.LeoObject;
import leola.vm.types.LeoObject.LeoType;
import leola.vm.types.LeoString;
import leola.vm.util.NetworkInputStream;

/**
 * The {@link ProxyActor} serves as a communication piece between Server and Client.  On the Client side, the
 * {@link ProxyActor} connects to the {@link ActorServer}.  On the Server side the {@link ProxyActor} is a means for
 * the {@link ActorServer} and client to communicate.
 * 
 * <p>
 * The main difference, is on the client side, the {@link ProxyActor#connect(String, int)} method is invoked, and spawns
 * a thread for listening to messages from the {@link ActorServer}.
 * 
 * @author Tony
 *
 */
public class ProxyActor extends Actor {

	/**
	 * The network
	 */
	private ClientNetwork network;
	private RawByteArrayOutputStream ostream;
	private NetworkInputStream istream;
	private ExecutorService executorService;
		
	class RawByteArrayOutputStream extends ByteArrayOutputStream {
		
		public byte[] getBytes() {
			return this.buf;
		}		
	}
	
	/**
	 * @param queue
	 * @param obj
	 * @param interpreter
	 */
	public ProxyActor(Queue<LeoObject> queue
					, LeoObject obj
					, Leola runtime
					, ClientNetwork network) {
		
		super(queue, obj, runtime);
		
		this.network = network;
		this.istream = new NetworkInputStream(network, new byte[1024*2]);
		this.ostream = new RawByteArrayOutputStream();
	}

	/**
	 * Connects to the remote server
	 * 
	 * @param address
	 * @param port
	 * @throws Exception
	 */
	public void connect(String address, int port) throws Exception {
		this.network.connect(InetAddress.getByName(address), port);
		this.executorService = Executors.newSingleThreadExecutor();
		this.executorService.submit(new Runnable() {

			@Override
			public void run() {
				while(isAlive()) {
					LeoObject msg = receive();
					sendToMailbox(msg);
				}
			}
			
		});
	}
	
	
	/**
	 * Receives a message
	 * 
	 * @throws LeolaRuntimeException
	 */
	public LeoObject receive() throws LeolaRuntimeException {
		try {
			Leola runtime = this.getRuntime();
			Symbols symbols = runtime.getSymbols();
			LeoObject env = runtime.getGlobalNamespace();
			
			DataInput in = new DataInputStream(this.istream);
			
			LeoObject msg = null;
			int type = in.readInt();
			switch(LeoType.values()[type]) {
				case ARRAY: 
				case MAP:
				case STRING:
				case INTEGER:
				case REAL:
				case NULL:
				case BOOLEAN: {						
					msg = LeoObject.read(env, symbols, in);
					break;
				}
				case CLASS: {
					LeoObject className = LeoObject.read(env, symbols, in);
					ClassDefinitions cd = symbols.lookupClassDefinitions(className);
					if (cd==null) {
						throw new LeolaRuntimeException("Unable to instantiate: " + className);
					}
					
					int nparams = in.readInt();
					LeoObject[] params=new LeoObject[nparams];
					for(int i = 0; i < nparams; i++) {
						params[i] = LeoObject.read(env, symbols, in);
					}
					
					msg = cd.newInstance(runtime, className.toLeoString(), params);										
					break;
				}
			}
			
			return msg;
			
		} catch (Exception e) {
			throw new LeolaRuntimeException(e);
		}
	}
	
	private void sendToMailbox(LeoObject msg) {
		super.send(msg);
	}
	
	/* (non-Javadoc)
	 * @see leola.lang.actors.Actor#send(leola.types.LeoObject)
	 */
	@Override
	public void send(LeoObject msg) {
		if ( msg != null ) {
			ostream.reset();
			
			DataOutput out = new DataOutputStream(ostream); 
			try {
				out.writeInt(msg.getType().ordinal());
				switch(msg.getType()) {
					case ARRAY: 
					case MAP:
					case STRING:
					case INTEGER:
					case REAL:
					case NULL:
					case BOOLEAN: {						
						msg.write(out);
						break;
					}
					case CLASS: {
						LeoClass klass = msg.as();
						
						klass.getClassName().write(out);
						LeoString[] paramNames = klass.getParamNames();
						if ( paramNames != null ) {
							out.writeInt(paramNames.length);
																																	
							for(int i = 0; i < paramNames.length; i++ ) {
								LeoString reference = paramNames[i];
								LeoObject property = klass.getProperty(reference);
								property.write(out);
							}
						}
						else {
							out.writeInt(0);
						}									
						break;
					}				
				}
												
				this.network.send(ostream.getBytes(), 0, ostream.size());
			}
			catch(Exception e) {
				throw new LeolaRuntimeException(e);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see leola.lang.actors.Actor#kill()
	 */
	@Override
	public void kill() {
		try {
			if ( this.executorService != null ) {
				try {
					this.executorService.shutdownNow(); 
				}
				catch(Exception e) {}
			}
			
			this.network.close();
			
		}
		catch(Exception e) {			
		}
		finally {
			super.kill();
		}
	}

}

