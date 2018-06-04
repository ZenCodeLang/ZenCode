/* Licensed under GPLv3 - https://opensource.org/licenses/GPL-3.0 */
package org.openzen.drawablegui.listeners;

import java.io.Closeable;

/**
 * An EvenListenerHandle is returned when an EventListener is registered in an
 * EventListenerList, which can then be used to unregister the event listener.
 *
 * @param <T> Event type
 * @author Stan Hebben
 */
public interface ListenerHandle<T> extends Closeable
{
	/**
	 * Gets this handle's listener.
	 *
	 * @return listener
	 */
	public T getListener();

	/**
	 * Closes (unregisters) the event listener from the list it was registered to.
	 */
	@Override
	public void close();
}
