package listeners;


public final class DummyListenerHandle<T> implements ListenerHandle<T>, AutoCloseable {
    public final T listener;
    
    public DummyListenerHandle(T listener) {
        this.listener = listener;
    }
    
    @Override
    public void close() {
    }
    
    public T getListener() {
        return listener;
    }
}
