/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package live;

import listeners.DummyListenerHandle;
import listeners.ListenerHandle;

import java.util.Collections;
import java.util.Iterator;

/**
 * @author Hoofdgebruiker
 */
public class LiveEmptyList<T> implements LiveList<T> {
	private static final LiveEmptyList INSTANCE = new LiveEmptyList();

	public static <T> LiveEmptyList<T> get() {
		return INSTANCE;
	}

	@Override
	public void close() {

	}

	@Override
	public int indexOf(T value) {
		return -1;
	}

	@Override
	public int getLength() {
		return 0;
	}

	@Override
	public T getAt(int index) {
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
