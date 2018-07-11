/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.formatter;

import org.openzen.zenscript.codemodel.CompareType;
import org.openzen.zenscript.formattershared.FormattableOperator;

/**
 *
 * @author Hoofdgebruiker
 */
public enum ZenScriptOperator implements FormattableOperator {
	ADD(6, " + "),
	SUB(6, " - "),
	MUL(7, " * "),
	DIV(7, " / "),
	MOD(7, " % "),
	CAT(6, " ~ "),
	OR(4, " | "),
	AND(4, " & "),
	XOR(4, " ^ "),
	NEG(8, "-"),
	NOT(8, "!"),
	INVERT(8, "~"),
	CONTAINS(5, " in "),
	EQUALS(6, " == "),
	NOTEQUALS(6, " != "),
	GREATER(6, " > "),
	LESS(6, " < "),
	GREATER_EQUALS(6, " >= "),
	LESS_EQUALS(6, " <= "),
	IS(6, " is "),
	SAME(6, " === "),
	NOTSAME(6, " !== "),
	ASSIGN(0, " = "),
	ADDASSIGN(0, " += "),
	SUBASSIGN(0, " -= "),
	MULASSIGN(0, " *= "),
	DIVASSIGN(0, " /= "),
	MODASSIGN(0, " %= "),
	CATASSIGN(0, " ~= "),
	ORASSIGN(0, " |= "),
	ANDASSIGN(0, " &= "),
	XORASSIGN(0, " ^= "),
	
	ANDAND(3, " && "),
	OROR(2, " || "),
	
	TERNARY(1, null),
	COALESCE(2, " ?? "),
	
	INCREMENT(8, "++"),
	DECREMENT(8, "--"),
	MEMBER(9, null),
	RANGE(9, " .. "),
	INDEX(9, null),
	CALL(9, null),
	CAST(9, null),
	
	PANIC(10, "panic "),
	PRIMARY(10, null),
	FUNCTION(10, null);
	
	private final int priority;
	private final String operatorString;
	
	private ZenScriptOperator(int priority, String operatorString) {
		this.priority = priority;
		this.operatorString = operatorString;
	}

	@Override
	public int getPriority() {
		return priority;
	}
	
	@Override
	public String getOperatorString() {
		return operatorString;
	}
	
	public static ZenScriptOperator getComparison(CompareType compare) {
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
