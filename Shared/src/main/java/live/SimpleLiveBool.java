package live;

import listeners.ListenerHandle;
import listeners.ListenerList;
import zsynthetic.FunctionBoolBoolToVoid;

public final class SimpleLiveBool implements MutableLiveBool {
    private final ListenerList<FunctionBoolBoolToVoid> listeners = new ListenerList<FunctionBoolBoolToVoid>();
    private boolean value;
    
    public SimpleLiveBool(boolean value) {
        this.value = value;
    }
    
    @Override
    public ListenerHandle<FunctionBoolBoolToVoid> addListener(FunctionBoolBoolToVoid listener) {
        return listeners.add(listener);
    }
    
    @Override
    public void setValue(boolean value) {
        if (value == this.value)
            return;
        boolean oldValue = this.value;
        this.value = value;
        listeners.accept(listener -> 
        listener.invoke(oldValue, this.value));
    }
    
    public boolean getValue() {
        return value;
    }
}
