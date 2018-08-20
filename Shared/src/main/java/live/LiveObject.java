package live;

import listeners.ListenerHandle;
import zsynthetic.FunctionTTToVoid;

public interface LiveObject<T> {
    T getValue();
    
    ListenerHandle<FunctionTTToVoid<T>> addListener(FunctionTTToVoid<T> listener);
}
