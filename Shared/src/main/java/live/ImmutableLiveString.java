package live;

import java.util.function.BiConsumer;
import listeners.DummyListenerHandle;
import listeners.ListenerHandle;

public final class ImmutableLiveString implements LiveString {
    public final String value;
    
    public ImmutableLiveString(String value) {
        this.value = value;
    }
    
    @Override
    public ListenerHandle<BiConsumer<String, String>> addListener(BiConsumer<String, String> listener) {
        return new DummyListenerHandle<BiConsumer<String, String>>(listener);
    }
    
    public String getValue() {
        return value;
    }
}
