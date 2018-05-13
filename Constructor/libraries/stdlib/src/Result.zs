export variant Result<T, E> {
	Success(T),
	Failure(E);
	
	public then<R>(fn as function(result as T) as R) as Result<R, E> {
		return match this {
			Success(result) => Success(fn(result)),
			Failure(error) => Failure(error)
		};
	}
	
	public handle<X>(handler as function(error as E) as Result<T, X>) as Result<T, X> {
		return match this {
			Success(result) => Success(result),
			Failure(error) => handler(error)
		};
	}
	
	public expect() as T {
		return match this {
			Success(result) => result,
			Failure(error) => panic<T>("demand with error value")
		};
	}
}

export expand <T, E : Exception> Result<T, E> {
	public unwrap() as T {
		return match this {
			Success(result) => result,
			Failure(error) => { throw error; }
		};
	}
}
