package live;

import java.util.function.BiConsumer;
import listeners.DummyListenerHandle;
import listeners.ListenerHandle;

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
