package live;

import listeners.ListenerHandle;
import zsynthetic.FunctionStringStringToVoid;

public interface LiveString {
    String getValue();
    
    ListenerHandle<FunctionStringStringToVoid> addListener(FunctionStringStringToVoid listener);
}
