package zsynthetic;

@FunctionalInterface
public interface FunctionUSizeTToU<T, U> {
	U invoke(int a, T b);
}
