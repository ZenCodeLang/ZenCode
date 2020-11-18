/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.type.member;

/**
 *
 * @author Hoofdgebruiker
 */
public enum BuiltinID {
	BOOL_NOT,
	BOOL_AND,
	BOOL_OR,
	BOOL_XOR,
	BOOL_EQUALS,
	BOOL_NOTEQUALS,
	BOOL_TO_STRING,
	BOOL_PARSE,
	
	BYTE_NOT,
	BYTE_INC,
	BYTE_DEC,
	BYTE_ADD_BYTE,
	BYTE_SUB_BYTE,
	BYTE_MUL_BYTE,
	BYTE_DIV_BYTE,
	BYTE_MOD_BYTE,
	BYTE_AND_BYTE,
	BYTE_OR_BYTE,
	BYTE_XOR_BYTE,
	BYTE_SHL,
	BYTE_SHR,
	BYTE_COMPARE,
	BYTE_TO_SBYTE,
	BYTE_TO_SHORT,
	BYTE_TO_USHORT,
	BYTE_TO_INT,
	BYTE_TO_UINT,
	BYTE_TO_LONG,
	BYTE_TO_ULONG,
	BYTE_TO_USIZE,
	BYTE_TO_FLOAT,
	BYTE_TO_DOUBLE,
	BYTE_TO_CHAR,
	BYTE_TO_STRING,
	BYTE_PARSE,
	BYTE_PARSE_WITH_BASE,
	BYTE_GET_MIN_VALUE,
	BYTE_GET_MAX_VALUE,
	
	SBYTE_NOT,
	SBYTE_NEG,
	SBYTE_INC,
	SBYTE_DEC,
	SBYTE_ADD_SBYTE,
	SBYTE_SUB_SBYTE,
	SBYTE_MUL_SBYTE,
	SBYTE_DIV_SBYTE,
	SBYTE_MOD_SBYTE,
	SBYTE_AND_SBYTE,
	SBYTE_OR_SBYTE,
	SBYTE_XOR_SBYTE,
	SBYTE_SHL,
	SBYTE_SHR,
	SBYTE_USHR,
	SBYTE_COMPARE,
	SBYTE_TO_BYTE,
	SBYTE_TO_SHORT,
	SBYTE_TO_USHORT,
	SBYTE_TO_INT,
	SBYTE_TO_UINT,
	SBYTE_TO_LONG,
	SBYTE_TO_ULONG,
	SBYTE_TO_USIZE,
	SBYTE_TO_FLOAT,
	SBYTE_TO_DOUBLE,
	SBYTE_TO_CHAR,
	SBYTE_TO_STRING,
	SBYTE_PARSE,
	SBYTE_PARSE_WITH_BASE,
	SBYTE_GET_MIN_VALUE,
	SBYTE_GET_MAX_VALUE,
	
	SHORT_NOT,
	SHORT_NEG,
	SHORT_INC,
	SHORT_DEC,
	SHORT_ADD_SHORT,
	SHORT_SUB_SHORT,
	SHORT_MUL_SHORT,
	SHORT_DIV_SHORT,
	SHORT_MOD_SHORT,
	SHORT_AND_SHORT,
	SHORT_OR_SHORT,
	SHORT_XOR_SHORT,
	SHORT_SHL,
	SHORT_SHR,
	SHORT_USHR,
	SHORT_COMPARE,
	SHORT_TO_BYTE,
	SHORT_TO_SBYTE,
	SHORT_TO_USHORT,
	SHORT_TO_INT,
	SHORT_TO_UINT,
	SHORT_TO_LONG,
	SHORT_TO_ULONG,
	SHORT_TO_USIZE,
	SHORT_TO_FLOAT,
	SHORT_TO_DOUBLE,
	SHORT_TO_CHAR,
	SHORT_TO_STRING,
	SHORT_PARSE,
	SHORT_PARSE_WITH_BASE,
	SHORT_GET_MIN_VALUE,
	SHORT_GET_MAX_VALUE,
	
