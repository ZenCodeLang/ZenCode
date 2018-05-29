/* Licensed under GPLv3 - https://opensource.org/licenses/GPL-3.0 */
package org.openzen.drawablegui.listeners;

import java.util.function.Consumer;

/**
 * Implements a list of event listeners. This class is thread-safe and listeners
 * can be added or removed concurrently, no external locking is ever needed.
 * Also, it's very lightweight.
 *
 * @param <T> event type
 * @author Stan Hebben
 */
public class ListenerList<T>
{
    public static final int PRIORITY_HIGH = 100;
    public static final int PRIORITY_DEFAULT = 0;
    public static final int PRIORITY_LOW = -100;
	
	// implements a linked list of nodes
	protected volatile EventListenerNode first = null;
	protected EventListenerNode last = null;

	/**
	 * Adds an IEventListener to the list.
	 *
	 * @param listener event listener
	 * @return event listener's handle
	 */
	public ListenerHandle<T> add(T listener)
	{
        return add(listener, PRIORITY_DEFAULT);
    }

    public ListenerHandle<T> add(T listener, int priority)
	{
		EventListenerNode node = new EventListenerNode(listener, priority);

		synchronized (this) {
			if (first == null) {
				first = last = node;
			} else {
                // prioritized list: where to insert?
                EventListenerNode previousNode = last;
                while (previousNode != null && priority > previousNode.priority) {
                    previousNode = previousNode.prev;
                }

                if (previousNode == null) {
                    node.next = first;
                    first.prev = previousNode;
                    first = node;
                } else {
                    if (previousNode.next == null) {
                        last = node;
                    } else {
                        previousNode.next.prev = node;
                    }

                    previousNode.next = node;
                    node.prev = previousNode;
                }
            }
		}

		return node;
	}

	/**
	 * Removes an IEventListener from the list.
	 *
	 * @param listener listener to be removed
	 * @return true if the listener was removed, false it it wasn't there
	 */
	public synchronized boolean remove(T listener)
	{
		EventListenerNode current = first;

		while (current != null) {
			if (current.listener == listener) {
				current.close();
				return true;
			}

            current = current.next;
		}

		return false;
	}
	
	/**
	 * Clears this listener list.
	 */
	public synchronized void clear()
	{
		first = last = null;
	}
	
	/**
	 * Invokes the given consumer for every listener in the list.
	 * 
	 * @param consumer consumer to be called for each listener
	 */
	public void accept(Consumer<T> consumer)
	{
		EventListenerNode current = first;
		
		while (current != null) {
			consumer.accept(current.listener);
			current = current.next;
		}
	}

	/**
	 * Checks if there are any listeners in this list.
	 *
	 * @return true if empty
	 */
	public boolean isEmpty()
	{
		return first == null;
	}

	// #######################
	// ### Private classes ###
	// #######################

	protected class EventListenerNode implements ListenerHandle<T>
	{
		protected final T listener;
        protected final int priority;
		protected EventListenerNode next = null;
		protected EventListenerNode prev = null;

		public EventListenerNode(T handler, int priority)
		{
			this.listener = handler;
            this.priority = priority;
		}

		@Override
		public T getListener()
		{
			return listener;
		}

		@Override
		public void close()
		{
			synchronized (ListenerList.this) {
				if (prev == null) {
					first = next;
				} else {
					prev.next = next;
				}

				if (next == null) {
					last = prev;
				} else {
					next.prev = prev;
				}
			}
		}
	}
}
