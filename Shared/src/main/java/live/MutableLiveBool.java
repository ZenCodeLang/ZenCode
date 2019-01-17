package live;

public interface MutableLiveBool extends LiveBool {
    void setValue(boolean value);
    
    default void toggle() {
        this.setValue(!getValue());
    }
}
