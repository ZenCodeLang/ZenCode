package org.openzen.zenscript.codemodel.type.builtin;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.constant.CompileTimeConstant;
import org.openzen.zenscript.codemodel.constant.StringConstant;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.identifiers.DefinitionSymbol;
import org.openzen.zenscript.codemodel.identifiers.MethodSymbol;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.codemodel.type.*;

import java.util.Optional;

import static org.openzen.zenscript.codemodel.OperatorType.*;
import static org.openzen.zenscript.codemodel.type.BasicTypeID.*;

public enum BuiltinMethodSymbol implements MethodSymbol {
	BOOL_NOT(BOOL, NOT, BOOL, BOOL),
	BOOL_AND(BOOL, AND, BOOL, BOOL, BOOL),
	BOOL_OR(BOOL, OR, BOOL, BOOL, BOOL),
	BOOL_XOR(BOOL, XOR, BOOL, BOOL, BOOL),
	BOOL_EQUALS(BOOL, EQUALS, BOOL, BOOL, BOOL),
	BOOL_NOTEQUALS(BOOL, NOTEQUALS, BOOL, BOOL, BOOL),
	BOOL_TO_STRING(BOOL, "as string", STRING, BOOL),
	BOOL_PARSE(BOOL, "parse", BOOL, STRING),

	BYTE_INVERT(BYTE, INVERT, BYTE, BYTE),
	BYTE_INC(BYTE, INCREMENT, BYTE, BYTE),
	BYTE_DEC(BYTE, DECREMENT, BYTE, BYTE),
	BYTE_ADD_BYTE(BYTE, ADD, BYTE, BYTE, BYTE),
	BYTE_SUB_BYTE(BYTE, SUB, BYTE, BYTE, BYTE),
	BYTE_MUL_BYTE(BYTE, MUL, BYTE, BYTE, BYTE),
	BYTE_DIV_BYTE(BYTE, DIV, BYTE, BYTE, BYTE),
	BYTE_MOD_BYTE(BYTE, MOD, BYTE, BYTE, BYTE),
	BYTE_AND_BYTE(BYTE, AND, BYTE, BYTE, BYTE),
	BYTE_OR_BYTE(BYTE, OR, BYTE, BYTE, BYTE),
	BYTE_XOR_BYTE(BYTE, XOR, BYTE, BYTE, BYTE),
	BYTE_SHL(BYTE, SHL, BYTE, BYTE, BYTE),
	BYTE_SHR(BYTE, SHR, BYTE, BYTE, BYTE),
	BYTE_COMPARE(BYTE, COMPARE, INT, BYTE, BYTE),
	BYTE_TO_SBYTE(BYTE, "as sbyte", SBYTE, BYTE),
	BYTE_TO_SHORT(BYTE, "as short", SHORT, BYTE),
	BYTE_TO_USHORT(BYTE, "as ushort", USHORT, BYTE),
	BYTE_TO_INT(BYTE, "as int", INT, BYTE),
	BYTE_TO_UINT(BYTE, "as uint", UINT, BYTE),
	BYTE_TO_LONG(BYTE, "as long", LONG, BYTE),
	BYTE_TO_ULONG(BYTE, "as ulong", ULONG, BYTE),
	BYTE_TO_USIZE(BYTE, "as usize", USIZE, BYTE),
	BYTE_TO_FLOAT(BYTE, "as float", FLOAT, BYTE),
	BYTE_TO_DOUBLE(BYTE, "as double", DOUBLE, BYTE),
	BYTE_TO_CHAR(BYTE, "as char", CHAR, BYTE),
	BYTE_TO_STRING(BYTE, "as string", STRING, BYTE),
	BYTE_PARSE(BYTE, "parse", BYTE, STRING),
	BYTE_PARSE_WITH_BASE(BYTE, "parse", BYTE, STRING, INT),
	BYTE_GET_MIN_VALUE(BYTE, "MIN_VALUE", BYTE),
	BYTE_GET_MAX_VALUE(BYTE, "MAX_VALUE", BYTE),

	SBYTE_INVERT(SBYTE, INVERT, SBYTE, SBYTE),
	SBYTE_NEG(SBYTE, NEG, SBYTE, SBYTE),
	SBYTE_INC(SBYTE, INCREMENT, SBYTE, SBYTE),
	SBYTE_DEC(SBYTE, DECREMENT, SBYTE, SBYTE),
	SBYTE_ADD_SBYTE(SBYTE, ADD, SBYTE, SBYTE),
	SBYTE_SUB_SBYTE(SBYTE, NEG, SBYTE, SBYTE),
	SBYTE_MUL_SBYTE(SBYTE, MUL, SBYTE, SBYTE),
	SBYTE_DIV_SBYTE(SBYTE, DIV, SBYTE, SBYTE),
	SBYTE_MOD_SBYTE(SBYTE, MOD, SBYTE, SBYTE),
	SBYTE_AND_SBYTE(SBYTE, AND, SBYTE, SBYTE),
	SBYTE_OR_SBYTE(SBYTE, OR, SBYTE, SBYTE),
	SBYTE_XOR_SBYTE(SBYTE, XOR, SBYTE, SBYTE),
	SBYTE_SHL(SBYTE, SHL, SBYTE, SBYTE),
	SBYTE_SHR(SBYTE, SHR, SBYTE, SBYTE),
	SBYTE_USHR(SBYTE, USHR, SBYTE, SBYTE),
	SBYTE_COMPARE(SBYTE, COMPARE, INT, SBYTE),
	SBYTE_TO_BYTE(SBYTE, "as byte", BYTE, SBYTE),
	SBYTE_TO_SHORT(SBYTE, "as short", SHORT, SBYTE),
	SBYTE_TO_USHORT(SBYTE, "as ushort", USHORT, SBYTE),
	SBYTE_TO_INT(SBYTE, "as int", INT, SBYTE),
	SBYTE_TO_UINT(SBYTE, "as uint", UINT, SBYTE),
	SBYTE_TO_LONG(SBYTE, "as long", LONG, SBYTE),
	SBYTE_TO_ULONG(SBYTE, "as ulong", ULONG, SBYTE),
	SBYTE_TO_USIZE(SBYTE, "as usize", USIZE, SBYTE),
	SBYTE_TO_FLOAT(SBYTE, "as float", FLOAT, SBYTE),
	SBYTE_TO_DOUBLE(SBYTE, "as double", DOUBLE, SBYTE),
	SBYTE_TO_CHAR(SBYTE, "as char", CHAR, SBYTE),
	SBYTE_TO_STRING(SBYTE, "as string", STRING, SBYTE),
	SBYTE_PARSE(SBYTE, "parse", SBYTE, STRING, INT),
	SBYTE_PARSE_WITH_BASE(SBYTE, "parse", SBYTE, STRING, INT),
	SBYTE_GET_MIN_VALUE(SBYTE, "MIN_VALUE", SBYTE),
	SBYTE_GET_MAX_VALUE(SBYTE, "MAX_VALUE", SBYTE),

