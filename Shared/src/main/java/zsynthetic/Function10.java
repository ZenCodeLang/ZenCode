package zsynthetic;
import stdlib.Result;


@FunctionalInterface
public interface Function10<T, X, E>  {
    Result<T, X> invoke(E error);
}
