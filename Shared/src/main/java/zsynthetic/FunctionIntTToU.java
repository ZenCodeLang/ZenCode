package zsynthetic;

@FunctionalInterface
public interface FunctionIntTToU<U, T> {
	U invoke(int index, T value);
}
