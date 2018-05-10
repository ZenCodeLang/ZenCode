val ternaryOperation = true ? 100 : 222;

println(ternaryOperation);



var coalesce_one as string? = null;
var coalesce_tow as string? = "test";


var coalesce = coalesce_one ?? coalesce_tow;

println(coalesce);


//if(coalesce == "test123") {
//	println("true");
//}

if(1 == 1) {
	println("intCompareTrue");
}

//if(1 == "1") {
//	println("well...");
//}


if("1" == 1) {
	println("...");
}


//var coco as int? = 10;
//var coal as int? = 1;
//
//println(coal ?? coco);


println(".....");
println(true ? "RR" : "TT");


println((false && true && true) ? "true" : "false");
println((true && true) ? "true" : "false");
println((true && false) ? "true" : "false");
println((false && false) ? "true" : "false");

println("---");

println((false || true) ? "true" : "false");
println((true || true) ? "true" : "false");
println((true || false) ? "true" : "false");
println((false || false) ? "true" : "false");

println(":::");

println((1 <= 2) ? "true" : "false");