	SHORT_INVERT(SHORT, INVERT, SHORT, SHORT),
	SHORT_NEG(SHORT, NEG, SHORT, SHORT),
	SHORT_INC(SHORT, INCREMENT, SHORT, SHORT),
	SHORT_DEC(SHORT, DECREMENT, SHORT, SHORT),
	SHORT_ADD_SHORT(SHORT, ADD, SHORT, SHORT),
	SHORT_SUB_SHORT(SHORT, NEG, SHORT, SHORT),
	SHORT_MUL_SHORT(SHORT, MUL, SHORT, SHORT),
	SHORT_DIV_SHORT(SHORT, DIV, SHORT, SHORT),
	SHORT_MOD_SHORT(SHORT, MOD, SHORT, SHORT),
	SHORT_AND_SHORT(SHORT, AND, SHORT, SHORT),
	SHORT_OR_SHORT(SHORT, OR, SHORT, SHORT),
	SHORT_XOR_SHORT(SHORT, XOR, SHORT, SHORT),
	SHORT_SHL(SHORT, SHL, SHORT, SHORT),
	SHORT_SHR(SHORT, SHR, SHORT, SHORT),
	SHORT_USHR(SHORT, USHR, SHORT, SHORT),
	SHORT_COMPARE(SHORT, COMPARE, INT, SHORT),
	SHORT_TO_BYTE(SHORT, "as byte", BYTE, SHORT),
	SHORT_TO_SBYTE(SHORT, "as sbyte", SBYTE, SHORT),
	SHORT_TO_USHORT(SHORT, "as ushort", USHORT, SHORT),
	SHORT_TO_INT(SHORT, "as int", INT, SHORT),
	SHORT_TO_UINT(SHORT, "as uint", UINT, SHORT),
	SHORT_TO_LONG(SHORT, "as long", LONG, SHORT),
	SHORT_TO_ULONG(SHORT, "as ulong", ULONG, SHORT),
	SHORT_TO_USIZE(SHORT, "as usize", USIZE, SHORT),
	SHORT_TO_FLOAT(SHORT, "as float", FLOAT, SHORT),
	SHORT_TO_DOUBLE(SHORT, "as double", DOUBLE, SHORT),
	SHORT_TO_CHAR(SHORT, "as char", CHAR, SHORT),
	SHORT_TO_STRING(SHORT, "as string", STRING, SHORT),
	SHORT_PARSE(SHORT, "parse", SHORT, STRING, INT),
	SHORT_PARSE_WITH_BASE(SHORT, "parse", SHORT, STRING, INT),
	SHORT_GET_MIN_VALUE(SHORT, "MIN_VALUE", SHORT),
	SHORT_GET_MAX_VALUE(SHORT, "MAX_VALUE", SHORT),

	USHORT_INVERT(USHORT, INVERT, USHORT, USHORT),
	USHORT_INC(USHORT, INCREMENT, USHORT, USHORT),
	USHORT_DEC(USHORT, DECREMENT, USHORT, USHORT),
	USHORT_ADD_USHORT(USHORT, ADD, USHORT, USHORT),
	USHORT_SUB_USHORT(USHORT, NEG, USHORT, USHORT),
	USHORT_MUL_USHORT(USHORT, MUL, USHORT, USHORT),
	USHORT_DIV_USHORT(USHORT, DIV, USHORT, USHORT),
	USHORT_MOD_USHORT(USHORT, MOD, USHORT, USHORT),
	USHORT_AND_USHORT(USHORT, AND, USHORT, USHORT),
	USHORT_OR_USHORT(USHORT, OR, USHORT, USHORT),
	USHORT_XOR_USHORT(USHORT, XOR, USHORT, USHORT),
	USHORT_SHL(USHORT, SHL, USHORT, USHORT),
	USHORT_SHR(USHORT, SHR, USHORT, USHORT),
	USHORT_COMPARE(USHORT, COMPARE, INT, USHORT),
	USHORT_TO_BYTE(USHORT, "as byte", BYTE, USHORT),
	USHORT_TO_SBYTE(USHORT, "as sbyte", SBYTE, USHORT),
	USHORT_TO_SHORT(USHORT, "as short", SHORT, USHORT),
	USHORT_TO_INT(USHORT, "as int", INT, USHORT),
	USHORT_TO_UINT(USHORT, "as uint", UINT, USHORT),
	USHORT_TO_LONG(USHORT, "as long", LONG, USHORT),
	USHORT_TO_ULONG(USHORT, "as ulong", ULONG, USHORT),
	USHORT_TO_USIZE(USHORT, "as usize", USIZE, USHORT),
	USHORT_TO_FLOAT(USHORT, "as float", FLOAT, USHORT),
	USHORT_TO_DOUBLE(USHORT, "as double", DOUBLE, USHORT),
	USHORT_TO_CHAR(USHORT, "as char", CHAR, USHORT),
	USHORT_TO_STRING(USHORT, "as string", STRING, USHORT),
	USHORT_PARSE(USHORT, "parse", USHORT, STRING, INT),
	USHORT_PARSE_WITH_BASE(USHORT, "parse", USHORT, STRING, INT),
	USHORT_GET_MIN_VALUE(USHORT, "MIN_VALUE", USHORT),
	USHORT_GET_MAX_VALUE(USHORT, "MAX_VALUE", USHORT),

	INT_INVERT(INT, INVERT, INT, INT),
	INT_NEG(INT, NEG, INT, INT),
	INT_INC(INT, INCREMENT, INT, INT),
	INT_DEC(INT, DECREMENT, INT, INT),
	INT_ADD_INT(INT, ADD, INT, INT),
	INT_ADD_USIZE(INT, ADD, USIZE, USIZE),
	INT_SUB_INT(INT, NEG, INT, INT),
	INT_MUL_INT(INT, MUL, INT, INT),
	INT_DIV_INT(INT, DIV, INT, INT),
	INT_MOD_INT(INT, MOD, INT, INT),
	INT_AND_INT(INT, AND, INT, INT),
	INT_OR_INT(INT, OR, INT, INT),
	INT_XOR_INT(INT, XOR, INT, INT),
	INT_SHL(INT, SHL, INT, INT),
	INT_SHR(INT, SHR, INT, INT),
	INT_USHR(INT, USHR, INT, INT),
	INT_COMPARE(INT, COMPARE, INT, INT),

