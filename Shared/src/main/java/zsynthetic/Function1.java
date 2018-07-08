package zsynthetic;

@FunctionalInterface
public interface Function1<U, T>  {
    U invoke(T value);
}
