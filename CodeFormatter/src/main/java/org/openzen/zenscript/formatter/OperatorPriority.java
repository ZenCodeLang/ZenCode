/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.formatter;

/**
 *
 * @author Hoofdgebruiker
 */
public enum OperatorPriority {
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
	
	private final int priority;
	private final boolean isCommutative;
	
	private OperatorPriority(int priority) {
		this(priority, false);
	}
	
	private OperatorPriority(int priority, boolean isCommutative) {
		this.priority = priority;
		this.isCommutative = isCommutative;
	}
	
	public static boolean shouldWrapLeft(OperatorPriority inner, OperatorPriority outer) {
		return inner == outer || inner.priority > outer.priority;
	}
	
	public static boolean shouldWrapRight(OperatorPriority inner, OperatorPriority outer) {
		return (inner == outer && inner.isCommutative) || (inner.priority > outer.priority);
	}
}