	USHORT_NOT,
	USHORT_INC,
	USHORT_DEC,
	USHORT_ADD_USHORT,
	USHORT_SUB_USHORT,
	USHORT_MUL_USHORT,
	USHORT_DIV_USHORT,
	USHORT_MOD_USHORT,
	USHORT_AND_USHORT,
	USHORT_OR_USHORT,
	USHORT_XOR_USHORT,
	USHORT_SHL,
	USHORT_SHR,
	USHORT_COMPARE,
	USHORT_TO_BYTE,
	USHORT_TO_SBYTE,
	USHORT_TO_SHORT,
	USHORT_TO_INT,
	USHORT_TO_UINT,
	USHORT_TO_LONG,
	USHORT_TO_ULONG,
	USHORT_TO_USIZE,
	USHORT_TO_FLOAT,
	USHORT_TO_DOUBLE,
	USHORT_TO_CHAR,
	USHORT_TO_STRING,
	USHORT_PARSE,
	USHORT_PARSE_WITH_BASE,
	USHORT_GET_MIN_VALUE,
	USHORT_GET_MAX_VALUE,
	
	INT_NOT,
	INT_NEG,
	INT_INC,
	INT_DEC,
	INT_ADD_INT,
	INT_ADD_USIZE,
	INT_SUB_INT,
	INT_MUL_INT,
	INT_DIV_INT,
	INT_MOD_INT,
	INT_AND_INT,
	INT_OR_INT,
	INT_XOR_INT,
	INT_SHL,
	INT_SHR,
	INT_USHR,
	INT_COMPARE,
	INT_TO_BYTE,
	INT_TO_SBYTE,
	INT_TO_SHORT,
	INT_TO_USHORT,
	INT_TO_UINT,
	INT_TO_LONG,
	INT_TO_ULONG,
	INT_TO_USIZE,
	INT_TO_FLOAT,
	INT_TO_DOUBLE,
	INT_TO_CHAR,
	INT_TO_STRING,
	INT_PARSE,
	INT_PARSE_WITH_BASE,
	INT_GET_MIN_VALUE,
	INT_GET_MAX_VALUE,
	INT_COUNT_LOW_ZEROES,
	INT_COUNT_HIGH_ZEROES,
	INT_COUNT_LOW_ONES,
	INT_COUNT_HIGH_ONES,
	INT_HIGHEST_ONE_BIT,
	INT_LOWEST_ONE_BIT,
	INT_HIGHEST_ZERO_BIT,
	INT_LOWEST_ZERO_BIT,
	INT_BIT_COUNT,
	
	UINT_NOT,
	UINT_INC,
	UINT_DEC,
	UINT_ADD_UINT,
	UINT_SUB_UINT,
	UINT_MUL_UINT,
	UINT_DIV_UINT,
	UINT_MOD_UINT,
	UINT_AND_UINT,
	UINT_OR_UINT,
	UINT_XOR_UINT,
	UINT_SHL,
	UINT_SHR,
	UINT_COMPARE,
	UINT_TO_BYTE,
	UINT_TO_SBYTE,
	UINT_TO_SHORT,
	UINT_TO_USHORT,
	UINT_TO_INT,
	UINT_TO_LONG,
	UINT_TO_ULONG,
	UINT_TO_USIZE,
	UINT_TO_FLOAT,
	UINT_TO_DOUBLE,
	UINT_TO_CHAR,
	UINT_TO_STRING,
	UINT_PARSE,
	UINT_PARSE_WITH_BASE,
	UINT_GET_MIN_VALUE,
	UINT_GET_MAX_VALUE,
	UINT_COUNT_LOW_ZEROES,
	UINT_COUNT_HIGH_ZEROES,
	UINT_COUNT_LOW_ONES,
	UINT_COUNT_HIGH_ONES,
	UINT_HIGHEST_ONE_BIT,
	UINT_LOWEST_ONE_BIT,
	UINT_HIGHEST_ZERO_BIT,
	UINT_LOWEST_ZERO_BIT,
	UINT_BIT_COUNT,
	
