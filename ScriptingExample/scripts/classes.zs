public class myTestClass {

	var nonFinalInt as int = 10;
	val finalInt as int = 20;

	public this() {

	}

	public this(nonfinalInt as int) {
		this.nonFinalInt = nonfinalInt;
		println(nonfinalInt);
	}

	public test() as string {
		return "TEST";
	}
}



val tt = new myTestClass(666);
println(tt.test());