/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.frontend.events;

import leola.frontend.listener.EventListener;
import leola.frontend.listener.EventMethod;

/**
 * @author Tony
 *
 */
public interface SyntaxErrorListener extends EventListener {

    @EventMethod
    public void onEvent(SyntaxErrorEvent event);
}

