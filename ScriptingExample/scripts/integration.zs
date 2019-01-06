import example.TestClass;

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

val testInstance = new TestOperators();
//testInstance("something");

something.dump();

val objects = makeArray(5);
printMany(objects);
