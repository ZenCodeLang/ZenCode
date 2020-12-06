package live;

import listeners.ListenerHandle;

public interface LiveList<T> extends AutoCloseable, Iterable<T> {
	@Override
	public void close();

	int getLength();

	int indexOf(T value);

	T getAt(int index);

	ListenerHandle<LiveList.Listener<T>> addListener(LiveList.Listener<T> listener);

	public interface Listener<T> {
		void onInserted(int index, T value);

		void onChanged(int index, T oldValue, T newValue);

		void onRemoved(int index, T oldValue);
	}
}
