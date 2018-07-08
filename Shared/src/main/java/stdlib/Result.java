package stdlib;

import stdlib.Result;
import stdlib.Result.Error;
import stdlib.Result.Ok;
import zsynthetic.Function10;
import zsynthetic.Function8;
import zsynthetic.Function9;

public abstract class Result<T, E> {
	public <R> Result<R, E> then(Class<R> typeOfR, Function8<R, E, T> fn) {
	    Result<R, E> temp1;
	    switch (this.getDiscriminant()) {
	        case Ok:
	            T result = ((Result.Ok<T, E>)this).value;
	            temp1 = fn.invoke(result);
	            break;
	        case Error:
	            E error = ((Result.Error<T, E>)this).value;
	            temp1 = new Error(error);
	            break;
	        default:
	            throw new AssertionError("Missing case");
	    }
	    return temp1;
	}
	
	public <X> Result<T, X> handle(Class<X> typeOfX, Function9<T, X, E> handler) {
	    Result<T, X> temp1;
	    switch (this.getDiscriminant()) {
	        case Ok:
	            T result = ((Result.Ok<T, E>)this).value;
	            temp1 = new Ok(result);
	            break;
	        case Error:
	            E error = ((Result.Error<T, E>)this).value;
	            temp1 = handler.invoke(error);
	            break;
	        default:
	            throw new AssertionError("Missing case");
	    }
	    return temp1;
	}
	
	public T expect() {
	    T temp1;
	    switch (this.getDiscriminant()) {
	        case Ok:
	            T result = ((Result.Ok<T, E>)this).value;
	            temp1 = result;
	            break;
	        case Error:
	            E error = ((Result.Error<T, E>)this).value;
	            throw new AssertionError("expect() called on an error value");
	        default:
	            throw new AssertionError("Missing case");
	    }
	    return temp1;
	}
	
	public T orElse(T other) {
	    T temp1;
	    switch (this.getDiscriminant()) {
	        case Ok:
	            T result = ((Result.Ok<T, E>)this).value;
	            temp1 = result;
	            break;
	        case Error:
	            E error = ((Result.Error<T, E>)this).value;
	            temp1 = other;
	            break;
	        default:
	            throw new AssertionError("Missing case");
	    }
	    return temp1;
	}
	
	public T orElse(Function10<T, E> other) {
	    T temp1;
	    switch (this.getDiscriminant()) {
	        case Ok:
	            T result = ((Result.Ok<T, E>)this).value;
	            temp1 = result;
	            break;
	        case Error:
	            E error = ((Result.Error<T, E>)this).value;
	            temp1 = other.invoke(error);
	            break;
	        default:
	            throw new AssertionError("Missing case");
	    }
	    return temp1;
	}
    public abstract Discriminant getDiscriminant();
    
    public static enum Discriminant {
        Ok,
        Error,
    }
    
    public static class Ok<T, E>  extends Result<T, E> {
        public final T value;
        
        public Ok(T value){
            this.value = value;
        }
        
        @Override
        public Discriminant getDiscriminant() {
            return Discriminant.Ok;
        }
    }
    
    public static class Error<T, E>  extends Result<T, E> {
        public final E value;
        
        public Error(E value){
            this.value = value;
        }
        
        @Override
        public Discriminant getDiscriminant() {
            return Discriminant.Error;
        }
    }
	public static <T, E extends Exception> T unwrap(Class<T> typeOfT, Class<E> typeOfE, Result<T, E> self) throws E {
	    T temp1;
	    switch (self.getDiscriminant()) {
	        case Ok:
	            T result = ((Result.Ok<T, E>)self).value;
	            temp1 = result;
	            break;
	        case Error:
	            E error = ((Result.Error<T, E>)self).value;
	            throw error;
	        default:
	            throw new AssertionError("Missing case");
	    }
	    return temp1;
	}
}
