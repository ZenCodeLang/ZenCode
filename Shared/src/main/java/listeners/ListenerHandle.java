package listeners;


public interface ListenerHandle<T> extends AutoCloseable {
    @Override
    public void close();
    
    T getListener();
}