	INT_TO_BYTE(INT, "as byte", BYTE, INT),
	INT_TO_SBYTE(INT, "as sbyte", SBYTE, INT),
	INT_TO_SHORT(INT, "as short", SHORT, INT),
	INT_TO_USHORT(INT, "as ushort", USHORT, INT),
	INT_TO_UINT(INT, "as uint", UINT, INT),
	INT_TO_LONG(INT, "as long", LONG, INT),
	INT_TO_ULONG(INT, "as ulong", ULONG, INT),
	INT_TO_USIZE(INT, "as usize", USIZE, INT),
	INT_TO_FLOAT(INT, "as float", FLOAT, INT),
	INT_TO_DOUBLE(INT, "as double", DOUBLE, INT),
	INT_TO_CHAR(INT, "as char", CHAR, INT),
	INT_TO_STRING(INT, "as string", STRING, INT),

	INT_PARSE(INT, "parse", INT, STRING),
	INT_PARSE_WITH_BASE(INT, "parse", INT, STRING, INT),
	INT_GET_MIN_VALUE(INT, "MIN_VALUE", INT),
	INT_GET_MAX_VALUE(INT, "MAX_VALUE", INT),
	INT_COUNT_LOW_ZEROES(INT, "countLowZeroes", USIZE),
	INT_COUNT_HIGH_ZEROES(INT, "countHighZeroes", USIZE),
	INT_COUNT_LOW_ONES(INT, "countLowOnes", USIZE),
	INT_COUNT_HIGH_ONES(INT, "countHighOnes", USIZE),
	INT_HIGHEST_ONE_BIT(INT, "highestOneBit", new OptionalTypeID(USIZE)),
	INT_LOWEST_ONE_BIT(INT, "lowestOneBit", new OptionalTypeID(USIZE)),
	INT_HIGHEST_ZERO_BIT(INT, "highestZeroBit", new OptionalTypeID(USIZE)),
	INT_LOWEST_ZERO_BIT(INT, "lowestZeroBit", new OptionalTypeID(USIZE)),
	INT_BIT_COUNT(INT, "bitCount", USIZE),

	UINT_INVERT(UINT, INVERT, UINT, UINT),
	UINT_INC(UINT, INCREMENT, UINT, UINT),
	UINT_DEC(UINT, DECREMENT, UINT, UINT),
	UINT_ADD_UINT(UINT, ADD, UINT, UINT),
	UINT_SUB_UINT(UINT, NEG, UINT, UINT),
	UINT_MUL_UINT(UINT, MUL, UINT, UINT),
	UINT_DIV_UINT(UINT, DIV, UINT, UINT),
	UINT_MOD_UINT(UINT, MOD, UINT, UINT),
	UINT_AND_UINT(UINT, AND, UINT, UINT),
	UINT_OR_UINT(UINT, OR, UINT, UINT),
	UINT_XOR_UINT(UINT, XOR, UINT, UINT),
	UINT_SHL(UINT, SHL, UINT, UINT),
	UINT_SHR(UINT, SHR, UINT, UINT),
	UINT_COMPARE(UINT, COMPARE, INT, UINT),
	UINT_TO_BYTE(UINT, "as byte", BYTE, UINT),
	UINT_TO_SBYTE(UINT, "as sbyte", SBYTE, UINT),
	UINT_TO_SHORT(UINT, "as short", SHORT, UINT),
	UINT_TO_USHORT(UINT, "as ushort", USHORT, UINT),
	UINT_TO_INT(UINT, "as int", INT, UINT),
	UINT_TO_LONG(UINT, "as long", LONG, UINT),
	UINT_TO_ULONG(UINT, "as ulong", ULONG, UINT),
	UINT_TO_USIZE(UINT, "as usize", USIZE, UINT),
	UINT_TO_FLOAT(UINT, "as float", FLOAT, UINT),
	UINT_TO_DOUBLE(UINT, "as double", DOUBLE, UINT),
	UINT_TO_CHAR(UINT, "as char", CHAR, UINT),
	UINT_TO_STRING(UINT, "as string", STRING, UINT),
	UINT_PARSE(UINT, "parse", UINT, STRING),
	UINT_PARSE_WITH_BASE(UINT, "parse", UINT, STRING, INT),
	UINT_GET_MIN_VALUE(UINT, "MIN_VALUE", UINT),
	UINT_GET_MAX_VALUE(UINT, "MIN_VALUE", UINT),
	UINT_COUNT_LOW_ZEROES(UINT, "countLowZeroes", USIZE),
	UINT_COUNT_HIGH_ZEROES(UINT, "countHighZeroes", USIZE),
	UINT_COUNT_LOW_ONES(UINT, "countLowOnes", USIZE),
	UINT_COUNT_HIGH_ONES(UINT, "countHighOnes", USIZE),
	UINT_HIGHEST_ONE_BIT(UINT, "highestOneBit", new OptionalTypeID(USIZE)),
	UINT_LOWEST_ONE_BIT(UINT, "lowestOneBit", new OptionalTypeID(USIZE)),
	UINT_HIGHEST_ZERO_BIT(UINT, "highestZeroBit", new OptionalTypeID(USIZE)),
	UINT_LOWEST_ZERO_BIT(UINT, "lowestZeroBit", new OptionalTypeID(USIZE)),
	UINT_BIT_COUNT(UINT, "bitCount", USIZE),