	LONG_NOT,
	LONG_NEG,
	LONG_INC,
	LONG_DEC,
	LONG_ADD_LONG,
	LONG_SUB_LONG,
	LONG_MUL_LONG,
	LONG_DIV_LONG,
	LONG_MOD_LONG,
	LONG_AND_LONG,
	LONG_OR_LONG,
	LONG_XOR_LONG,
	LONG_SHL,
	LONG_SHR,
	LONG_USHR,
	LONG_COMPARE,
	LONG_COMPARE_INT,
	LONG_TO_BYTE,
	LONG_TO_SBYTE,
	LONG_TO_SHORT,
	LONG_TO_USHORT,
	LONG_TO_INT,
	LONG_TO_UINT,
	LONG_TO_ULONG,
	LONG_TO_USIZE,
	LONG_TO_FLOAT,
	LONG_TO_DOUBLE,
	LONG_TO_CHAR,
	LONG_TO_STRING,
	LONG_PARSE,
	LONG_PARSE_WITH_BASE,
	LONG_GET_MIN_VALUE,
	LONG_GET_MAX_VALUE,
	LONG_COUNT_LOW_ZEROES,
	LONG_COUNT_HIGH_ZEROES,
	LONG_COUNT_LOW_ONES,
	LONG_COUNT_HIGH_ONES,
	LONG_HIGHEST_ONE_BIT,
	LONG_LOWEST_ONE_BIT,
	LONG_HIGHEST_ZERO_BIT,
	LONG_LOWEST_ZERO_BIT,
	LONG_BIT_COUNT,
	
	ULONG_NOT,
	ULONG_INC,
	ULONG_DEC,
	ULONG_ADD_ULONG,
	ULONG_SUB_ULONG,
	ULONG_MUL_ULONG,
	ULONG_DIV_ULONG,
	ULONG_MOD_ULONG,
	ULONG_AND_ULONG,
	ULONG_OR_ULONG,
	ULONG_XOR_ULONG,
	ULONG_SHL,
	ULONG_SHR,
	ULONG_COMPARE,
	ULONG_COMPARE_UINT,
	ULONG_COMPARE_USIZE,
	ULONG_TO_BYTE,
	ULONG_TO_SBYTE,
	ULONG_TO_SHORT,
	ULONG_TO_USHORT,
	ULONG_TO_INT,
	ULONG_TO_UINT,
	ULONG_TO_LONG,
	ULONG_TO_USIZE,
	ULONG_TO_FLOAT,
	ULONG_TO_DOUBLE,
	ULONG_TO_CHAR,
	ULONG_TO_STRING,
	ULONG_PARSE,
	ULONG_PARSE_WITH_BASE,
	ULONG_GET_MIN_VALUE,
	ULONG_GET_MAX_VALUE,
	ULONG_COUNT_LOW_ZEROES,
	ULONG_COUNT_HIGH_ZEROES,
	ULONG_COUNT_LOW_ONES,
	ULONG_COUNT_HIGH_ONES,
	ULONG_HIGHEST_ONE_BIT,
	ULONG_LOWEST_ONE_BIT,
	ULONG_HIGHEST_ZERO_BIT,
	ULONG_LOWEST_ZERO_BIT,
	ULONG_BIT_COUNT,
	
