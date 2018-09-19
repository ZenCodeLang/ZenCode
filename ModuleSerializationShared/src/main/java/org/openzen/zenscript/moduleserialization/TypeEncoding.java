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
public class TypeEncoding {
	private TypeEncoding() {}
	
	public static final int TYPE_NONE = 0;
	public static final int TYPE_VOID = 1;
	public static final int TYPE_NULL = 2;
	public static final int TYPE_BOOL = 3;
	public static final int TYPE_BYTE = 4;
	public static final int TYPE_SBYTE = 5;
	public static final int TYPE_SHORT = 6;
	public static final int TYPE_USHORT = 7;
	public static final int TYPE_INT = 8;
	public static final int TYPE_UINT = 9;
	public static final int TYPE_LONG = 10;
	public static final int TYPE_ULONG = 11;
	public static final int TYPE_USIZE = 12;
	public static final int TYPE_FLOAT = 13;
	public static final int TYPE_DOUBLE = 14;
	public static final int TYPE_CHAR = 15;
	public static final int TYPE_STRING = 16;
	public static final int TYPE_UNDETERMINED = 17;
	public static final int TYPE_DEFINITION = 18;
	public static final int TYPE_DEFINITION_INNER = 19;
	public static final int TYPE_GENERIC = 20;
	public static final int TYPE_FUNCTION = 21;
	public static final int TYPE_ARRAY = 22;
	public static final int TYPE_ARRAY_MULTIDIMENSIONAL = 23;
	public static final int TYPE_ASSOC = 24;
	public static final int TYPE_GENERIC_MAP = 25;
	public static final int TYPE_RANGE = 26;
	public static final int TYPE_ITERATOR = 27;
	public static final int TYPE_OPTIONAL = 28;
	public static final int TYPE_CONST = 29;
	public static final int TYPE_IMMUTABLE = 30;
}
