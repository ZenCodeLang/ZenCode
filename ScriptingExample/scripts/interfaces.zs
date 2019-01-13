public interface MyInterface {
	interfaceMethod() as string;
}

public class MyClass {
	val name as string;
	
	public this(name as string) {
		this.name = name;
	}

	public implements MyInterface {
		interfaceMethod() => "InterfaceMethod " + name;
	}
}

println(new MyClass("hello").interfaceMethod());
