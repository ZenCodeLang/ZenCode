package zsynthetic;

@FunctionalInterface
public interface FunctionTToBool<T> {
	boolean invoke(T value);
}
