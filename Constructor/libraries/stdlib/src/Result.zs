export variant Result<T, E> {
	Ok(T),
	Error(E);
	
	public then<R>(fn as function(result as T) as R) as Result<R, E> {
		return match this {
			Ok(result) => Ok(fn(result)),
			Error(error) => Error(error)
		};
	}
	
	public handle<X>(handler as function(error as E) as Result<T, X>) as Result<T, X> {
		return match this {
			Ok(result) => Ok(result),
			Error(error) => handler(error)
		};
	}
	
	public expect() as T {
		return match this {
			Ok(result) => result,
			Error(error) => panic<T>("demand with error value")
		};
	}
	
	public orElse(other as T) as T {
		return match this {
			Ok(result) => result,
			Error(error) => other
		};
	}
	
	public orElse(other as function(error as E) as T) as T {
		return match this {
			Ok(result) => result,
			Error(error) => other(error)
		};
	}
}

export expand <T, E : Exception> Result<T, E> {
	public unwrap() as T {
		return match this {
			Ok(result) => result,
			Error(error) => { throw error; }
		};
	}
}
