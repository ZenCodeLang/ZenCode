package live;

import listeners.ListenerHandle;

import java.util.function.BiConsumer;

public interface LiveObject<T> {
	T getValue();

	ListenerHandle<BiConsumer<T, T>> addListener(BiConsumer<T, T> listener);
}
