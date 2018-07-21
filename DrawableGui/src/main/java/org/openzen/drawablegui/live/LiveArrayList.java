/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.live;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.openzen.drawablegui.listeners.ListenerHandle;
import org.openzen.drawablegui.listeners.ListenerList;

/**
 *
 * @author Hoofdgebruiker
 */
public class LiveArrayList<T> implements MutableLiveList<T> {
	private final List<T> values = new ArrayList<>();
	private final ListenerList<Listener<T>> listeners = new ListenerList<>();

	@Override
	public void add(T value) {
		int index = values.size();
		values.add(value);
		listeners.accept(listener -> listener.onInserted(index, value));
	}

	@Override
	public void add(int index, T value) {
		values.add(index, value);
		listeners.accept(listener -> listener.onInserted(index, value));
	}

	@Override
	public void set(int index, T value) {
		T oldValue = values.set(index, value);
		listeners.accept(listener -> listener.onChanged(index, oldValue, value));
	}

	@Override
	public void remove(int index) {
		T oldValue = values.remove(index);
		listeners.accept(listener -> listener.onRemoved(index, oldValue));
	}

	@Override
	public void remove(T value) {
		int index = indexOf(value);
		if (index < 0)
			return;
		
		remove(index);
	}
	
	@Override
	public void clear() {
		for (int i = size() - 1; i >= 0; i--)
			remove(i);
	}

	@Override
	public int indexOf(T value) {
		return values.indexOf(value);
	}
	
	@Override
	public int size() {
		return values.size();
	}

	@Override
	public T get(int index) {
		return values.get(index);
	}
	
	@Override
	public void close() {
		// TODO: close closeables
	}

	@Override
	public Iterator<T> iterator() {
		return values.iterator();
	}

	@Override
	public ListenerHandle<Listener<T>> addListener(Listener<T> listener) {
		return listeners.add(listener);
	}
}
