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


val aSomeArray = new string[](5, "HelloWorld");
val bSomeArray = new string[]<string>(aSomeArray, projection);
println("HelloWorldProjectedArray");
println(bSomeArray[1]);

println(e[2,3,4]);


val constructorLambdaArray = new string[](5, i => "No" + i);
println(constructorLambdaArray[1]);

val constructorLambdaArrayMulti = new string[,](5, 5, (i1, i2) => "No" + i1 + i2);
println(constructorLambdaArrayMulti[1, 2]);


val testArray = new string[,](5, 5, "helloWorld");

val indexedProjectionWithLambdaNonInlined = new string[,]<string>(testArray as string[,], (index1, index2, value) => {
    return value + "" + index1 + index2;
} as function(index1 as usize, index2 as usize, value as string`borrow) as string);

val indexedProjectionWithLambdaInlined = new string[,]<string>(testArray, ((i as usize, j as usize, s as string`borrow) => (s + "" + i + j) as string) as function(i as usize, j as usize, s as string`borrow) as string);

println(indexedProjectionWithLambdaNonInlined[1, 2]);
println(indexedProjectionWithLambdaInlined[1, 2]);