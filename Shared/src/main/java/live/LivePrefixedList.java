package live;

import java.util.Iterator;
import listeners.ListenerHandle;
import listeners.ListenerList;

// TODO: convert to zencode
public class LivePrefixedList<T> implements LiveList<T> {
	private final ListenerList<Listener<T>> listeners = new ListenerList<>();
	private final T prefix;
	private final LiveList<T> values;
	private final ListenerHandle<LiveList.Listener<T>> baseListener;
	
	public LivePrefixedList(T prefix, LiveList<T> values) {
		this.prefix = prefix;
		this.values = values;
		this.baseListener = values.addListener(new BaseListener());
	}
	
	@Override
	public void close() {
		baseListener.close();
	}

	/*@Override
	public void add(T value) {
		values.add(value);
	}

	@Override
	public void add(int index, T value) {
		if (index == 0)
			throw new UnsupportedOperationException("Cannot add an item at the beginning of a prefixed list");
		values.add(index - 1, value);
	}

	@Override
	public void set(int index, T value) {
		if (index == 0)
			throw new UnsupportedOperationException("Cannot change the item at the beginning of a prefixed list");
		
		values.set(index - 1, value);
	}

	@Override
	public void remove(int index) {
		if (index == 0)
			throw new UnsupportedOperationException("Cannot remote the item at the beginning of a prefixed list");
		
		values.remove(index - 1);
	}

	@Override
	public void remove(T value) {
		values.remove(value);
	}*/

	@Override
	public int indexOf(T value) {
		if (value.equals(prefix))
			return 0;
		
		int base = values.indexOf(value);
		return base < 0 ? base : base - 1;
	}

	@Override
	public int getLength() {
		return values.getLength() + 1;
	}
	
	@Override
	public T getAt(int index) {
		return index == 0 ? prefix : values.getAt(index);
	}

	@Override
	public Iterator<T> iterator() {
		return new PrefixIterator();
	}

	@Override
	public ListenerHandle<Listener<T>> addListener(Listener<T> listener) {
		return listeners.add(listener);
	}
	
	private class PrefixIterator implements Iterator<T> {
		private boolean first = true;
		private Iterator<T> others = values.iterator();

		@Override
		public boolean hasNext() {
			return first || others.hasNext();
		}

		@Override
		public T next() {
			if (first) {
				first = false;
				return prefix;
			} else {
				return others.next();
			}
		}
	}
	
	private class BaseListener implements Listener<T> {
		@Override
		public void onInserted(int index, T value) {
			listeners.accept(listener -> listener.onInserted(index + 1, value));
		}

		@Override
		public void onChanged(int index, T oldValue, T newValue) {
			listeners.accept(listener -> listener.onChanged(index + 1, oldValue, newValue));
		}

		@Override
		public void onRemoved(int index, T oldValue) {
			listeners.accept(listener -> listener.onRemoved(index + 1, oldValue));
		}
	}
}
