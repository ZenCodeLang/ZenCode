package zsynthetic;

@FunctionalInterface
public interface FunctionIntTToVoid<T> {
    void invoke(int index, T value);
}
