public class myTestClass {

	var nonFinalInt as int = 10;
	val finalInt as int = 20;

	static var staticNonFinalInt as int = 10;

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

public interface myTestInterface {
	test() as string;
}


public enum myTestEnum {
	ADD(6),
    SUB(6),
    MUL(7),
    DIV(7),
    MOD(7),
    CAT(6),
    OR(4),
    AND(4),
    XOR(4),
    NEG(8),
    NOT(8),
    INVERT(8),
    CONTAINS(5),
    COMPARE(5),
    ASSIGN(0),
    ADDASSIGN(0),
    SUBASSIGN(0),
    MULASSIGN(0),
    DIVASSIGN(0),
    MODASSIGN(0),
    CATASSIGN(0),
    ORASSIGN(0),
    ANDASSIGN(0),
    XORASSIGN(0),
    ANDAND(3),
    OROR(2),
    TERNARY(1),
    COALESCE(2),
    INCREMENT(8),
    DECREMENT(8),
    MEMBER(9),
    RANGE(9),
    INDEX(9),
    CALL(9),
    CAST(9),
    PRIMARY(10);

	private val priority as int;
	private val isCommutative as bool;

	public static val test as int = 10;

	this(i as int) {
		this(i, false);
	}

	this(i as int, isCommutative as bool) {
		this.priority = i;
		this.isCommutative = isCommutative;
		}
}