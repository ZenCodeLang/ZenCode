package zsynthetic;

@FunctionalInterface
public interface FunctionIntTToBool<T> {
	boolean invoke(int index, T value);
}