	LONG_INVERT(LONG, INVERT, LONG, LONG),
	LONG_NEG(LONG, NEG, LONG, LONG),
	LONG_INC(LONG, INCREMENT, LONG, LONG),
	LONG_DEC(LONG, DECREMENT, LONG, LONG),
	LONG_ADD_LONG(LONG, ADD, LONG, LONG),
	LONG_SUB_LONG(LONG, NEG, LONG, LONG),
	LONG_MUL_LONG(LONG, MUL, LONG, LONG),
	LONG_DIV_LONG(LONG, DIV, LONG, LONG),
	LONG_MOD_LONG(LONG, MOD, LONG, LONG),
	LONG_AND_LONG(LONG, AND, LONG, LONG),
	LONG_OR_LONG(LONG, OR, LONG, LONG),
	LONG_XOR_LONG(LONG, XOR, LONG, LONG),
	LONG_SHL(LONG, SHL, LONG, LONG),
	LONG_SHR(LONG, SHR, LONG, LONG),
	LONG_USHR(LONG, USHR, LONG, LONG),
	LONG_COMPARE(LONG, COMPARE, LONG, LONG),
	LONG_COMPARE_INT(LONG, COMPARE, LONG, LONG),
	LONG_TO_BYTE(LONG, "as byte", BYTE, LONG),
	LONG_TO_SBYTE(LONG, "as sbyte", SBYTE, LONG),
	LONG_TO_SHORT(LONG, "as short", SHORT, LONG),
	LONG_TO_USHORT(LONG, "as ushort", USHORT, LONG),
	LONG_TO_INT(LONG, "as int", INT, LONG),
	LONG_TO_UINT(LONG, "as uint", UINT, LONG),
	LONG_TO_ULONG(LONG, "as ulong", ULONG, LONG),
	LONG_TO_USIZE(LONG, "as usize", USIZE, LONG),
	LONG_TO_FLOAT(LONG, "as float", FLOAT, LONG),
	LONG_TO_DOUBLE(LONG, "as double", DOUBLE, LONG),
	LONG_TO_CHAR(LONG, "as char", CHAR, LONG),
	LONG_TO_STRING(LONG, "as string", STRING, LONG),
	LONG_PARSE(LONG, "parse", LONG, STRING),
	LONG_PARSE_WITH_BASE(LONG, "parse", LONG, STRING, INT),
	LONG_GET_MIN_VALUE(LONG, "MIN_VALUE", LONG),
	LONG_GET_MAX_VALUE(LONG, "MAX_VALUE", LONG),
	LONG_COUNT_LOW_ZEROES(LONG, "countLowZeroes", USIZE),
	LONG_COUNT_HIGH_ZEROES(LONG, "countHighZeroes", USIZE),
	LONG_COUNT_LOW_ONES(LONG, "countLowOnes", USIZE),
	LONG_COUNT_HIGH_ONES(LONG, "countHighOnes", USIZE),
	LONG_HIGHEST_ONE_BIT(LONG, "highestOneBit", new OptionalTypeID(USIZE)),
	LONG_LOWEST_ONE_BIT(LONG, "lowestOneBit", new OptionalTypeID(USIZE)),
	LONG_HIGHEST_ZERO_BIT(LONG, "highestZeroBit", new OptionalTypeID(USIZE)),
	LONG_LOWEST_ZERO_BIT(LONG, "lowestZeroBit", new OptionalTypeID(USIZE)),
	LONG_BIT_COUNT(LONG, "bitCount", USIZE),

	ULONG_INVERT(ULONG, INVERT, ULONG, ULONG),
	ULONG_INC(ULONG, INCREMENT, ULONG, ULONG),
	ULONG_DEC(ULONG, DECREMENT, ULONG, ULONG),
	ULONG_ADD_ULONG(ULONG, ADD, ULONG, ULONG),
	ULONG_SUB_ULONG(ULONG, NEG, ULONG, ULONG),
	ULONG_MUL_ULONG(ULONG, MUL, ULONG, ULONG),
	ULONG_DIV_ULONG(ULONG, DIV, ULONG, ULONG),
	ULONG_MOD_ULONG(ULONG, MOD, ULONG, ULONG),
	ULONG_AND_ULONG(ULONG, AND, ULONG, ULONG),
	ULONG_OR_ULONG(ULONG, OR, ULONG, ULONG),
	ULONG_XOR_ULONG(ULONG, XOR, ULONG, ULONG),
	ULONG_SHL(ULONG, SHL, ULONG, ULONG),
	ULONG_SHR(ULONG, SHR, ULONG, ULONG),
	ULONG_COMPARE(ULONG, COMPARE, INT, ULONG),
	ULONG_COMPARE_UINT(ULONG, COMPARE, INT, ULONG),
	ULONG_COMPARE_USIZE(ULONG, COMPARE, INT, ULONG),
	ULONG_TO_BYTE(ULONG, "as byte", BYTE, ULONG),
	ULONG_TO_SBYTE(ULONG, "as sbyte", SBYTE, ULONG),
	ULONG_TO_SHORT(ULONG, "as short", SHORT, ULONG),
	ULONG_TO_USHORT(ULONG, "as ushort", USHORT, ULONG),
	ULONG_TO_INT(ULONG, "as int", INT, ULONG),
	ULONG_TO_UINT(ULONG, "as uint", UINT, ULONG),
	ULONG_TO_LONG(ULONG, "as long", LONG, ULONG),
	ULONG_TO_USIZE(ULONG, "as usize", USIZE, ULONG),
	ULONG_TO_FLOAT(ULONG, "as float", FLOAT, ULONG),
	ULONG_TO_DOUBLE(ULONG, "as double", DOUBLE, ULONG),
	ULONG_TO_CHAR(ULONG, "as char", CHAR, ULONG),
	ULONG_TO_STRING(ULONG, "as string", STRING, ULONG),
	ULONG_PARSE(ULONG, "parse", ULONG, STRING),
	ULONG_PARSE_WITH_BASE(ULONG, "parse", ULONG, STRING, INT),
	ULONG_GET_MIN_VALUE(ULONG, "MIN_VALUE", ULONG),
	ULONG_GET_MAX_VALUE(ULONG, "MAX_VALUE", ULONG),
	ULONG_COUNT_LOW_ZEROES(ULONG, "countLowZeroes", USIZE),
	ULONG_COUNT_HIGH_ZEROES(ULONG, "countHighZeroes", USIZE),
	ULONG_COUNT_LOW_ONES(ULONG, "countLowOnes", USIZE),
	ULONG_COUNT_HIGH_ONES(ULONG, "countHighOnes", USIZE),
	ULONG_HIGHEST_ONE_BIT(ULONG, "highestOneBit", new OptionalTypeID(USIZE)),
	ULONG_LOWEST_ONE_BIT(ULONG, "lowestOneBit", new OptionalTypeID(USIZE)),
	ULONG_HIGHEST_ZERO_BIT(ULONG, "highestZeroBit", new OptionalTypeID(USIZE)),
	ULONG_LOWEST_ZERO_BIT(ULONG, "lowestZeroBit", new OptionalTypeID(USIZE)),
	ULONG_BIT_COUNT(ULONG, "bitCount", USIZE),

