val a  = new int[](3, 10);

for i in a {
	println(i);
}

println(a[1]);


val multiDim = new int[,,](1,2,3, 130);
println(multiDim[0,1,2]);

val t = multiDim;


val b = new int[](3);
for i in b {
	println(i);
}


val c = new int[,,](1,2,3);
println(c[0,1,2]);