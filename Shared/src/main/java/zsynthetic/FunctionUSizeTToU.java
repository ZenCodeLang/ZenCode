package zsynthetic;

@FunctionalInterface
public interface FunctionUSizeTToU<U, T> {
    U invoke(int index, T value);
}
