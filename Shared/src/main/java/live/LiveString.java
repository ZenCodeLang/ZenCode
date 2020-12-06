package live;

import listeners.ListenerHandle;

import java.util.function.BiConsumer;

public interface LiveString {
	String getValue();

	ListenerHandle<BiConsumer<String, String>> addListener(BiConsumer<String, String> listener);
}
