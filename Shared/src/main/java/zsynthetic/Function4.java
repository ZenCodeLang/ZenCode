package zsynthetic;

@FunctionalInterface
public interface Function4<T>  {
    boolean invoke(int index, T value);
}
