package live;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import listeners.ListenerHandle;
import listeners.ListenerList;

public final class LiveArrayList<T> implements MutableLiveList<T>, AutoCloseable {
    private final List<T> values = new ArrayList<T>();
    private final ListenerList<LiveList.Listener<T>> listeners = new ListenerList<LiveList.Listener<T>>();
    
    @Override
    public void add(T value) {
        int index = values.size();
        values.add(value);
        listeners.accept(listener -> 
        listener.onInserted(index, value));
    }
    
    @Override
    public void insert(int index, T value) {
        values.add(index, value);
        listeners.accept(listener -> 
        listener.onInserted(index, value));
    }
    
    @Override
    public void setAt(int index, T value) {
        T oldValue = values.get(index);
        values.set(index, value);
        listeners.accept(listener -> 
        listener.onChanged(index, oldValue, value));
    }
    
    @Override
    public void remove(int index) {
        T oldValue = values.remove(index);
        listeners.accept(listener -> 
        listener.onRemoved(index, oldValue));
    }
    
    @Override
    public void remove(T value) {
        int index = this.indexOf(value);
        if (index < 0)
            return;
        this.remove(index);
    }
    
    @Override
    public void clear() {
        int i = getLength();
        while (i > 0) {
            i--;
            this.remove(i);
        }
    }
    
    @Override
    public Iterator<T> iterator() {
        return values.iterator();
    }
    
    @Override
    public int indexOf(T value) {
        return values.indexOf(value);
    }
    
    @Override
    public int getLength() {
        return values.size();
    }
    
    @Override
    public T getAt(int index) {
        return values.get(index);
    }
    
    @Override
    public ListenerHandle<LiveList.Listener<T>> addListener(LiveList.Listener<T> listener) {
        return listeners.add(listener);
    }
    
    @Override
    public void close() {
    }
}
