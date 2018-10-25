val outOfScope = 10;
val fun1 = (a as int, b as int) as int => a + b * outOfScope;
val fun2 = (a as int, c as int) as int => 13;



function apply(fn as function(value as int) as int, scale as int) as int {
    return fn(scale);
}


println(fun1(30, 20));
println(fun2(30, 20));




function scale(value as int, scale as int) as int {
    return apply(v => v * scale, value);
}

println("Value: " + scale(10, 5));