	USIZE_INVERT(USIZE, NOT, USIZE, USIZE),
	USIZE_INC(USIZE, INCREMENT, USIZE, USIZE),
	USIZE_DEC(USIZE, DECREMENT, USIZE, USIZE),
	USIZE_ADD_USIZE(USIZE, ADD, USIZE, USIZE),
	USIZE_SUB_USIZE(USIZE, SUB, USIZE, USIZE),
	USIZE_MUL_USIZE(USIZE, MUL, USIZE, USIZE),
	USIZE_DIV_USIZE(USIZE, DIV, USIZE, USIZE),
	USIZE_MOD_USIZE(USIZE, MOD, USIZE, USIZE),
	USIZE_AND_USIZE(USIZE, AND, USIZE, USIZE),
	USIZE_OR_USIZE(USIZE, OR, USIZE, USIZE),
	USIZE_XOR_USIZE(USIZE, XOR, USIZE, USIZE),
	USIZE_SHL(USIZE, SHL, USIZE, USIZE),
	USIZE_SHR(USIZE, SHR, USIZE, USIZE),
	USIZE_COMPARE(USIZE, COMPARE, INT, USIZE),
	USIZE_COMPARE_UINT(USIZE, COMPARE, INT, USIZE),
	USIZE_TO_BYTE(USIZE, "as byte", BYTE, USIZE),
	USIZE_TO_SBYTE(USIZE, "as sbyte", SBYTE, USIZE),
	USIZE_TO_SHORT(USIZE, "as short", SHORT, USIZE),
	USIZE_TO_USHORT(USIZE, "as ushort", USHORT, USIZE),
	USIZE_TO_INT(USIZE, "as int", INT, USIZE),
	USIZE_TO_UINT(USIZE, "as uint", UINT, USIZE),
	USIZE_TO_LONG(USIZE, "as long", LONG, USIZE),
	USIZE_TO_ULONG(USIZE, "as ulong", ULONG, USIZE),
	USIZE_TO_FLOAT(USIZE, "as float", FLOAT, USIZE),
	USIZE_TO_DOUBLE(USIZE, "as double", DOUBLE, USIZE),
	USIZE_TO_CHAR(USIZE, "as char", CHAR, USIZE),
	USIZE_TO_STRING(USIZE, "as string", STRING, USIZE),
	USIZE_PARSE(USIZE, "parse", USIZE, STRING),
	USIZE_PARSE_WITH_BASE(USIZE, "parse", USIZE, STRING, INT),
	USIZE_GET_MIN_VALUE(USIZE, "MIN_VALUE", USIZE),
	USIZE_GET_MAX_VALUE(USIZE, "MAX_VALUE", USIZE),
	USIZE_COUNT_LOW_ZEROES(USIZE, "countLowZeroes", USIZE),
	USIZE_COUNT_HIGH_ZEROES(USIZE, "countHighZeroes", USIZE),
	USIZE_COUNT_LOW_ONES(USIZE, "countLowOnes", USIZE),
	USIZE_COUNT_HIGH_ONES(USIZE, "countHighOnes", USIZE),
	USIZE_HIGHEST_ONE_BIT(USIZE, "highestOneBit", new OptionalTypeID(USIZE)),
	USIZE_LOWEST_ONE_BIT(USIZE, "lowestOneBit", new OptionalTypeID(USIZE)),
	USIZE_HIGHEST_ZERO_BIT(USIZE, "highestZeroBit", new OptionalTypeID(USIZE)),
	USIZE_LOWEST_ZERO_BIT(USIZE, "lowestZeroBit", new OptionalTypeID(USIZE)),
	USIZE_BIT_COUNT(USIZE, "bitCount", USIZE),
	USIZE_BITS(USIZE, "bits", USIZE),

	FLOAT_INVERT(FLOAT, INVERT, FLOAT, FLOAT),
	FLOAT_INC(FLOAT, INCREMENT, FLOAT, FLOAT),
	FLOAT_DEC(FLOAT, DECREMENT, FLOAT, FLOAT),
	FLOAT_ADD_FLOAT(FLOAT, ADD, FLOAT, FLOAT),
	FLOAT_SUB_FLOAT(FLOAT, SUB, FLOAT, FLOAT),
	FLOAT_MUL_FLOAT(FLOAT, MUL, FLOAT, FLOAT),
	FLOAT_DIV_FLOAT(FLOAT, DIV, FLOAT, FLOAT),
	FLOAT_MOD_FLOAT(FLOAT, MOD, FLOAT, FLOAT),
	FLOAT_COMPARE(FLOAT, COMPARE, INT, FLOAT),
	FLOAT_TO_BYTE(FLOAT, "as byte", BYTE, FLOAT),
	FLOAT_TO_SBYTE(FLOAT, "as sbyte", SBYTE, FLOAT),
	FLOAT_TO_SHORT(FLOAT, "as short", SHORT, FLOAT),
	FLOAT_TO_USHORT(FLOAT, "as ushort", USHORT, FLOAT),
	FLOAT_TO_INT(FLOAT, "as int", INT, FLOAT),
	FLOAT_TO_UINT(FLOAT, "as uint", UINT, FLOAT),
	FLOAT_TO_LONG(FLOAT, "as long", LONG, FLOAT),
	FLOAT_TO_ULONG(FLOAT, "as ulong", ULONG, FLOAT),
	FLOAT_TO_USIZE(FLOAT, "as usize", USIZE, FLOAT),
	FLOAT_TO_DOUBLE(FLOAT, "as double", DOUBLE, FLOAT),
	FLOAT_TO_STRING(FLOAT, "as string", STRING, FLOAT),
	FLOAT_BITS(FLOAT, "bits", FLOAT, UINT),
	FLOAT_FROM_BITS(FLOAT, "fromBits", FLOAT, UINT),
	FLOAT_PARSE(FLOAT, "parse", FLOAT, STRING),
	FLOAT_GET_MIN_VALUE(FLOAT, "MIN_VALUE", FLOAT),
	FLOAT_GET_MAX_VALUE(FLOAT, "MAX_VALUE", FLOAT),

