val test as int = 10;
val tt1 = "tt";
val tt2 = "tt3";
val tt4 = "tt5";


println(match 10 {
	1 => "one",
	10 => "yo",
	//tt1 === "tt1" for some compiler issue it takes the variable as string input?
	100 => match tt1 {
		"10" => "t",
		"tt1" => tt1,
		"tt2" => tt1 + tt4,
		default => tt4
	},
	default => tt2
});


println(tt1);

//println(match test {
//	case 1 : "tt",
//	default : "kk"
//});



function myFunc (par1 as int) as void {

    val v0 = par1 - 1;
	println(match par1 {
		10 => v0,
		default => match(v0) {
			10 => 99,
			default => v0
		}
	});
}


myFunc(10);
myFunc(11);
myFunc(12);


val t = (a as int) as int => a;


println(t(10));