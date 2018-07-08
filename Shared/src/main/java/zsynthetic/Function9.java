package zsynthetic;
import stdlib.Result;


@FunctionalInterface
public interface Function9<T, X, E>  {
    Result<T, X> invoke(E error);
}
