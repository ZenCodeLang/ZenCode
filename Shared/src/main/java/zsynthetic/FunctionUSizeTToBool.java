package zsynthetic;

@FunctionalInterface
public interface FunctionUSizeTToBool<T> {
    boolean invoke(int index, T value);
}
