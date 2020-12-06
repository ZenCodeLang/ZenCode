package zsynthetic;

import stdlib.Result;


@FunctionalInterface
public interface FunctionTToResultWithUV<R, E, T> {
	Result<R, E> invoke(T result);
}