	USIZE_NOT,
	USIZE_INC,
	USIZE_DEC,
	USIZE_ADD_USIZE,
	USIZE_SUB_USIZE,
	USIZE_MUL_USIZE,
	USIZE_DIV_USIZE,
	USIZE_MOD_USIZE,
	USIZE_AND_USIZE,
	USIZE_OR_USIZE,
	USIZE_XOR_USIZE,
	USIZE_SHL,
	USIZE_SHR,
	USIZE_COMPARE,
	USIZE_COMPARE_UINT,
	USIZE_TO_BYTE,
	USIZE_TO_SBYTE,
	USIZE_TO_SHORT,
	USIZE_TO_USHORT,
	USIZE_TO_INT,
	USIZE_TO_UINT,
	USIZE_TO_LONG,
	USIZE_TO_ULONG,
	USIZE_TO_FLOAT,
	USIZE_TO_DOUBLE,
	USIZE_TO_CHAR,
	USIZE_TO_STRING,
	USIZE_PARSE,
	USIZE_PARSE_WITH_BASE,
	USIZE_GET_MIN_VALUE,
	USIZE_GET_MAX_VALUE,
	USIZE_COUNT_LOW_ZEROES,
	USIZE_COUNT_HIGH_ZEROES,
	USIZE_COUNT_LOW_ONES,
	USIZE_COUNT_HIGH_ONES,
	USIZE_HIGHEST_ONE_BIT,
	USIZE_LOWEST_ONE_BIT,
	USIZE_HIGHEST_ZERO_BIT,
	USIZE_LOWEST_ZERO_BIT,
	USIZE_BIT_COUNT,
	USIZE_BITS,
	
	FLOAT_NEG,
	FLOAT_INC,
	FLOAT_DEC,
	FLOAT_ADD_FLOAT,
	FLOAT_SUB_FLOAT,
	FLOAT_MUL_FLOAT,
	FLOAT_DIV_FLOAT,
	FLOAT_MOD_FLOAT,
	FLOAT_COMPARE,
	FLOAT_TO_BYTE,
	FLOAT_TO_SBYTE,
	FLOAT_TO_SHORT,
	FLOAT_TO_USHORT,
	FLOAT_TO_INT,
	FLOAT_TO_UINT,
	FLOAT_TO_LONG,
	FLOAT_TO_ULONG,
	FLOAT_TO_USIZE,
	FLOAT_TO_DOUBLE,
	FLOAT_TO_STRING,
	FLOAT_BITS,
	FLOAT_FROM_BITS,
	FLOAT_PARSE,
	FLOAT_GET_MIN_VALUE,
	FLOAT_GET_MAX_VALUE,
	
	DOUBLE_NEG,
	DOUBLE_INC,
	DOUBLE_DEC,
	DOUBLE_ADD_DOUBLE,
	DOUBLE_SUB_DOUBLE,
	DOUBLE_MUL_DOUBLE,
	DOUBLE_DIV_DOUBLE,
	DOUBLE_MOD_DOUBLE,
	DOUBLE_COMPARE,
	DOUBLE_TO_BYTE,
	DOUBLE_TO_SBYTE,
	DOUBLE_TO_SHORT,
	DOUBLE_TO_USHORT,
	DOUBLE_TO_INT,
	DOUBLE_TO_UINT,
	DOUBLE_TO_LONG,
	DOUBLE_TO_ULONG,
	DOUBLE_TO_USIZE,
	DOUBLE_TO_FLOAT,
	DOUBLE_TO_STRING,
	DOUBLE_BITS,
	DOUBLE_FROM_BITS,
	DOUBLE_PARSE,
	DOUBLE_GET_MIN_VALUE,
	DOUBLE_GET_MAX_VALUE,
	
	CHAR_ADD_INT,
	CHAR_SUB_INT,
	CHAR_SUB_CHAR,
	CHAR_COMPARE,
	CHAR_TO_BYTE,
	CHAR_TO_SBYTE,
	CHAR_TO_SHORT,
	CHAR_TO_USHORT,
	CHAR_TO_INT,
	CHAR_TO_UINT,
	CHAR_TO_LONG,
	CHAR_TO_ULONG,
	CHAR_TO_USIZE,
	CHAR_TO_STRING,
	CHAR_GET_MIN_VALUE,
	CHAR_GET_MAX_VALUE,
	CHAR_REMOVE_DIACRITICS,
	CHAR_TO_LOWER_CASE,
	CHAR_TO_UPPER_CASE,
	
