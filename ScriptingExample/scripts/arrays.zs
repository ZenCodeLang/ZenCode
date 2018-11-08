//val a  = new int[](3, 10);
//
//for i in a {
//	println(i);
//}
//
//println(a[1]);
////
////
val multiDim = new int[,,](1,2,3, 130);
println(multiDim[0,1,2]);

val t = multiDim;
//
//
val b = new int[](3);
for i in b {
	println(i);
}


val c = new int[,,](1,2,3);
println(c[0,1,2]);
//
//
//val d = new string[,,](5,5,5, "HelloWorld");
//println(d[2,2,2]);
//
//
////val e = new int[](a, value => value);
//
//
//var projection = (value => value) as function(value as string`borrow) as string;
//val e = new string[](5, "HelloWorld");
//val f = new string[]<string>(e, projection);
//var projection = (value => value) as function(value as string`borrow) as string;
//val a = new string[](5, "HelloWorld");
//val b = new string[]<string>(a, projection);

val d = new string[,,](3,4,5, "HelloWorld");

val someString = "someString";

var projection = (value => "" + value) as function(value as string`borrow) as string;
val e = new string[,,]<string>(
    d, 
    (value => "137" + value + someString) as function(value as string`borrow) as string
);


//val a = new string[](5, "HelloWorld");
//val b = new string[]<string>(a, projection);

println(e[2,3,4]);