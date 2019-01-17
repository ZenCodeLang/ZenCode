import example.TestClass;
import example.TestInterface;

val instance = new TestClass("Instance");
println("Name: " + instance.name);
instance.dump();

class TestOperators {
	public (name as string) as void {
		println("MyTestClass: " + name);
	}
	
	//.(key as string) as string
	//	=> "key " + key;
}

class TestImplementation {
	public implements TestInterface {
		interfaceMethod() => "TestImplementation";
	}
}

val testInstance = new TestOperators();
//testInstance("something");

something.dump();

val objects = makeArray(5);
printMany(objects);


println(<test string>);
println(<test string>.name);
println(<test string>.interfaceMethod());
println(new TestImplementation().interfaceMethod());

var diamond = <item:minecraft:diamond>;
var dirt = <item:minecraft:dirt>;
addShapedRecipe("TestRecipe", diamond, [[dirt, dirt, dirt],[dirt, dirt, dirt],[dirt, dirt, dirt]]);

var count = 10;
floatMethod(5f * count);
