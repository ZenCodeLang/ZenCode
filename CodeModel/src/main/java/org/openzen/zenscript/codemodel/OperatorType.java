package org.openzen.zenscript.codemodel;

/**
 * Enum used to indicate operator type.
 * 
 * @author Stan Hebben
 */
public enum OperatorType {
	ADD("+", "add"),
	SUB("-", "subtract"),
	MUL("*", "multiply"),
	DIV("/", "divide"),
	MOD("%", "modulo"),
	CAT("~", "concat"),
	OR("|", "or"),
	AND("&", "and"),
	XOR("^", "xor"),
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
	
	OperatorType(String operator, String compiledName) {
		this.operator = operator;
		this.compiledName = compiledName;
		assignOperatorFor = null;
	}
	
	OperatorType(String operator, String compiledName, OperatorType assignOperatorFor) {
		this.operator = operator;
		this.compiledName = compiledName;
		this.assignOperatorFor = assignOperatorFor;
	}
}
