package zsynthetic;

@FunctionalInterface
public interface FunctionTToVoid<T> {
    void invoke(T value);
}
