package zsynthetic;

@FunctionalInterface
public interface FunctionUSizeTToVoid<T> {
    void invoke(int index, T value);
}
