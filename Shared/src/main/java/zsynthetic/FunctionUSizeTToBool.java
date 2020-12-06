package zsynthetic;

@FunctionalInterface
public interface FunctionUSizeTToBool<T> {
	boolean invoke(int a, T b);
}
