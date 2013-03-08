/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.lang.actors;

import leola.vm.exceptions.LeolaRuntimeException;
import leola.vm.types.LeoObject;


/**
 * @author Tony
 *
 */
public interface ActorPool {

	/**
	 * Shuts down the Actor pool
	 */
	public void shutdown();
	
	/**
	 * Awaits the completion of any tasks
	 */
	public void awaitShutdown();

	/**
	 * Creates a new {@link Actor} that blocks and waits for in bound messages.
	 *
	 * @param func
	 * @return
	 * @throws EvalException
	 */
	public LeoObject newBlockingActor(LeoObject func) throws LeolaRuntimeException;

	/**
	 * Creates a new {@link Actor} that does not block or wait for messages.
	 * @param func
	 * @return
	 * @throws EvalException
	 */
	public LeoObject newActor(LeoObject func) throws LeolaRuntimeException;
	
	public Actor postNonBlockingActor(final Actor actor) throws LeolaRuntimeException;
	public Actor postBlockingActor(final Actor actor) throws LeolaRuntimeException;
}

