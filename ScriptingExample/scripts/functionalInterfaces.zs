//val x = ((a as string) => "hello" + a) as function`auto(a as string) as string`auto;
//invokeFunctional(x);

val y = (a as int, b as int) => a + b;


//invokeFunctionalInt((a, b) => a + b);
invokeFunctionalInt(y);


println(((x as int) => x)(10));

//TODO: Globals can't be "captured"
//invokeFunctionalInt((a, b) => {
//	println("a");
//	return a + b;
//});