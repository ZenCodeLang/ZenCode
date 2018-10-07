public variant Result<T, E> {
    Ok(T),
    Error(E),
    Other(T, E);
    
    /*public then<R>(fn as function(result as T) as Result<R, E>) as Result<R, E> {
        return match this {
            Ok(result) => fn(result),
            Error(error) => Error(error),
			Other(result, error) => fn(result)
        };
    }*/
    
    //public handle<X>(handler as function(error as E) as Result<T, X>) as Result<T, X> {
    //    return match this {
    //        Ok(result) => Ok(result),
    //        Error(error) => handler(error)
    //    };
    //}
    
    public expect() as T {
        return match this {
            Ok(result) => result,
            Error(error) => panic "expect() called on an error value",
			Other(result, error) => result
        };
    }
    
    public orElse(other as T) as T {
        return match this {
            Ok(result) => result,
            Error(error) => other,
			Other(result, error) => result
        };
    }
    
    public orElse(other as function(error as E) as T) as T {
        return match this {
            Ok(result) => result,
            Error(error) => other(error),
			Other(result, error) => result
        };
    }
}



function makeResult() as Result<string, string>
    => Ok("10");


function makeErrResult() as Result<string, string>
    => Error("10");


println(makeResult().orElse("Ten"));
println(makeResult().expect());
println(makeErrResult().orElse("Ten"));


//CompileException [TYPE_ARGUMENTS_NOT_INFERRABLE] Could not infer generic type parameters [ParsedExpressionFunction.compile, line 75]
//println(makeResult().then(tValue => Result<string, string>.Ok(tValue)).expect());

//IllegalArgumentException: Cannot retrieve members of undetermined type [TypeMembers.<init>, line 71]
//println(makeResult().then(a => Ok(a)).expect());
//println(makeResult().then(a as string => Ok(a)).expect());

//CompileException [UNEXPECTED_TOKEN] ) expected [LLparserTokenStream.required, line 97]
//Wants to compile a call to function() instead of creating a lambda
//println(makeResult().then((function (t as string) as Result<string, string>)(t => Ok(t))).expect());
