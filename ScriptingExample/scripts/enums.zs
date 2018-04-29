enum TestEnum {
	HELLO,
	WORLD
}

enum TestEnum2 {
	HELLO("Hello"),
	WORLD("World");
	
	val value as string;
	
	this(value as string) {
		this.value = value;
	}
}
