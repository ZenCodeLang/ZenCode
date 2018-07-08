package zsynthetic;

@FunctionalInterface
public interface Function11<T, E>  {
    T invoke(E error);
}
