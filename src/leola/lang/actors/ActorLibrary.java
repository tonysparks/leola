/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.lang.actors;

import java.util.concurrent.ConcurrentLinkedQueue;

import leola.lang.actors.remote.ActorServer;
import leola.lang.actors.remote.ProxyActor;
import leola.network.tcp.SocketClientNetwork;
import leola.vm.Leola;
import leola.vm.exceptions.LeolaRuntimeException;
import leola.vm.lib.LeolaIgnore;
import leola.vm.lib.LeolaLibrary;
import leola.vm.types.LeoNamespace;
import leola.vm.types.LeoObject;


/**
 * The Standard Actor Library for the Leola Programming Language.
 *
 * @author Tony
 *
 */
public class ActorLibrary implements LeolaLibrary {

	private Leola runtime;
	
	/* (non-Javadoc)
	 * @see leola.frontend.LeolaLibrary#init(leola.frontend.Leola)
	 */
	@LeolaIgnore
	public void init(Leola leola, LeoNamespace namespace) throws Exception {
		this.runtime = leola;
		this.runtime.putIntoNamespace(this, namespace);		
	}

	/**
	 * Constructs a new {@link ActorServer}.
	 * 
	 * @param functor
	 * @return a new {@link ActorServer}
	 */
	public final ActorServer newServer(ActorPool pool, LeoObject functor) throws LeolaRuntimeException {
		ActorServer server = new ActorServer(new ConcurrentLinkedQueue<LeoObject>(), functor, runtime);
		pool.postBlockingActor(server);
		return server;
	}
	
	public final ProxyActor newProxy(ActorPool pool, LeoObject functor) throws LeolaRuntimeException {
		ProxyActor actor = new ProxyActor(new ConcurrentLinkedQueue<LeoObject>(), functor, runtime, new SocketClientNetwork());
		pool.postBlockingActor(actor);
		
		return actor;
	}
	
	/**
	 * Constructs a new ActorPool.
	 *
	 * @param leola
	 * @return a new pool
	 */
	public final ActorPool newPool() {
		return new ActorDispersedPool(this.runtime);
	}

	/**
	 * Creates a shared pool in which the actors share 'ticks' on a pool.
	 * @param leola
	 * @return a new pool
	 */
	public final ActorPool newSharedPool() {
		return new ActorSharedPool(this.runtime);
	}

	/**
	 * Constructs a new {@link Actor} that waits for in bound messages
	 *
	 * @param pool
	 * @param func
	 * @return
	 * @throws EvalException
	 */
	public static final LeoObject newBlockActor(ActorPool pool, LeoObject func) throws LeolaRuntimeException {
		return pool.newBlockingActor(func);
	}

	/**
	 * Constructs a new {@link Actor}
	 *
	 * @param pool
	 * @param func
	 * @return
	 * @throws EvalException
	 */
	public static final LeoObject newActor(ActorPool pool, LeoObject func) throws LeolaRuntimeException {
		return pool.newActor(func);
	}
}

