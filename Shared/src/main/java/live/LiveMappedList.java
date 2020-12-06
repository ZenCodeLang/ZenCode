/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package live;

import listeners.ListenerHandle;
import listeners.ListenerList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

// TODO: rewrite to zencode
public class LiveMappedList<T, U> implements AutoCloseable, LiveList<U> {
	private final ListenerList<Listener<U>> listeners = new ListenerList<>();
	private final Function<T, U> projection;
	private final List<U> mapped;
	private final ListenerHandle<Listener<T>> mappingListenerHandle;

	public LiveMappedList(LiveList<T> original, Function<T, U> projection) {
		this.projection = projection;
		mappingListenerHandle = original.addListener(new MappingListener());

		mapped = new ArrayList<>();
		for (T originalItem : original)
			mapped.add(projection.apply(originalItem));
	}

	@Override
	public void close() {
		mappingListenerHandle.close();
	}

	@Override
	public int indexOf(U value) {
		return mapped.indexOf(value);
	}

	@Override
	public int getLength() {
		return mapped.size();
	}

	@Override
	public U getAt(int index) {
		return mapped.get(index);
	}

	@Override
	public Iterator<U> iterator() {
		return mapped.iterator();
	}

	@Override
	public ListenerHandle<Listener<U>> addListener(Listener<U> listener) {
		return listeners.add(listener);
	}

	private class MappingListener implements Listener<T> {
		@Override
		public void onInserted(int index, T value) {
			U mappedValue = projection.apply(value);
			mapped.add(index, mappedValue);
			listeners.accept(listener -> listener.onInserted(index, mappedValue));
		}

		@Override
		public void onChanged(int index, T oldValue, T newValue) {
			U mappedNewValue = projection.apply(newValue);
			U mappedOldValue = mapped.set(index, mappedNewValue);
			listeners.accept(listener -> listener.onChanged(index, mappedOldValue, mappedNewValue));
		}

		@Override
		public void onRemoved(int index, T oldValue) {
			U oldMappedValue = mapped.remove(index);
			if (oldMappedValue instanceof AutoCloseable) {
				try {
					((AutoCloseable) oldMappedValue).close();
				} catch (Exception ex) {
				}
			}
			listeners.accept(listener -> listener.onRemoved(index, oldMappedValue));
		}
	}
}