	DOUBLE_INVERT(DOUBLE, INVERT, DOUBLE, DOUBLE),
	DOUBLE_INC(DOUBLE, INCREMENT, DOUBLE, DOUBLE),
	DOUBLE_DEC(DOUBLE, DECREMENT, DOUBLE, DOUBLE),
	DOUBLE_ADD_DOUBLE(DOUBLE, ADD, DOUBLE, DOUBLE),
	DOUBLE_SUB_DOUBLE(DOUBLE, SUB, DOUBLE, DOUBLE),
	DOUBLE_MUL_DOUBLE(DOUBLE, MUL, DOUBLE, DOUBLE),
	DOUBLE_DIV_DOUBLE(DOUBLE, DIV, DOUBLE, DOUBLE),
	DOUBLE_MOD_DOUBLE(DOUBLE, MOD, DOUBLE, DOUBLE),
	DOUBLE_COMPARE(DOUBLE, COMPARE, INT, DOUBLE),
	DOUBLE_TO_BYTE(DOUBLE, "as byte", BYTE, DOUBLE),
	DOUBLE_TO_SBYTE(DOUBLE, "as sbyte", SBYTE, DOUBLE),
	DOUBLE_TO_SHORT(DOUBLE, "as short", SHORT, DOUBLE),
	DOUBLE_TO_USHORT(DOUBLE, "as ushort", USHORT, DOUBLE),
	DOUBLE_TO_INT(DOUBLE, "as int", INT, DOUBLE),
	DOUBLE_TO_UINT(DOUBLE, "as uint", UINT, DOUBLE),
	DOUBLE_TO_LONG(DOUBLE, "as long", LONG, DOUBLE),
	DOUBLE_TO_ULONG(DOUBLE, "as ulong", ULONG, DOUBLE),
	DOUBLE_TO_USIZE(DOUBLE, "as usize", USIZE, DOUBLE),
	DOUBLE_TO_FLOAT(DOUBLE, "as float", FLOAT, DOUBLE),
	DOUBLE_TO_STRING(DOUBLE, "as string", STRING, DOUBLE),
	DOUBLE_BITS(DOUBLE, "bits", ULONG),
	DOUBLE_FROM_BITS(DOUBLE, "fromBits", DOUBLE, ULONG),
	DOUBLE_PARSE(DOUBLE, "parse", DOUBLE, STRING),
	DOUBLE_GET_MIN_VALUE(DOUBLE, "MIN_VALUE", DOUBLE),
	DOUBLE_GET_MAX_VALUE(DOUBLE, "MAX_VALUE", DOUBLE),

	CHAR_ADD_INT(CHAR, ADD, CHAR, CHAR),
	CHAR_SUB_INT(CHAR, SUB, CHAR, CHAR),
	CHAR_SUB_CHAR(CHAR, SUB, CHAR, CHAR),
	CHAR_COMPARE(CHAR, COMPARE, CHAR, CHAR),
	CHAR_TO_BYTE(CHAR, "as byte", BYTE, CHAR),
	CHAR_TO_SBYTE(CHAR, "as sbyte", SBYTE, CHAR),
	CHAR_TO_SHORT(CHAR, "as short", SHORT, CHAR),
	CHAR_TO_USHORT(CHAR, "as ushort", USHORT, CHAR),
	CHAR_TO_INT(CHAR, "as int", INT, CHAR),
	CHAR_TO_UINT(CHAR, "as uint", UINT, CHAR),
	CHAR_TO_LONG(CHAR, "as long", LONG, CHAR),
	CHAR_TO_ULONG(CHAR, "as ulong", ULONG, CHAR),
	CHAR_TO_USIZE(CHAR, "as usize", USIZE, CHAR),
	CHAR_TO_STRING(CHAR, "as string", STRING, CHAR),
	CHAR_GET_MIN_VALUE(CHAR, "MIN_VALUE", CHAR),
	CHAR_GET_MAX_VALUE(CHAR, "MAX_VALUE", CHAR),
	CHAR_REMOVE_DIACRITICS(CHAR, "removeDiacritics", CHAR),
	CHAR_TO_LOWER_CASE(CHAR, "toLowerCase", CHAR),
	CHAR_TO_UPPER_CASE(CHAR, "toUpperCase", CHAR),

	STRING_CONSTRUCTOR_CHARACTERS(STRING, "this", STRING, ArrayTypeID.CHAR),
	STRING_ADD_STRING(STRING, ADD, STRING, STRING) {
		@Override
		public Optional<CompileTimeConstant> evaluate(TypeID[] typeArguments, CompileTimeConstant[] arguments) {
			Optional<String> a = arguments[0].asString().map(s -> s.value);
			Optional<String> b = arguments[1].asString().map(s -> s.value);
			if (a.isPresent() && b.isPresent()) {
				return Optional.of(new StringConstant(a.get() + b.get()));
			} else {
				return Optional.empty();
			}
		}
	},
	STRING_COMPARE(STRING, COMPARE, INT, STRING),
	STRING_LENGTH(STRING, "length", USIZE),
	STRING_INDEXGET(STRING, INDEXGET, CHAR, USIZE),
	STRING_RANGEGET(STRING, INDEXGET, STRING, RangeTypeID.USIZE),
	STRING_CHARACTERS(STRING, "characters", ArrayTypeID.CHAR),
	STRING_ISEMPTY(STRING, "isEmpty", BOOL),
	STRING_REMOVE_DIACRITICS(STRING, "removeDiacritics", STRING),
	STRING_TRIM(STRING, "trim", STRING),
	STRING_TO_LOWER_CASE(STRING, "toLowerCase", STRING),
	STRING_TO_UPPER_CASE(STRING, "toUpperCase", STRING),
	STRING_CONTAINS_CHAR(STRING, CONTAINS, BOOL, CHAR),
	STRING_CONTAINS_STRING(STRING, CONTAINS, BOOL, STRING),

