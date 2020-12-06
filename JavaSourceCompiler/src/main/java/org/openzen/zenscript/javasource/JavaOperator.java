/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javasource;

import org.openzen.zenscript.codemodel.CompareType;
import org.openzen.zenscript.formattershared.FormattableOperator;

/**
 * @author Hoofdgebruiker
 */
public enum JavaOperator implements FormattableOperator {
	INCREMENT(14, "++"),
	DECREMENT(14, "--"),

	NOT(13, "!"),
	INVERT(13, "~"),
	NEG(13, "-"),

	MUL(12, " * "),
	DIV(12, " / "),
	MOD(12, " % "),

	ADD(11, " + "),
	SUB(11, " - "),

	SHL(10, " << "),
	SHR(10, " >> "),
	USHR(10, " >>> "),

	GREATER(9, " > "),
	LESS(9, " < "),
	GREATER_EQUALS(9, " >= "),
	LESS_EQUALS(9, " <= "),
	INSTANCEOF(9, " instanceof "),

	EQUALS(8, " == "),
	NOTEQUALS(8, " != "),

	AND(7, " & "),
	AND_FF(7, " & 0xFF"),
	AND_FFFF(7, " & 0xFFFF"),
	AND_FFL(7, " & 0xFFL"),
	AND_FFFFL(7, " & 0xFFFFL"),
	AND_8FL(7, " & 0xFFFFFFFFL"),

	XOR(6, " ^ "),
	OR(5, " | "),
	ANDAND(4, " && "),
	OROR(3, " || "),

	TERNARY(2, null),

	ASSIGN(1, " = "),
	ADDASSIGN(1, " += "),
	SUBASSIGN(1, " -= "),
	MULASSIGN(1, " *= "),
	DIVASSIGN(1, " /= "),
	MODASSIGN(1, " %= "),
	ORASSIGN(1, " |= "),
	ANDASSIGN(1, " &= "),
	XORASSIGN(1, " ^= "),

	MEMBER(20, null),
	INDEX(20, null),
	CALL(20, null),
	CAST(20, null),
	NEW(20, null),
	TOSTRING(CALL.priority, ".toString()"),

	LAMBDA(1, null),

	PRIMARY(21, null);

	private final int priority;
	private final String operatorString;

	private JavaOperator(int priority, String operatorString) {
		this.priority = priority;
		this.operatorString = operatorString;
	}

	public static JavaOperator getComparison(CompareType compare) {
		switch (compare) {
			case EQ:
				return EQUALS;
			case NE:
				return NOTEQUALS;
			case LT:
				return LESS;
			case GT:
				return GREATER;
			case LE:
				return LESS_EQUALS;
			case GE:
				return GREATER_EQUALS;
			default:
				return null;
		}
	}

	@Override
	public int getPriority() {
		return priority;
	}

	@Override
	public String getOperatorString() {
		return operatorString;
	}
}
