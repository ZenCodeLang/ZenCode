val outOfScope = 10;

val fun = (a as int, b as int) as int => a + b * outOfScope;
println(fun(30, 20));

function apply_fn2(fn as function(value as int, othervalue as int) as int, value as int) as int {
    return fn(value, value);
}

//function scale(value as int, scale as int) as int {

//	val fun as function(value as int, othervalue as int) as int = ((v as int) as int => v * scale);
//    return apply(fun, scale);
//}

apply_fn2(fun, 10);

//println("Value: " + scale(10, 5));

