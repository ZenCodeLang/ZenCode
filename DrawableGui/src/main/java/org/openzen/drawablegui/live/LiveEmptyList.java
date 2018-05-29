/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.live;

import java.util.Collections;
import java.util.Iterator;
import org.openzen.drawablegui.listeners.DummyListenerHandle;
import org.openzen.drawablegui.listeners.ListenerHandle;

/**
 *
 * @author Hoofdgebruiker
 */
public class LiveEmptyList<T> implements LiveList<T> {
	private static final LiveEmptyList INSTANCE = new LiveEmptyList();
	
	public static <T> LiveEmptyList<T> get() {
		return INSTANCE;
	}

	@Override
	public void add(T value) {
		throw new UnsupportedOperationException("Immutable list");
	}

	@Override
	public void add(int index, T value) {
		throw new UnsupportedOperationException("Immutable list");
	}

	@Override
	public void set(int index, T value) {
		throw new UnsupportedOperationException("Immutable list");
	}

	@Override
	public void remove(int index) {
		throw new UnsupportedOperationException("Immutable list");
	}

	@Override
	public void remove(T value) {
		throw new UnsupportedOperationException("Immutable list");
	}

	@Override
	public int indexOf(T value) {
		return -1;
	}
	
	@Override
	public int size() {
		return 0;
	}
	
	@Override
	public T get(int index) {
		throw new ArrayIndexOutOfBoundsException(index);
	}

	@Override
	public Iterator<T> iterator() {
		return Collections.emptyIterator();
	}

	@Override
	public ListenerHandle<Listener<T>> addListener(Listener<T> listener) {
		return new DummyListenerHandle<>(listener);
	}
}
