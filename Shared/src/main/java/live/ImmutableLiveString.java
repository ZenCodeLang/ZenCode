package live;

import listeners.DummyListenerHandle;
import listeners.ListenerHandle;
import zsynthetic.FunctionStringStringToVoid;

public final class ImmutableLiveString implements LiveString {
    public final String value;
    
    public ImmutableLiveString(String value) {
        this.value = value;
    }
    
    @Override
    public ListenerHandle<FunctionStringStringToVoid> addListener(FunctionStringStringToVoid listener) {
        return new DummyListenerHandle<FunctionStringStringToVoid>(listener);
    }
    
    public String getValue() {
        return value;
    }
}
