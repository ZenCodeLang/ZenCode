package live;

import listeners.ListenerHandle;
import listeners.ListenerList;

import java.util.function.BiConsumer;

// TODO: write in ZenCode
public class SimpleLiveObject<T> implements MutableLiveObject<T> {
	private final ListenerList<BiConsumer<T, T>> listeners = new ListenerList<>();
	private T value;

	public SimpleLiveObject(T value) {
		this.value = value;
	}

	@Override
	public T getValue() {
		return value;
	}

	@Override
	public void setValue(T value) {
		if (value == this.value)
			return;

		T oldValue = this.value;
		this.value = value;
		listeners.accept(listener -> listener.accept(oldValue, value));
	}

	@Override
	public ListenerHandle<BiConsumer<T, T>> addListener(BiConsumer<T, T> listener) {
		return listeners.add(listener);
	}
}
