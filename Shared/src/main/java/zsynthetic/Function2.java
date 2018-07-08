package zsynthetic;

@FunctionalInterface
public interface Function2<U, T>  {
    U invoke(int index, T value);
}
