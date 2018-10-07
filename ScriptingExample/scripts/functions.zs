function test() as void {
	println("functions.zs; test1");
}



function test2() as void {
	println("functions.zs; test2");
}


test();
test2();
println(test3(1, 3));


function test3(a as int, b as int) as int{
	println(a+b);
	return a + b;
}
