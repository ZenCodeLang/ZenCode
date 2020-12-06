/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.moduleserialization;

/**
 * @author Hoofdgebruiker
 */
public class ExpressionEncoding {
	public static final int FLAG_POSITION = 1;
	public static final int FLAG_IMPLICIT = 2; // for cast expressions
	public static final int FLAG_NAMES = 4;
	public static final int FLAG_INVERTED = 8;
	public static final int TYPE_NONE = 0;
	public static final int TYPE_AND_AND = 1;
	public static final int TYPE_ARRAY = 2;
	public static final int TYPE_COMPARE = 3;
	public static final int TYPE_CALL = 4;
	public static final int TYPE_CALL_STATIC = 5;
	public static final int TYPE_CAPTURED_CLOSURE = 6;
	public static final int TYPE_CAPTURED_DIRECT = 7;
	public static final int TYPE_CAPTURED_LOCAL_VARIABLE = 8;
	public static final int TYPE_CAPTURED_PARAMETER = 9;
	public static final int TYPE_CAPTURED_THIS = 10;
	public static final int TYPE_CAST = 11;
	public static final int TYPE_CHECKNULL = 12;
	public static final int TYPE_COALESCE = 13;
	public static final int TYPE_CONDITIONAL = 14;
	public static final int TYPE_CONST = 15;
	public static final int TYPE_CONSTANT_BOOL_TRUE = 16;
	public static final int TYPE_CONSTANT_BOOL_FALSE = 100; // TODO: reorder
	public static final int TYPE_CONSTANT_BYTE = 17;
	public static final int TYPE_CONSTANT_CHAR = 18;
	public static final int TYPE_CONSTANT_DOUBLE = 19;
	public static final int TYPE_CONSTANT_FLOAT = 20;
	public static final int TYPE_CONSTANT_INT = 21;
	public static final int TYPE_CONSTANT_LONG = 22;
	public static final int TYPE_CONSTANT_SBYTE = 23;
	public static final int TYPE_CONSTANT_SHORT = 24;
	public static final int TYPE_CONSTANT_STRING = 25;
	public static final int TYPE_CONSTANT_UINT = 26;
	public static final int TYPE_CONSTANT_ULONG = 27;
	public static final int TYPE_CONSTANT_USHORT = 28;
	public static final int TYPE_CONSTANT_USIZE = 29;
	public static final int TYPE_CONSTRUCTOR_THIS_CALL = 30;
	public static final int TYPE_CONSTRUCTOR_SUPER_CALL = 31;
	public static final int TYPE_ENUM_CONSTANT = 32;
	public static final int TYPE_FUNCTION = 33;
	public static final int TYPE_GET_FIELD = 34;
	public static final int TYPE_GET_FUNCTION_PARAMETER = 35;
	public static final int TYPE_GET_LOCAL_VARIABLE = 36;
	public static final int TYPE_GET_MATCHING_VARIANT_FIELD = 37;
	public static final int TYPE_GET_STATIC_FIELD = 38;
	public static final int TYPE_GETTER = 39;
	public static final int TYPE_GLOBAL = 40;
	public static final int TYPE_GLOBAL_CALL = 41;
	public static final int TYPE_INTERFACE_CAST = 42;
	public static final int TYPE_IS = 43;
	public static final int TYPE_MAKE_CONST = 44;
	public static final int TYPE_MAP = 45;
	public static final int TYPE_MATCH = 46;
	public static final int TYPE_NEW = 47;
	public static final int TYPE_NULL = 48;
	public static final int TYPE_OR_OR = 49;
	public static final int TYPE_PANIC = 50;
	public static final int TYPE_POST_CALL = 51;
	public static final int TYPE_RANGE = 52;
	public static final int TYPE_SAME_OBJECT = 53;
	public static final int TYPE_SET_FIELD = 54;
	public static final int TYPE_SET_FUNCTION_PARAMETER = 55;
	public static final int TYPE_SET_LOCAL_VARIABLE = 56;
	public static final int TYPE_SET_STATIC_FIELD = 57;
	public static final int TYPE_SETTER = 58;
	public static final int TYPE_STATIC_GETTER = 59;
	public static final int TYPE_STATIC_SETTER = 60;
	public static final int TYPE_SUPERTYPE_CAST = 61;
	public static final int TYPE_THIS = 62;
	public static final int TYPE_THROW = 63;
	public static final int TYPE_TRY_CONVERT = 64;
	public static final int TYPE_TRY_RETHROW_AS_EXCEPTION = 65;
	public static final int TYPE_TRY_RETHROW_AS_RESULT = 66;
	public static final int TYPE_VARIANT_VALUE = 67;
	public static final int TYPE_WRAP_OPTIONAL = 68;
	public static final int COMPARATOR_LT = 1;
	public static final int COMPARATOR_GT = 2;
	public static final int COMPARATOR_EQ = 3;
	public static final int COMPARATOR_NE = 4;
	public static final int COMPARATOR_LE = 5;
	public static final int COMPARATOR_GE = 6;

	private ExpressionEncoding() {
	}
}
