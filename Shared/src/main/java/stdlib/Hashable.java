package stdlib;

public interface Hashable<T> {
	int hashCode();

	boolean equals_(T other);
}
