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