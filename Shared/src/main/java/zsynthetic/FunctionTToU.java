package zsynthetic;

@FunctionalInterface
public interface FunctionTToU<U, T> {
    U invoke(T value);
}
