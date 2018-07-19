package zsynthetic;
import stdlib.Result;


@FunctionalInterface
public interface Function9<R, E, T>  {
    Result<R, E> invoke(T result);
}
