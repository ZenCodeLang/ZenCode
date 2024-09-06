package org.openzen.zenscript.codemodel;

/**
 * Enum used to indicate operator type.
 */
public enum OperatorType {
	ADD("+", "add", true),
	SUB("-", "subtract", true),
	MUL("*", "multiply", true),
	DIV("/", "divide", true),
	MOD("%", "modulo", true),
	CAT("~", "concat"),
	OR("|", "or", true),
	AND("&", "and", true),
	XOR("^", "xor", true),
	NEG("-", "negate"),
	INVERT("~", "invert"),
	NOT("!", "not"),
	INDEXSET("[]=", "setElement"),
	INDEXGET("[]", "getElement"),
	CONTAINS("in", "contains"),
	COMPARE("compare", "compareTo"),
	MEMBERGETTER(".", "getMember"),
	MEMBERSETTER(".=", "setMember"),
	EQUALS("==", "equals"),
	NOTEQUALS("!=", "notEquals"),
	SAME("===", "same"),
	NOTSAME("!==", "notSame"),
	SHL("<<", "shl"),
	SHR(">>", "shr"),
	USHR(">>>", "ushr"),

	ADDASSIGN("+=", "addAssign", ADD),
	SUBASSIGN("-=", "subAssign", SUB),
	MULASSIGN("*=", "mulAssign", MUL),
	DIVASSIGN("/=", "divAssign", DIV),
	MODASSIGN("%=", "modAssign", MOD),
	CATASSIGN("~=", "concatAssign", CAT),
	ORASSIGN("|=", "orAssign", OR),
	ANDASSIGN("&=", "andAssign", AND),
	XORASSIGN("^=", "xorAssign", XOR),
	SHLASSIGN("<<=", "shlAssign", SHL),
	SHRASSIGN(">>=", "shrAssign", SHR),
	USHRASSIGN(">>>=", "ushrAssign", USHR),

	INCREMENT("++", "increment"),
	DECREMENT("--", "decrement"),

	RANGE("..", "rangeTo"),

	CONSTRUCTOR("this", "construct"),
	DESTRUCTOR("~this", "destruct"),
	CALL("()", "call"),
	CAST("as", "cast");

	public final OperatorType assignOperatorFor;
	public final String operator;
	public final String compiledName;
	public final boolean widening;

	OperatorType(String operator, String compiledName) {
		this.operator = operator;
		this.compiledName = compiledName;
		assignOperatorFor = null;
		this.widening = false;
	}

	OperatorType(String operator, String compiledName, boolean widening) {
		this.operator = operator;
		this.compiledName = compiledName;
		assignOperatorFor = null;
		this.widening = widening;
	}

	OperatorType(String operator, String compiledName, OperatorType assignOperatorFor) {
		this.operator = operator;
		this.compiledName = compiledName;
		this.assignOperatorFor = assignOperatorFor;
		this.widening = false;
	}
}
