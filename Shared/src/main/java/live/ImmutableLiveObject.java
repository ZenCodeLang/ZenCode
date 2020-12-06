package live;

import listeners.DummyListenerHandle;
import listeners.ListenerHandle;

import java.util.function.BiConsumer;

public final class ImmutableLiveObject<T> implements LiveObject<T> {
	public final T value;

	public ImmutableLiveObject(T value) {
		this.value = value;
	}

	@Override
	public ListenerHandle<BiConsumer<T, T>> addListener(BiConsumer<T, T> listener) {
		return new DummyListenerHandle<BiConsumer<T, T>>(listener);
	}

	public T getValue() {
		return value;
	}
}
