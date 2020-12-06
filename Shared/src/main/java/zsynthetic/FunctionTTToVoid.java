package zsynthetic;

@FunctionalInterface
public interface FunctionTTToVoid<T> {
	void invoke(T oldValue, T newValue);
}
