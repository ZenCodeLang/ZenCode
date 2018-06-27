/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javasource;

import org.openzen.zenscript.codemodel.CompareType;
import org.openzen.zenscript.formattershared.FormattableOperator;

/**
 *
 * @author Hoofdgebruiker
 */
public enum JavaOperator implements FormattableOperator {
	ADD(6, " + "),
	SUB(6, " - "),
	MUL(7, " * "),
	DIV(7, " / "),
	MOD(7, " % "),
	OR(4, " | "),
	AND(4, " & "),
	AND_FF(4, " & 0xFF"),
	AND_FFFF(4, " & 0xFFFF"),
	AND_FFL(4, " & 0xFFL"),
	AND_FFFFL(4, " & 0xFFFFL"),
	AND_8FL(4, " & 0xFFFFFFFFL"),
	XOR(4, " ^ "),
	NEG(8, " - "),
	NOT(8, " ! "),
	INVERT(8, " ~ "),
	SHL(7, " << "),
	SHR(7, " >> "),
	USHR(7, " >>> "),
	EQUALS(5, " == "),
	NOTEQUALS(6, " != "),
	GREATER(6, " > "),
	LESS(6, " < "),
	GREATER_EQUALS(6, " >= "),
	LESS_EQUALS(6, " <= "),
	ASSIGN(0, " = "),
	ADDASSIGN(0, " += "),
	SUBASSIGN(0, " -= "),
	MULASSIGN(0, " *= "),
	DIVASSIGN(0, " /= "),
	MODASSIGN(0, " %= "),
	ORASSIGN(0, " |= "),
	ANDASSIGN(0, " &= "),
	XORASSIGN(0, " ^= "),
	
	ANDAND(3, " && "),
	OROR(2, " || "),
	
	TERNARY(1, null),
	
	INCREMENT(8, "++"),
	DECREMENT(8, "--"),
	MEMBER(10, null),
	INDEX(10, null),
	CALL(10, null),
	CAST(10, null),
	INSTANCEOF(10, " instanceof "),
	NEW(10, null),
	TOSTRING(CALL.priority, ".toString()"),
	
	PRIMARY(10, null);
	
	private final int priority;
	private final boolean isCommutative;
	private final String operatorString;
	
	private JavaOperator(int priority, String operatorString) {
		this(priority, false, operatorString);
	}
	
	private JavaOperator(int priority, boolean isCommutative, String operatorString) {
		this.priority = priority;
		this.isCommutative = isCommutative;
		this.operatorString = operatorString;
	}

	@Override
	public int getPriority() {
		return priority;
	}

	@Override
	public boolean isCommutative() {
		return isCommutative;
	}
	
	@Override
	public String getOperatorString() {
		return operatorString;
	}
	
	public static JavaOperator getComparison(CompareType compare) {
		switch (compare) {
			case EQ: return EQUALS;
			case NE: return NOTEQUALS;
			case LT: return LESS;
			case GT: return GREATER;
			case LE: return LESS_EQUALS;
			case GE: return GREATER_EQUALS;
			default:
				return null;
		}
	}
}
