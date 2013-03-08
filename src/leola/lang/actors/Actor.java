/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.lang.actors;

import static leola.vm.util.ClassUtil.getMethodByName;

import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import leola.frontend.EvalException;
import leola.vm.Leola;
import leola.vm.exceptions.LeolaRuntimeException;
import leola.vm.types.LeoNativeFunction;
import leola.vm.types.LeoNull;
import leola.vm.types.LeoObject;
import leola.vm.types.LeoScopedObject;
import leola.vm.types.LeoString;


/**
 * @author Tony
 *
 */
public class Actor {

	/**
	 * The functor
	 */
	private LeoObject obj;


	/**
	 * Messages
	 */
	private Queue<LeoObject> messageBox;

	/**
	 * If its still active
	 */
	private AtomicBoolean isAlive;

	/**
	 * The future
	 */
	private Future<?> future;

	private Leola runtime;

	/**
	 * @param obj
	 * @param queue
	 */
	public Actor(Queue<LeoObject> queue, LeoObject obj, Leola runtime) {
		if ( obj == null ) {
			throw new IllegalArgumentException("The supplied actor type can not be null!");
		}

		this.obj = obj;
		this.runtime = runtime;

		this.messageBox = queue;
		this.isAlive = new AtomicBoolean(true);

		/*
		 * Infuse with the actor traits
		 */
		switch(obj.getType()) {
			case CLASS:
			case MAP:
			case NATIVE_CLASS: {
				LeoScopedObject sObj = obj.as();
				sObj.addProperty(LeoString.valueOf("kill"), new LeoNativeFunction(getMethodByName(Actor.class, "kill"), this));
				sObj.addProperty(LeoString.valueOf("waitAndKill"), new LeoNativeFunction(getMethodByName(Actor.class, "waitAndKill"), this));
				sObj.addProperty(LeoString.valueOf("send"), new LeoNativeFunction(getMethodByName(Actor.class, "send", LeoObject.class), this));
				sObj.addProperty(LeoString.valueOf("isAlive"), new LeoNativeFunction(getMethodByName(Actor.class, "isAlive"), this));
								
				if ( ! sObj.hasProperty(LeoString.valueOf("act")) ) {
					throw new IllegalArgumentException("The supplied object: " + obj + " does not have an 'act' method!");
				}

				break;
			}
		}
	}

	/**
	 * @return the messageBox
	 */
	protected Queue<LeoObject> getMessageBox() {
		return messageBox;
	}
	
	/**
	 * @return the functor object
	 */
	protected LeoObject getFunctor() {
		return obj;
	}
	
	/**
	 * @return the runtime
	 */
	public Leola getRuntime() {
		return runtime;
	}

	/**
	 * Sets the future
	 * @param f
	 */
	void setFuture(Future<?> f) {
		this.future = f;
	}

	/**
	 * Kills this Actor
	 */
	public void kill() {
		this.isAlive.set(false);
		if ( this.future != null ) {
			this.future.cancel(true);
		}
	}

	/**
	 * Waits until this actor is finished with its current work load
	 * and then terminates
	 */
	public void waitAndKill() {
		this.isAlive.set(false);
		if ( this.future != null ) {
			try {
				this.future.get();				
			} catch (InterruptedException e) {
			} catch (ExecutionException e) {
				throw new LeolaRuntimeException(e);
			}
		}
	}
	
	/**
	 * Determines this this Actor is still alive
	 * @return true if still alive
	 */
	public boolean isAlive() {
		return this.isAlive.get();
	}

	/**
	 * Sends this Actor a message
	 *
	 * @param msg
	 */
	public void send(LeoObject msg) {
		if ( this.isAlive() ) {
			LeoObject clonedMsg = msg==null ? LeoNull.LEONULL : msg;
			this.messageBox.add(clonedMsg);
		}
	}


	/**
	 * One actor tick means it is given a cycle tick on the thread
	 * pool.
	 *
	 * @throws EvalException
	 */
	public void tick() throws LeolaRuntimeException {
		if ( this.isAlive() ) {
			LeoObject msg = this.messageBox.poll();
			if ( msg != null ) {
				invoke(msg);
			}
		}
	}

	/**
	 * React to a received message.
	 *
	 * @param interp
	 * @throws EvalException
	 */
	public void react() throws LeolaRuntimeException {
		while( this.isAlive() ) {
			LeoObject msg = this.messageBox.poll();
			if ( msg != null ) {
				invoke(msg);
			}

			Thread.yield();

//			try {
//				Thread.sleep(100);
//			} catch (InterruptedException e) {
//				return;
//			}

		}
	}

	/**
	 * Acts, doesn't read the in bound messages
	 *
	 * @param interp
	 * @throws EvalException
	 */
	public void act() throws LeolaRuntimeException {
		invoke(null);

		// mark as complete (we don't want to kill it because
		// it could potentially for an interrupt
		this.isAlive.set(false);
	}

	/**
	 * Invokes the functor.
	 *
	 * @param interp
	 * @param msg
	 * @throws EvalException
	 */
	protected void invoke(LeoObject msg) throws LeolaRuntimeException {
		invoke(this.obj, msg);
	}

	/**
	 * Invokes the method or searches for the "act" functor on the supplied object.
	 * @param interp
	 * @param obj
	 * @param msg
	 * @throws EvalException
	 */
	protected void invoke(LeoObject obj, LeoObject msg) throws LeolaRuntimeException {
		if ( obj != null ) {
			switch(obj.getType()) {
				case FUNCTION: 
				case NATIVE_FUNCTION: {
					if (msg==null) this.runtime.execute(obj);
					else this.runtime.execute(obj, msg);
					break;
				}
				case CLASS:
				case MAP:
				case NATIVE_CLASS: {


					LeoScopedObject sObj = obj.as();
					LeoObject functor = sObj.getProperty(LeoString.valueOf("act"));
					if ( functor == null ) {
						throw new LeolaRuntimeException("No 'act' method found in object: " + obj);
					}

					invoke(functor, msg);
					break;
				}
			}
		}
	}
}

