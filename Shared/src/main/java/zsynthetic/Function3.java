package zsynthetic;

@FunctionalInterface
public interface Function3<T>  {
    boolean invoke(T value);
}
