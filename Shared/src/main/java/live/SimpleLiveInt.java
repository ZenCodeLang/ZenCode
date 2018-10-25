package live;

import listeners.ListenerHandle;
import listeners.ListenerList;
import zsynthetic.FunctionIntIntToVoid;

public final class SimpleLiveInt implements MutableLiveInt {
    private final ListenerList<FunctionIntIntToVoid> listeners = new ListenerList<FunctionIntIntToVoid>();
    private int value;
    
    public SimpleLiveInt(int value) {
        this.value = value;
    }
    
    @Override
    public ListenerHandle<FunctionIntIntToVoid> addListener(FunctionIntIntToVoid listener) {
        return listeners.add(listener);
    }
    
    @Override
    public void setValue(int value) {
        if (value == this.value)
            return;
        int oldValue = this.value;
        this.value = value;
        listeners.accept(listener -> 
        listener.invoke(oldValue, this.value));
    }
    
    public int getValue() {
        return value;
    }
}
