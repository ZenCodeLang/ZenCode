package live;

import java.util.function.BiConsumer;
import listeners.ListenerHandle;

public interface LiveString {
    String getValue();
    
    ListenerHandle<BiConsumer<String, String>> addListener(BiConsumer<String, String> listener);
}
