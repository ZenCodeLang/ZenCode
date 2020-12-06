/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package live;

import listeners.ListenerHandle;
import listeners.ListenerList;

import java.util.*;

/**
 * @author Hoofdgebruiker
 */
public class SortedLiveList<T> implements LiveList<T>, LiveList.Listener<T> {
	private final LiveList<T> original;
	private final Comparator<T> ordering;
	private final List<T> sorted;
	private final ListenerHandle<LiveList.Listener<T>> originalListener;
	private final ListenerList<Listener<T>> listeners = new ListenerList<>();

	public SortedLiveList(LiveList<T> original, Comparator<T> ordering) {
		this.original = original;
		this.ordering = ordering;

		sorted = new ArrayList<>();
		for (T item : original)
			sorted.add(item);

		Collections.sort(sorted, ordering);
		originalListener = original.addListener(this);
	}

	@Override
	public void close() {
		original.close();
		originalListener.close();
	}

	@Override
	public int indexOf(T value) {
		return sorted.indexOf(value);
	}

	@Override
	public int getLength() {
		return sorted.size();
	}

	@Override
	public T getAt(int index) {
		return sorted.get(index);
	}

	@Override
	public Iterator<T> iterator() {
		return sorted.iterator();
	}

	@Override
	public ListenerHandle<Listener<T>> addListener(Listener<T> listener) {
		return listeners.add(listener);
	}

	@Override
	public void onInserted(int index, T value) {
		internalAdd(value);
	}

	@Override
	public void onChanged(int index, T oldValue, T newValue) {
		internalRemove(oldValue);
		internalAdd(newValue);
	}

	@Override
	public void onRemoved(int index, T oldValue) {
		internalRemove(oldValue);
	}

	private void internalAdd(T value) {
		int atIndex = 0;
		while (atIndex < sorted.size() && ordering.compare(value, sorted.get(atIndex)) > 0)
			atIndex++;

		sorted.add(atIndex, value);
		final int finalAtIndex = atIndex;
		listeners.accept(listener -> listener.onInserted(finalAtIndex, value));
	}

	private void internalRemove(T value) {
		int index = sorted.indexOf(value);
		if (index < 0)
			return;

		sorted.remove(index);
		listeners.accept(listener -> listener.onRemoved(index, value));
	}
}
