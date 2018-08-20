package live;


public interface MutableLiveList<T> extends AutoCloseable, LiveList<T> {
    void add(T value);
    
    void insert(int index, T value);
    
    void setAt(int index, T value);
    
    void remove(int index);
    
    void remove(T value);
    
    void clear();
    
    @Override
    public void close();
}
