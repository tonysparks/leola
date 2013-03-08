/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.lang.actors;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import leola.vm.Leola;
import leola.vm.exceptions.LeolaRuntimeException;
import leola.vm.types.LeoObject;


/**
 * @author Tony
 *
 */
public class ActorDispersedPool implements ActorPool {

	/**
	 * The pool of threads
	 */
	private ExecutorService executor;

	/**
	 * The execution context
	 */
	private Leola runtime;


	/**
	 * Actors
	 */
	private Queue<Actor> actors;


	/**
	 * @param leola
	 */
	public ActorDispersedPool(Leola leola) {
		this.runtime = leola;
		this.actors = new ConcurrentLinkedQueue<Actor>();

		this.executor = Executors.newCachedThreadPool();

		Thread.currentThread().setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

			public void uncaughtException(Thread t, Throwable e) {
				if ( !ignore(e) ) {								
					System.out.println(e);	
				}
			}
		});
	}


	private boolean ignore(Throwable e) {		
		for(Throwable i = e; i != null; i = i.getCause()) {
			if ( (i instanceof InterruptedException) ) {
				return true; /* ignore the interrupts */
			}	
		}
		
		return false;
	}
	
	/* (non-Javadoc)
	 * @see leola.vm.lang.actors.ActorPool#await()
	 */	
	public void awaitShutdown() {
		// TODO
		
	}
	
	/**
	 * Shuts down the Actor pool
	 */
	public void shutdown() {
		shutdownAndAwaitTermination(this.executor);
	}

	/**
	 * Taken from the Executors Example.
	 * @param pool
	 */
	private void shutdownAndAwaitTermination(ExecutorService pool) {
		pool.shutdown(); // Disable new tasks from being submitted
		try {
			// Wait a while for existing tasks to terminate
			if (!pool.awaitTermination(1, TimeUnit.SECONDS)) {
				pool.shutdownNow(); // Cancel currently executing tasks
				// Wait a while for tasks to respond to being cancelled
				pool.awaitTermination(1, TimeUnit.SECONDS);
			}
		}
		catch (InterruptedException ie) {
			// (Re-)Cancel if current thread also interrupted
			pool.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
		}
		finally {
			for(Actor actor: this.actors) {
				try {
					actor.kill();
				}
				catch(Exception e) {}
			}

			this.actors.clear();
		}
	}
	
	/**
	 * Posts a blocking actor
	 * 
	 * @param actor
	 * @return
	 * @throws LeolaRuntimeException
	 */
	public final Actor postBlockingActor(final Actor actor) throws LeolaRuntimeException {
		Future<Void> future = this.executor.submit(new Callable<Void>() {

			public Void call() throws Exception {
				try {
					actor.react();
				}
				catch(Throwable t) {
					if ( !ignore(t) ) {								
						System.out.println(t);	
					}
				}
				return null;
			}

		});

		actor.setFuture(future);

		this.actors.add(actor);

		return actor;		
	}
	
	/**
	 * Posts a non-blocking Actor
	 * 
	 * @param func
	 * @return
	 * @throws EvalException
	 */
	public final Actor postNonBlockingActor(final Actor actor) throws LeolaRuntimeException {	
		Future<Void> future = this.executor.submit(new Callable<Void>() {

			public Void call() throws Exception {
				try {
					actor.act();
				}
				catch(Throwable t) {
					if ( !ignore(t) ) {								
						System.out.println(t);	
					}
				}
				return null;
			}

		});

		actor.setFuture(future);

		this.actors.add(actor);

		return actor;
	}
	
	/**
	 * Creates a new {@link Actor} that blocks and waits for in bound messages.
	 *
	 * @param func
	 * @return
	 * @throws EvalException
	 */
	public final LeoObject newBlockingActor(LeoObject func) throws LeolaRuntimeException {
		final Actor actor = new Actor(new LinkedBlockingQueue<LeoObject>(), func, this.runtime);
		postBlockingActor(actor);
		return func;
	}

	/**
	 * Creates a new {@link Actor} that does not block or wait for messages.
	 * @param func
	 * @return
	 * @throws EvalException
	 */
	public final LeoObject newActor(LeoObject func) throws LeolaRuntimeException {
		final Actor actor = new Actor(new ConcurrentLinkedQueue<LeoObject>(), func, this.runtime );
		postNonBlockingActor(actor);
		return func;
	}
}