	ASSOC_CONSTRUCTOR(MapTypeSymbol.INSTANCE, "this", new AssocTypeID(MapTypeSymbol.KEY_TYPE, MapTypeSymbol.VALUE_TYPE)),
	ASSOC_INDEXGET(MapTypeSymbol.INSTANCE, INDEXGET, new OptionalTypeID(MapTypeSymbol.VALUE_TYPE), MapTypeSymbol.KEY_TYPE),
	ASSOC_INDEXSET(MapTypeSymbol.INSTANCE, INDEXSET, VOID, MapTypeSymbol.KEY_TYPE, MapTypeSymbol.VALUE_TYPE),
	ASSOC_CONTAINS(MapTypeSymbol.INSTANCE, CONTAINS, BOOL, MapTypeSymbol.KEY_TYPE),
	ASSOC_GETORDEFAULT(MapTypeSymbol.INSTANCE, "getOrDefault", MapTypeSymbol.VALUE_TYPE, MapTypeSymbol.KEY_TYPE, MapTypeSymbol.VALUE_TYPE),
	ASSOC_SIZE(MapTypeSymbol.INSTANCE, "size", USIZE),
	ASSOC_ISEMPTY(MapTypeSymbol.INSTANCE, "isEmpty", BOOL),
	ASSOC_KEYS(MapTypeSymbol.INSTANCE, "keys", new ArrayTypeID(MapTypeSymbol.KEY_TYPE)),
	ASSOC_VALUES(MapTypeSymbol.INSTANCE, "values", new ArrayTypeID(MapTypeSymbol.VALUE_TYPE)),
	ASSOC_HASHCODE(MapTypeSymbol.INSTANCE, "objectHashCode", UINT),
	ASSOC_EQUALS(MapTypeSymbol.INSTANCE, EQUALS, new AssocTypeID(MapTypeSymbol.KEY_TYPE, MapTypeSymbol.VALUE_TYPE)),
	ASSOC_NOTEQUALS(MapTypeSymbol.INSTANCE, NOTEQUALS, new AssocTypeID(MapTypeSymbol.KEY_TYPE, MapTypeSymbol.VALUE_TYPE)),
	ASSOC_SAME(MapTypeSymbol.INSTANCE, SAME, new AssocTypeID(MapTypeSymbol.KEY_TYPE, MapTypeSymbol.VALUE_TYPE)),
	ASSOC_NOTSAME(MapTypeSymbol.INSTANCE, NOTSAME, new AssocTypeID(MapTypeSymbol.KEY_TYPE, MapTypeSymbol.VALUE_TYPE)),

	GENERICMAP_CONSTRUCTOR(GenericMapTypeSymbol.INSTANCE, "this", GenericMapTypeSymbol.PROTOTYPE),
	GENERICMAP_GETOPTIONAL(
			GenericMapTypeSymbol.INSTANCE,
			INDEXGET,
			new FunctionHeader(
					new TypeParameter[] { GenericMapTypeSymbol.PARAMETER },
					new GenericTypeID(GenericMapTypeSymbol.VALUE),
					null,
					new FunctionParameter(new GenericTypeID(GenericMapTypeSymbol.PARAMETER)))),
	GENERICMAP_PUT(
			GenericMapTypeSymbol.INSTANCE,
			INDEXSET,
			new FunctionHeader(
					new TypeParameter[] { GenericMapTypeSymbol.PARAMETER },
					new GenericTypeID(GenericMapTypeSymbol.VALUE),
					null,
					new FunctionParameter(new GenericTypeID(GenericMapTypeSymbol.PARAMETER)),
					new FunctionParameter(new GenericTypeID(GenericMapTypeSymbol.VALUE))
			)),
	GENERICMAP_CONTAINS(GenericMapTypeSymbol.INSTANCE, CONTAINS, new FunctionHeader(
			new TypeParameter[] { GenericMapTypeSymbol.PARAMETER },
			BOOL,
			null,
			new FunctionParameter(new GenericTypeID(GenericMapTypeSymbol.PARAMETER))
	)),
	GENERICMAP_ADDALL(GenericMapTypeSymbol.INSTANCE, "addAll", FunctionHeader.PLACEHOLDER),
	GENERICMAP_SIZE(GenericMapTypeSymbol.INSTANCE, "size", USIZE),
	GENERICMAP_ISEMPTY(GenericMapTypeSymbol.INSTANCE, "isEmpty", BOOL),
	GENERICMAP_HASHCODE(GenericMapTypeSymbol.INSTANCE, "hashCode", UINT),
	GENERICMAP_SAME(GenericMapTypeSymbol.INSTANCE, SAME, BOOL),
	GENERICMAP_NOTSAME(GenericMapTypeSymbol.INSTANCE, NOTSAME, BOOL),

	ARRAY_CONSTRUCTOR_SIZED(ArrayTypeSymbol.ARRAY, "this", FunctionHeader.PLACEHOLDER),
	ARRAY_CONSTRUCTOR_INITIAL_VALUE(ArrayTypeSymbol.ARRAY, "this", FunctionHeader.PLACEHOLDER),
	ARRAY_CONSTRUCTOR_LAMBDA(ArrayTypeSymbol.ARRAY, "this", FunctionHeader.PLACEHOLDER),
	ARRAY_CONSTRUCTOR_PROJECTED(ArrayTypeSymbol.ARRAY, "this", FunctionHeader.PLACEHOLDER),
	ARRAY_CONSTRUCTOR_PROJECTED_INDEXED(ArrayTypeSymbol.ARRAY, "this", FunctionHeader.PLACEHOLDER),
	ARRAY_INDEXGET(ArrayTypeSymbol.ARRAY, "[]", FunctionHeader.PLACEHOLDER),
	ARRAY_INDEXSET(ArrayTypeSymbol.ARRAY, "[]=", FunctionHeader.PLACEHOLDER),
	// 1D arrays only
	ARRAY_INDEXGETRANGE(ArrayTypeSymbol.ARRAY, "[..]", new RangeTypeID(ArrayTypeSymbol.ELEMENT_TYPE)),
	ARRAY_CONTAINS(ArrayTypeSymbol.ARRAY, "in", BOOL, ArrayTypeSymbol.ELEMENT_TYPE),
	ARRAY_LENGTH1D(ArrayTypeSymbol.ARRAY, "length", USIZE),
	ARRAY_LENGTHMD(ArrayTypeSymbol.ARRAY, "length", new ArrayTypeID(USIZE, 1)),
	ARRAY_ISEMPTY(ArrayTypeSymbol.ARRAY, "isEmpty", BOOL),
	ARRAY_HASHCODE(ArrayTypeSymbol.ARRAY, "hashCode", UINT),
	ARRAY_EQUALS(ArrayTypeSymbol.ARRAY, "==", FunctionHeader.PLACEHOLDER),
	ARRAY_NOTEQUALS(ArrayTypeSymbol.ARRAY, "!=", FunctionHeader.PLACEHOLDER),
	ARRAY_SAME(ArrayTypeSymbol.ARRAY, "===", FunctionHeader.PLACEHOLDER),
	ARRAY_NOTSAME(ArrayTypeSymbol.ARRAY, "!==", FunctionHeader.PLACEHOLDER),