	STRING_CONSTRUCTOR_CHARACTERS,
	STRING_ADD_STRING,
	STRING_COMPARE,
	STRING_LENGTH,
	STRING_INDEXGET,
	STRING_RANGEGET,
	STRING_CHARACTERS,
	STRING_ISEMPTY,
	STRING_REMOVE_DIACRITICS,
	STRING_TRIM,
	STRING_TO_LOWER_CASE,
	STRING_TO_UPPER_CASE,
	STRING_CONTAINS_CHAR,
	STRING_CONTAINS_STRING,
	
	ASSOC_CONSTRUCTOR,
	ASSOC_INDEXGET,
	ASSOC_INDEXSET,
	ASSOC_CONTAINS,
	ASSOC_GETORDEFAULT,
	ASSOC_SIZE,
	ASSOC_ISEMPTY,
	ASSOC_KEYS,
	ASSOC_VALUES,
	ASSOC_HASHCODE,
	ASSOC_EQUALS,
	ASSOC_NOTEQUALS,
	ASSOC_SAME,
	ASSOC_NOTSAME,
	
	GENERICMAP_CONSTRUCTOR,
	GENERICMAP_GETOPTIONAL,
	GENERICMAP_PUT,
	GENERICMAP_CONTAINS,
	GENERICMAP_ADDALL,
	GENERICMAP_SIZE,
	GENERICMAP_ISEMPTY,
	GENERICMAP_HASHCODE,
	GENERICMAP_EQUALS,
	GENERICMAP_NOTEQUALS,
	GENERICMAP_SAME,
	GENERICMAP_NOTSAME,
	
	ARRAY_CONSTRUCTOR_SIZED,
	ARRAY_CONSTRUCTOR_INITIAL_VALUE,
	ARRAY_CONSTRUCTOR_LAMBDA,
	ARRAY_CONSTRUCTOR_PROJECTED,
	ARRAY_CONSTRUCTOR_PROJECTED_INDEXED,
	ARRAY_INDEXGET,
	ARRAY_INDEXSET,
	ARRAY_INDEXGETRANGE,
	ARRAY_CONTAINS,
	ARRAY_LENGTH,
	ARRAY_ISEMPTY,
	ARRAY_HASHCODE,
	ARRAY_EQUALS,
	ARRAY_NOTEQUALS,
	ARRAY_SAME,
	ARRAY_NOTSAME,
	
	SBYTE_ARRAY_AS_BYTE_ARRAY,
	BYTE_ARRAY_AS_SBYTE_ARRAY,
	SHORT_ARRAY_AS_USHORT_ARRAY,
	USHORT_ARRAY_AS_SHORT_ARRAY,
	INT_ARRAY_AS_UINT_ARRAY,
	UINT_ARRAY_AS_INT_ARRAY,
	LONG_ARRAY_AS_ULONG_ARRAY,
	ULONG_ARRAY_AS_LONG_ARRAY,
	
	FUNCTION_CALL,
	FUNCTION_SAME,
	FUNCTION_NOTSAME,
	
	CLASS_DEFAULT_CONSTRUCTOR,
	STRUCT_EMPTY_CONSTRUCTOR,
	STRUCT_VALUE_CONSTRUCTOR,
	ENUM_EMPTY_CONSTRUCTOR,
	ENUM_NAME,
	ENUM_ORDINAL,
	ENUM_VALUES,
	ENUM_TO_STRING,
	ENUM_COMPARE,
	OBJECT_HASHCODE,
	OBJECT_SAME,
	OBJECT_NOTSAME,
	
	RANGE_FROM,
	RANGE_TO,
	
	OPTIONAL_IS_NULL,
	OPTIONAL_IS_NOT_NULL,
	
	ITERATOR_INT_RANGE,
	ITERATOR_ARRAY_VALUES,
	ITERATOR_ARRAY_KEY_VALUES,
	ITERATOR_ASSOC_KEYS,
	ITERATOR_ASSOC_KEY_VALUES,
	ITERATOR_STRING_CHARS,
	ITERATOR_ITERABLE;
	
	private static final BuiltinID[] VALUES = values();
	public static BuiltinID get(int ordinal) {
		return VALUES[ordinal];
	}
}
