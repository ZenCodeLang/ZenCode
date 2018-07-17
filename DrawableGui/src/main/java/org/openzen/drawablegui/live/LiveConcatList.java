/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.live;

import java.util.Iterator;
import org.openzen.drawablegui.Destructible;
import org.openzen.drawablegui.listeners.ListenerHandle;
import org.openzen.drawablegui.listeners.ListenerList;

/**
 *
 * @author Hoofdgebruiker
 */
public class LiveConcatList<T> implements Destructible, LiveList<T> {
	private final ListenerList<Listener<T>> listeners = new ListenerList<>();
	private final LiveList<T> a;
	private final LiveList<T> b;
	
	private final ListenerHandle<Listener<T>> aListener;
	private final ListenerHandle<Listener<T>> bListener;
	
	public LiveConcatList(LiveList<T> a, LiveList<T> b) {
		this.a = a;
		this.b = b;
		
		aListener = a.addListener(new FirstListListener());
		bListener = b.addListener(new SecondListListener());
	}
	
	@Override
	public void close() {
		aListener.close();
		bListener.close();
	}

	@Override
	public int indexOf(T value) {
		int result = a.indexOf(value);
		if (result >= 0)
			return result;
		
		result = b.indexOf(value);
		return result < 0 ? -1 : result + a.size();
	}

	@Override
	public int size() {
		return a.size() + b.size();
	}

	@Override
	public T get(int index) {
		return index < a.size() ? a.get(index) : b.get(index - a.size());
	}

	@Override
	public Iterator<T> iterator() {
		return new ConcatIterator();
	}

	@Override
	public ListenerHandle<Listener<T>> addListener(Listener<T> listener) {
		return listeners.add(listener);
	}
	
	private class ConcatIterator implements Iterator<T> {
		private boolean firstList = true;
		private Iterator<T> iterator;

		public ConcatIterator() {
			iterator = a.iterator();
			if (!iterator.hasNext()) {
				firstList = false;
				iterator = b.iterator();
			}
		}
		
		@Override
		public boolean hasNext() {
			return iterator.hasNext();
		}

		@Override
		public T next() {
			T result = iterator.next();
			if (firstList && !iterator.hasNext()) {
				firstList = false;
				iterator = b.iterator();
			}
			return result;
		}
	}
	
	private class FirstListListener implements Listener<T> {
		@Override
		public void onInserted(int index, T value) {
			listeners.accept(listener -> listener.onInserted(index, value));
		}

		@Override
		public void onChanged(int index, T oldValue, T newValue) {
			listeners.accept(listener -> listener.onChanged(index, oldValue, newValue));
		}

		@Override
		public void onRemoved(int index, T oldValue) {
			listeners.accept(listener -> listener.onRemoved(index, oldValue));
		}
	}
	
	private class SecondListListener implements Listener<T> {
		@Override
		public void onInserted(int index, T value) {
			listeners.accept(listener -> listener.onInserted(a.size() + index, value));
		}

		@Override
		public void onChanged(int index, T oldValue, T newValue) {
			listeners.accept(listener -> listener.onChanged(a.size() + index, oldValue, newValue));
		}

		@Override
		public void onRemoved(int index, T oldValue) {
			listeners.accept(listener -> listener.onRemoved(a.size() + index, oldValue));
		}
	}
}
