package zsynthetic;

@FunctionalInterface
public interface Function8<W, V>  {
    W invoke(V value);
}
