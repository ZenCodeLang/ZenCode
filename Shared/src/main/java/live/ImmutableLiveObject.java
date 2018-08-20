package live;

import listeners.DummyListenerHandle;
import listeners.ListenerHandle;
import zsynthetic.FunctionTTToVoid;

public final class ImmutableLiveObject<T> implements LiveObject<T> {
    public final T value;
    
    public ImmutableLiveObject(T value) {
        this.value = value;
    }
    
    @Override
    public ListenerHandle<FunctionTTToVoid<T>> addListener(FunctionTTToVoid<T> listener) {
        return new DummyListenerHandle<FunctionTTToVoid<T>>(listener);
    }
    
    public T getValue() {
        return value;
    }
}
