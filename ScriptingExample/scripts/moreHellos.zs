println("Hello world!");
println(1 as string);


var test = "test";
println(test);


test = "testMore";
println(test);

test = 13;
println(test);

val test2 = 14;
println(test2);


if true 
    println("ifTest");
else
    println("elseTest");


if false 
    println("testIf");
else
    println("testElse");


if (true) {

    while :testLable true {
        println("trueee");
        if true
            break testLable;
        else
            println("nobreak");
    }
    
    
    do {
        println("tru"); 
        if(false){
        	println("brea");
            continue;
        }
        else{
            println("");
            break;
        }
    } while true;
}

var testArray = ["1", "2", "3"];

for item in testArray {
	println("test");
	println(item);
}

println("");

var test3 = [1, 2, 3];

for item in test3 {
	println(item);
}

for i, item in ["5", "ttt"] {
	println(item + i);
}

for i, item in [1, 5, 7] {
	println(item + (i as int));
}

for myI in 10 .. 20 {
	println(myI);
}

var lateInit as string;
lateInit = "initialized later";
println(lateInit);





/*
catch e as string {
	println("noLuv");
}
*/