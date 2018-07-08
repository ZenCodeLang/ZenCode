package zsynthetic;

@FunctionalInterface
public interface Function10<T, E>  {
    T invoke(E error);
}
