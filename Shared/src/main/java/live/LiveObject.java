package live;

import java.util.function.BiConsumer;
import listeners.ListenerHandle;

public interface LiveObject<T> {
    T getValue();
    
    ListenerHandle<BiConsumer<T, T>> addListener(BiConsumer<T, T> listener);
}
