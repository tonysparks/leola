/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.lang.actors;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import leola.vm.Leola;
import leola.vm.exceptions.LeolaRuntimeException;
import leola.vm.types.LeoObject;


/**
 * @author Tony
 *
 */
public class ActorSharedPool implements ActorPool {
	/**
	 * The pool of threads
	 */
	private ExecutorService executor;

	/**
	 * The execution context
	 */
	private Leola leola;


	/**
	 * Actors
	 */
	private Queue<Actor> actors;

	/**
	 * The pool is Open
	 */
	private AtomicBoolean isOpen;

	/**
	 * @param leola
	 */
	public ActorSharedPool(Leola leola) {
		this.leola = leola;

		this.actors = new ConcurrentLinkedQueue<Actor>();
		this.isOpen = new AtomicBoolean(true);

		this.executor = Executors.newCachedThreadPool();
		this.executor.execute(new ActorExecutorDirector());

		Thread.currentThread().setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

			public void uncaughtException(Thread t, Throwable e) {
				System.out.println(e);
			}
		});
	}


	/**
	 * Directs which Actor gets executed and where.  If one actor is stealing
	 * all of the cycles the queued up actors are moved to another queue.
	 *
	 * @author Tony
	 *
	 */
	private class ActorExecutorDirector implements Runnable {

		private Queue<ActorExecutor> availableExecutors;
		private Queue<ActorExecutor> blockedExecutors;

		/**
		 *
		 */
		public ActorExecutorDirector() {
			this.availableExecutors = new ConcurrentLinkedQueue<ActorExecutor>();
			this.blockedExecutors = new ConcurrentLinkedQueue<ActorExecutor>();
		}


		private void checkForBlockedExecutors(List<Actor> workToDisperse) {
			int numberOfTries = 0;

			do {
				Iterator<ActorExecutor> executors = this.availableExecutors.iterator();
				while( executors.hasNext() ) {
					ActorExecutor exe = executors.next();

					long lastCycle = exe.getLastCycleTime();

					/* If this thing is blocked for more than a X amount of time
					 * move it to the blocked queue, shift the other queued up actors else where
					 */
					if ( (System.currentTimeMillis() - lastCycle) > 1000 ) {
						exe.moveToBlock();

						Queue<Actor> queuedActors = exe.getQueuedActors();
						workToDisperse.addAll(queuedActors);

						this.blockedExecutors.add(exe);
						executors.remove();
					}
					else {
						if ( ! workToDisperse.isEmpty() ) {
							exe.addActors(workToDisperse);
							workToDisperse.clear();
						}
					}
				}

				numberOfTries++;

			} while (!workToDisperse.isEmpty() && numberOfTries < 2);
		}

		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		public void run() {

			List<Actor> workToDisperse = new ArrayList<Actor>();
			while ( isOpen.get() ) {
				while( ! actors.isEmpty() ) {
					workToDisperse.add(actors.poll());
				}

				checkForBlockedExecutors(workToDisperse);


				/* All workers are busy, spawn a new worker */
				if ( ! workToDisperse.isEmpty() ) {
					ActorExecutor exe = new ActorExecutor();
					exe.addActors(workToDisperse);
					workToDisperse.clear();

					executor.execute(exe);
				}

				/* Check the Blocked Executors. to see if they are unblocked now */
				Iterator<ActorExecutor> it = this.blockedExecutors.iterator();
				while(it.hasNext()) {
					ActorExecutor exe = it.next();
					long blockedTime = exe.getBlockedTime();
					long lastCycleTime = exe.getLastCycleTime();

					/* This executor is no longer blocked */
					if ( (lastCycleTime - blockedTime) > 1000 ) {
						this.availableExecutors.add(exe);
					}
				}
			}

			for(ActorExecutor exe : this.availableExecutors) {
				exe.kill();
			}

			for(ActorExecutor exe : this.blockedExecutors) {
				exe.kill();
			}
		}

	}

	private class ActorExecutor implements Runnable {

		private Queue<Actor> actors;
		private Queue<Actor> queuedActors;
		private AtomicLong lastCycleTime;
		private AtomicLong blockedTime;
		private AtomicBoolean working;
		private AtomicInteger numberOfActors;

		/**
		 *
		 */
		public ActorExecutor() {
			this.actors = new ConcurrentLinkedQueue<Actor>();
			this.queuedActors = new ConcurrentLinkedQueue<Actor>();
			this.working = new AtomicBoolean(true);

			this.numberOfActors = new AtomicInteger(0);
			this.lastCycleTime = new AtomicLong(0);
			this.blockedTime = new AtomicLong(0);

		}

		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			try {
				while ( working.get() ) {
					this.lastCycleTime.set(System.currentTimeMillis());

					Queue<Actor> runAgainQueue = new LinkedList<Actor>();

					while( ! this.actors.isEmpty() ) {
						Actor actor = this.actors.poll();

						// rebuild the queuedActors
						synchronized (this.queuedActors) {
							this.queuedActors.clear();
							this.queuedActors.addAll(this.actors);
						}

						/* Let the actor have a cycle */
						if ( actor.isAlive() ) {
							actor.tick();

							/* If it's still alive after this,
							 * lets run it again next cycle
							 */
							if ( actor.isAlive() ) {
								runAgainQueue.add(actor);
							}
						}
					}

					this.actors.addAll(runAgainQueue);
				}
			}
			catch(LeolaRuntimeException t) {
				// TODO : Fail over the queued up actors
			}
			catch(Throwable t) {

			}
		}

		public void kill() {
			this.working.set(false);
		}

		public long getLastCycleTime() {
			return this.lastCycleTime.get();
		}


		public void addActors(List<Actor> actors) {
			this.numberOfActors.addAndGet(actors.size());
			this.actors.addAll(actors);
		}

		public long getBlockedTime() {
			return blockedTime.get();
		}

		public void moveToBlock() {
			this.blockedTime.set(System.currentTimeMillis());
		}

		public Queue<Actor> getQueuedActors() {
			synchronized (this.queuedActors) {
				return queuedActors;
			}
		}
	}

	/* (non-Javadoc)
	 * @see leola.vm.lang.actors.ActorPool#awaitShutdown()
	 */
	public void awaitShutdown() {
		this.executor.shutdown();
		// TODO
	}
	
	/* (non-Javadoc)
	 * @see leola.lang.actors.ActorPool#shutdown()
	 */
	public void shutdown() {
		this.isOpen.set(false);

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

	/* (non-Javadoc)
	 * @see leola.vm.lang.actors.ActorPool#postBlockingActor(leola.vm.lang.actors.Actor)
	 */
	@Override
	public Actor postBlockingActor(Actor actor) throws LeolaRuntimeException {
		this.actors.add(actor);
		return actor;
	}
	
	/* (non-Javadoc)
	 * @see leola.vm.lang.actors.ActorPool#postNonBlockingActor(leola.vm.lang.actors.Actor)
	 */
	@Override
	public Actor postNonBlockingActor(Actor actor) throws LeolaRuntimeException {
		this.actors.add(actor);
		return actor;
	}
	
	/* (non-Javadoc)
	 * @see leola.lang.actors.ActorPool#newBlockingActor(leola.types.LeoObject)
	 */
	public final LeoObject newBlockingActor(LeoObject func) throws LeolaRuntimeException {
		final Actor actor = new Actor(new ConcurrentLinkedQueue<LeoObject>(), func, this.leola);
		this.actors.add(actor);

		return func;
	}

	/* (non-Javadoc)
	 * @see leola.lang.actors.ActorPool#newActor(leola.types.LeoObject)
	 */
	public final LeoObject newActor(LeoObject func) throws LeolaRuntimeException {
		final Actor actor = new Actor(new ConcurrentLinkedQueue<LeoObject>(), func, this.leola );
		this.actors.add(actor);

		return func;
	}


}

