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
	NOT("!", "not"),
	INDEXSET("[]=", "setElement"),
	INDEXGET("[]", "getElement"),
	CONTAINS("in", "contains"),
	COMPARE("compare", "compareTo"),
	MEMBERGETTER(".", "getMember"),
	MEMBERSETTER(".=", "setMember"),
	EQUALS("==", "equals"),
	
	ADDASSIGN("+=", "addAssign", ADD),
	SUBASSIGN("-=", "subAssign", SUB),
	MULASSIGN("*=", "mulAssign", MUL),
	DIVASSIGN("/=", "divAssign", DIV),
	MODASSIGN("%=", "modAssign", MOD),
	CATASSIGN("~=", "concatAssign", CAT),
	ORASSIGN("|=", "orAssign", OR),
	ANDASSIGN("&=", "andAssign", AND),
	XORASSIGN("^=", "xorAssign", XOR),
	
	POST_INCREMENT("x++", "postIncrement"),
	POST_DECREMENT("x--", "postDecrement"),
	PRE_INCREMENT("++x", "preIncrement"),
	PRE_DECREMENT("--x", "preDecrement"),
	
	CONSTRUCTOR("this", "construct"),
	CALL("()", "call"),
	CAST("as", "cast");
	
	public final OperatorType assignOperatorFor;
	public final String operator;
	public final String compiledName;
	
	private OperatorType(String operator, String compiledName) {
		this.operator = operator;
		this.compiledName = compiledName;
		assignOperatorFor = null;
	}
	
	private OperatorType(String operator, String compiledName, OperatorType assignOperatorFor) {
		this.operator = operator;
		this.compiledName = compiledName;
		this.assignOperatorFor = assignOperatorFor;
	}
}