	SBYTE_ARRAY_AS_BYTE_ARRAY(ArrayTypeSymbol.ARRAY, "as byte[]", ArrayTypeID.BYTE),
	BYTE_ARRAY_AS_SBYTE_ARRAY(ArrayTypeSymbol.ARRAY, "as sbyte[]", ArrayTypeID.SBYTE),
	SHORT_ARRAY_AS_USHORT_ARRAY(ArrayTypeSymbol.ARRAY, "as ushort[]", ArrayTypeID.USHORT),
	USHORT_ARRAY_AS_SHORT_ARRAY(ArrayTypeSymbol.ARRAY, "as short[]", ArrayTypeID.SHORT),
	INT_ARRAY_AS_UINT_ARRAY(ArrayTypeSymbol.ARRAY, "as uint[]", ArrayTypeID.UINT),
	UINT_ARRAY_AS_INT_ARRAY(ArrayTypeSymbol.ARRAY, "as int[]", ArrayTypeID.INT),
	LONG_ARRAY_AS_ULONG_ARRAY(ArrayTypeSymbol.ARRAY, "as ulong[]", ArrayTypeID.ULONG),
	ULONG_ARRAY_AS_LONG_ARRAY(ArrayTypeSymbol.ARRAY, "as long[]", ArrayTypeID.LONG),

	FUNCTION_CALL(FunctionTypeSymbol.PLACEHOLDER, "()", FunctionHeader.PLACEHOLDER),
	FUNCTION_SAME(FunctionTypeSymbol.PLACEHOLDER, "===", FunctionHeader.PLACEHOLDER),
	FUNCTION_NOTSAME(FunctionTypeSymbol.PLACEHOLDER, "!==", FunctionHeader.PLACEHOLDER),

	CLASS_DEFAULT_CONSTRUCTOR(FunctionTypeSymbol.PLACEHOLDER, "this", FunctionHeader.PLACEHOLDER),
	STRUCT_EMPTY_CONSTRUCTOR(FunctionTypeSymbol.PLACEHOLDER, "this", VOID),
	ENUM_EMPTY_CONSTRUCTOR(FunctionTypeSymbol.PLACEHOLDER, "this", VOID),
	ENUM_NAME(FunctionTypeSymbol.PLACEHOLDER, "name", STRING),
	ENUM_ORDINAL(FunctionTypeSymbol.PLACEHOLDER, "ordinal", USIZE),
	ENUM_VALUES(FunctionTypeSymbol.PLACEHOLDER, "values", FunctionHeader.PLACEHOLDER),
	ENUM_TO_STRING(FunctionTypeSymbol.PLACEHOLDER, "as string", STRING),
	ENUM_COMPARE(FunctionTypeSymbol.PLACEHOLDER, COMPARE, FunctionHeader.PLACEHOLDER),


	METHOD_CALL(FunctionTypeSymbol.PLACEHOLDER, CALL, FunctionHeader.PLACEHOLDER),

	OBJECT_HASHCODE(FunctionTypeSymbol.PLACEHOLDER, "hashCode", UINT),
	OBJECT_SAME(FunctionTypeSymbol.PLACEHOLDER, "===", BOOL),
	OBJECT_NOTSAME(FunctionTypeSymbol.PLACEHOLDER, "!==", BOOL),

	RANGE_FROM(RangeTypeSymbol.INSTANCE, "from", new GenericTypeID(RangeTypeSymbol.PARAMETER)),
	RANGE_TO(RangeTypeSymbol.INSTANCE, "to", new GenericTypeID(RangeTypeSymbol.PARAMETER)),

	OPTIONAL_IS_NULL(OptionalTypeSymbol.INSTANCE, EQUALS, NULL),
	OPTIONAL_IS_NOT_NULL(OptionalTypeSymbol.INSTANCE, NOTEQUALS, NULL),

	ITERATOR_INT_RANGE(RangeTypeSymbol.INSTANCE, "iterator", FunctionHeader.PLACEHOLDER),
	ITERATOR_ARRAY_VALUES(ArrayTypeSymbol.ARRAY, "iterator", FunctionHeader.PLACEHOLDER),
	ITERATOR_ARRAY_KEY_VALUES(ArrayTypeSymbol.ARRAY, "iterator", FunctionHeader.PLACEHOLDER),
	ITERATOR_ASSOC_KEYS(MapTypeSymbol.INSTANCE, "iterator", FunctionHeader.PLACEHOLDER),
	ITERATOR_ASSOC_KEY_VALUES(MapTypeSymbol.INSTANCE, "iterator", FunctionHeader.PLACEHOLDER),
	ITERATOR_STRING_CHARS(STRING, "iterator", FunctionHeader.PLACEHOLDER),
	/*ITERATOR_ITERABLE()*/;

	private final TypeSymbol definingType;
	private final String name;
	private final OperatorType operator;
	private final FunctionHeader header;

	BuiltinMethodSymbol(TypeSymbol definingType, String name, FunctionHeader header) {
		this.definingType = definingType;
		this.name = name;
		this.operator = null;
		this.header = header;
	}

	BuiltinMethodSymbol(TypeSymbol definingType, String name, TypeID result, TypeID... parameters) {
		this.definingType = definingType;
		this.name = name;
		this.operator = null;
		header = new FunctionHeader(result, parameters);
	}

	BuiltinMethodSymbol(TypeSymbol definingType, OperatorType operator, FunctionHeader header) {
		this.definingType = definingType;
		this.name = operator.operator;
		this.operator = operator;
		this.header = header;
	}

	BuiltinMethodSymbol(TypeSymbol definingType, OperatorType operator, TypeID result, TypeID... parameters) {
		this.definingType = definingType;
		this.name = operator.operator;
		this.operator = operator;
		header = new FunctionHeader(result, parameters);
	}

	@Override
	public DefinitionSymbol getDefiningType() {
		return definingType;
	}

	@Override
	public TypeSymbol getTargetType() {
		return definingType;
	}

	@Override
	public Modifiers getModifiers() {
		return new Modifiers(Modifiers.PUBLIC | Modifiers.FINAL);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Optional<OperatorType> getOperator() {
		return Optional.ofNullable(operator);
	}

	@Override
	public FunctionHeader getHeader() {
		return header;
	}

	@Override
	public Optional<MethodInstance> getOverrides() {
		return Optional.empty();
	}
}
