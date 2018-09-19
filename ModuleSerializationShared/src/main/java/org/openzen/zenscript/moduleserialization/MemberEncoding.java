/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.moduleserialization;

/**
 *
 * @author Hoofdgebruiker
 */
public class MemberEncoding {
	private MemberEncoding() {}
	
	public static final int FLAG_POSITION = 1;
	public static final int FLAG_NAME = 2;
	public static final int FLAG_AUTO_GETTER = 4;
	public static final int FLAG_AUTO_SETTER = 8;
	public static final int FLAG_ANNOTATIONS = 16;
	
	public static final int TYPE_BUILTIN = 1;
	public static final int TYPE_CONST = 2;
	public static final int TYPE_FIELD = 3;
	public static final int TYPE_CONSTRUCTOR = 4;
	public static final int TYPE_DESTRUCTOR = 5;
	public static final int TYPE_METHOD = 6;
	public static final int TYPE_GETTER = 7;
	public static final int TYPE_SETTER = 8;
	public static final int TYPE_OPERATOR = 9;
	public static final int TYPE_CASTER = 10;
	public static final int TYPE_ITERATOR = 11;
	public static final int TYPE_CALLER = 12;
	public static final int TYPE_IMPLEMENTATION = 13;
	public static final int TYPE_INNER_DEFINITION = 14;
	public static final int TYPE_STATIC_INITIALIZER = 15;
	
	public static final int OPERATOR_ADD = 1;
	public static final int OPERATOR_SUB = 2;
	public static final int OPERATOR_MUL = 3;
	public static final int OPERATOR_DIV = 4;
	public static final int OPERATOR_MOD = 5;
	public static final int OPERATOR_CAT = 6;
	public static final int OPERATOR_OR = 7;
	public static final int OPERATOR_AND = 8;
	public static final int OPERATOR_XOR = 9;
	public static final int OPERATOR_NEG = 10;
	public static final int OPERATOR_INVERT = 11;
	public static final int OPERATOR_NOT = 12;
	public static final int OPERATOR_INDEXSET = 13;
	public static final int OPERATOR_INDEXGET = 14;
	public static final int OPERATOR_CONTAINS = 15;
	public static final int OPERATOR_COMPARE = 16;
	public static final int OPERATOR_MEMBERGETTER = 17;
	public static final int OPERATOR_MEMBERSETTER = 18;
	public static final int OPERATOR_EQUALS = 19;
	public static final int OPERATOR_NOTEQUALS = 20;
	public static final int OPERATOR_SAME = 21;
	public static final int OPERATOR_NOTSAME = 22;
	public static final int OPERATOR_SHL = 23;
	public static final int OPERATOR_SHR = 24;
	public static final int OPERATOR_USHR = 25;
	
	public static final int OPERATOR_ADDASSIGN = 26;
	public static final int OPERATOR_SUBASSIGN = 27;
	public static final int OPERATOR_MULASSIGN = 28;
	public static final int OPERATOR_DIVASSIGN = 29;
	public static final int OPERATOR_MODASSIGN = 30;
	public static final int OPERATOR_CATASSIGN = 31;
	public static final int OPERATOR_ORASSIGN = 32;
	public static final int OPERATOR_ANDASSIGN = 33;
	public static final int OPERATOR_XORASSIGN = 34;
	public static final int OPERATOR_SHLASSIGN = 35;
	public static final int OPERATOR_SHRASSIGN = 36;
	public static final int OPERATOR_USHRASSIGN = 37;
	
	public static final int OPERATOR_INCREMENT = 38;
	public static final int OPERATOR_DECREMENT = 39;
	
	public static final int OPERATOR_RANGE = 40;
}
