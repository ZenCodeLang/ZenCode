package org.openzen.zenscript.codemodel.type.builtin;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.constant.CompileTimeConstant;
import org.openzen.zenscript.codemodel.constant.StringConstant;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.identifiers.DefinitionSymbol;
import org.openzen.zenscript.codemodel.identifiers.MethodID;
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
	BOOL_TO_STRING(BOOL, MethodID.caster(STRING), STRING, BOOL),
	BOOL_PARSE(BOOL, MethodID.staticMethod("parse"), BOOL, STRING),

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
	BYTE_TO_SBYTE(BYTE, MethodID.caster(SBYTE), SBYTE, BYTE),
	BYTE_TO_SHORT(BYTE, MethodID.caster(SHORT), SHORT, BYTE),
	BYTE_TO_USHORT(BYTE, MethodID.caster(USHORT), USHORT, BYTE),
	BYTE_TO_INT(BYTE, MethodID.caster(INT), INT, BYTE),
	BYTE_TO_UINT(BYTE, MethodID.caster(UINT), UINT, BYTE),
	BYTE_TO_LONG(BYTE, MethodID.caster(LONG), LONG, BYTE),
	BYTE_TO_ULONG(BYTE, MethodID.caster(ULONG), ULONG, BYTE),
	BYTE_TO_USIZE(BYTE, MethodID.caster(USIZE), USIZE, BYTE),
	BYTE_TO_FLOAT(BYTE, MethodID.caster(FLOAT), FLOAT, BYTE),
	BYTE_TO_DOUBLE(BYTE, MethodID.caster(DOUBLE), DOUBLE, BYTE),
	BYTE_TO_CHAR(BYTE, MethodID.caster(CHAR), CHAR, BYTE),
	BYTE_TO_STRING(BYTE, MethodID.caster(STRING), STRING, BYTE),
	BYTE_PARSE(BYTE, MethodID.staticMethod("parse"), BYTE, STRING),
	BYTE_PARSE_WITH_BASE(BYTE, MethodID.staticMethod("parse"), BYTE, STRING, INT),

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
	SBYTE_TO_BYTE(SBYTE, MethodID.caster(BYTE), BYTE, SBYTE),
	SBYTE_TO_SHORT(SBYTE, MethodID.caster(SHORT), SHORT, SBYTE),
	SBYTE_TO_USHORT(SBYTE, MethodID.caster(USHORT), USHORT, SBYTE),
	SBYTE_TO_INT(SBYTE, MethodID.caster(INT), INT, SBYTE),
	SBYTE_TO_UINT(SBYTE, MethodID.caster(UINT), UINT, SBYTE),
	SBYTE_TO_LONG(SBYTE, MethodID.caster(LONG), LONG, SBYTE),
	SBYTE_TO_ULONG(SBYTE, MethodID.caster(ULONG), ULONG, SBYTE),
	SBYTE_TO_USIZE(SBYTE, MethodID.caster(USIZE), USIZE, SBYTE),
	SBYTE_TO_FLOAT(SBYTE, MethodID.caster(FLOAT), FLOAT, SBYTE),
	SBYTE_TO_DOUBLE(SBYTE, MethodID.caster(DOUBLE), DOUBLE, SBYTE),
	SBYTE_TO_CHAR(SBYTE, MethodID.caster(CHAR), CHAR, SBYTE),
	SBYTE_TO_STRING(SBYTE, MethodID.caster(STRING), STRING, SBYTE),
	SBYTE_PARSE(SBYTE, MethodID.staticMethod("parse"), SBYTE, STRING, INT),
	SBYTE_PARSE_WITH_BASE(SBYTE, MethodID.staticMethod("parse"), SBYTE, STRING, INT),

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
	SHORT_TO_BYTE(SHORT, MethodID.caster(BYTE), BYTE, SHORT),
	SHORT_TO_SBYTE(SHORT, MethodID.caster(SBYTE), SBYTE, SHORT),
	SHORT_TO_USHORT(SHORT, MethodID.caster(USHORT), USHORT, SHORT),
	SHORT_TO_INT(SHORT, MethodID.caster(INT), INT, SHORT),
	SHORT_TO_UINT(SHORT, MethodID.caster(UINT), UINT, SHORT),
	SHORT_TO_LONG(SHORT, MethodID.caster(LONG), LONG, SHORT),
	SHORT_TO_ULONG(SHORT, MethodID.caster(ULONG), ULONG, SHORT),
	SHORT_TO_USIZE(SHORT, MethodID.caster(USIZE), USIZE, SHORT),
	SHORT_TO_FLOAT(SHORT, MethodID.caster(FLOAT), FLOAT, SHORT),
	SHORT_TO_DOUBLE(SHORT, MethodID.caster(DOUBLE), DOUBLE, SHORT),
	SHORT_TO_CHAR(SHORT, MethodID.caster(CHAR), CHAR, SHORT),
	SHORT_TO_STRING(SHORT, MethodID.caster(STRING), STRING, SHORT),
	SHORT_PARSE(SHORT, MethodID.staticMethod("parse"), SHORT, STRING, INT),
	SHORT_PARSE_WITH_BASE(SHORT, MethodID.staticMethod("parse"), SHORT, STRING, INT),

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
	USHORT_TO_BYTE(USHORT, MethodID.caster(BYTE), BYTE, USHORT),
	USHORT_TO_SBYTE(USHORT, MethodID.caster(SBYTE), SBYTE, USHORT),
	USHORT_TO_SHORT(USHORT, MethodID.caster(SHORT), SHORT, USHORT),
	USHORT_TO_INT(USHORT, MethodID.caster(INT), INT, USHORT),
	USHORT_TO_UINT(USHORT, MethodID.caster(UINT), UINT, USHORT),
	USHORT_TO_LONG(USHORT, MethodID.caster(LONG), LONG, USHORT),
	USHORT_TO_ULONG(USHORT, MethodID.caster(ULONG), ULONG, USHORT),
	USHORT_TO_USIZE(USHORT, MethodID.caster(USIZE), USIZE, USHORT),
	USHORT_TO_FLOAT(USHORT, MethodID.caster(FLOAT), FLOAT, USHORT),
	USHORT_TO_DOUBLE(USHORT, MethodID.caster(DOUBLE), DOUBLE, USHORT),
	USHORT_TO_CHAR(USHORT, MethodID.caster(CHAR), CHAR, USHORT),
	USHORT_TO_STRING(USHORT, MethodID.caster(STRING), STRING, USHORT),
	USHORT_PARSE(USHORT, MethodID.staticMethod("parse"), USHORT, STRING, INT),
	USHORT_PARSE_WITH_BASE(USHORT, MethodID.staticMethod("parse"), USHORT, STRING, INT),

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

	INT_TO_BYTE(INT, MethodID.caster(BYTE), BYTE, INT),
	INT_TO_SBYTE(INT, MethodID.caster(SBYTE), SBYTE, INT),
	INT_TO_SHORT(INT, MethodID.caster(SHORT), SHORT, INT),
	INT_TO_USHORT(INT, MethodID.caster(USHORT), USHORT, INT),
	INT_TO_UINT(INT, MethodID.caster(UINT), UINT, INT),
	INT_TO_LONG(INT, MethodID.caster(LONG), LONG, INT),
	INT_TO_ULONG(INT, MethodID.caster(ULONG), ULONG, INT),
	INT_TO_USIZE(INT, MethodID.caster(USIZE), USIZE, INT),
	INT_TO_FLOAT(INT, MethodID.caster(FLOAT), FLOAT, INT),
	INT_TO_DOUBLE(INT, MethodID.caster(DOUBLE), DOUBLE, INT),
	INT_TO_CHAR(INT, MethodID.caster(CHAR), CHAR, INT),
	INT_TO_STRING(INT, MethodID.caster(STRING), STRING, INT),

	INT_PARSE(INT, MethodID.staticMethod("parse"), INT, STRING),
	INT_PARSE_WITH_BASE(INT, MethodID.staticMethod("parse"), INT, STRING, INT),
	INT_COUNT_LOW_ZEROES(INT, MethodID.getter("countLowZeroes"), USIZE),
	INT_COUNT_HIGH_ZEROES(INT, MethodID.getter("countHighZeroes"), USIZE),
	INT_COUNT_LOW_ONES(INT, MethodID.getter("countLowOnes"), USIZE),
	INT_COUNT_HIGH_ONES(INT, MethodID.getter("countHighOnes"), USIZE),
	INT_HIGHEST_ONE_BIT(INT, MethodID.getter("highestOneBit"), new OptionalTypeID(USIZE)),
	INT_LOWEST_ONE_BIT(INT, MethodID.getter("lowestOneBit"), new OptionalTypeID(USIZE)),
	INT_HIGHEST_ZERO_BIT(INT, MethodID.getter("highestZeroBit"), new OptionalTypeID(USIZE)),
	INT_LOWEST_ZERO_BIT(INT, MethodID.getter("lowestZeroBit"), new OptionalTypeID(USIZE)),
	INT_BIT_COUNT(INT, MethodID.getter("bitCount"), USIZE),

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
	UINT_TO_BYTE(UINT, MethodID.caster(BYTE), BYTE, UINT),
	UINT_TO_SBYTE(UINT, MethodID.caster(SBYTE), SBYTE, UINT),
	UINT_TO_SHORT(UINT, MethodID.caster(SHORT), SHORT, UINT),
	UINT_TO_USHORT(UINT, MethodID.caster(USHORT), USHORT, UINT),
	UINT_TO_INT(UINT, MethodID.caster(INT), INT, UINT),
	UINT_TO_LONG(UINT, MethodID.caster(LONG), LONG, UINT),
	UINT_TO_ULONG(UINT, MethodID.caster(ULONG), ULONG, UINT),
	UINT_TO_USIZE(UINT, MethodID.caster(USIZE), USIZE, UINT),
	UINT_TO_FLOAT(UINT, MethodID.caster(FLOAT), FLOAT, UINT),
	UINT_TO_DOUBLE(UINT, MethodID.caster(DOUBLE), DOUBLE, UINT),
	UINT_TO_CHAR(UINT, MethodID.caster(CHAR), CHAR, UINT),
	UINT_TO_STRING(UINT, MethodID.caster(STRING), STRING, UINT),
	UINT_PARSE(UINT, MethodID.staticMethod("parse"), UINT, STRING),
	UINT_PARSE_WITH_BASE(UINT, MethodID.staticMethod("parse"), UINT, STRING, INT),
	UINT_COUNT_LOW_ZEROES(UINT, MethodID.getter("countLowZeroes"), USIZE),
	UINT_COUNT_HIGH_ZEROES(UINT, MethodID.getter("countHighZeroes"), USIZE),
	UINT_COUNT_LOW_ONES(UINT, MethodID.getter("countLowOnes"), USIZE),
	UINT_COUNT_HIGH_ONES(UINT, MethodID.getter("countHighOnes"), USIZE),
	UINT_HIGHEST_ONE_BIT(UINT, MethodID.getter("highestOneBit"), new OptionalTypeID(USIZE)),
	UINT_LOWEST_ONE_BIT(UINT, MethodID.getter("lowestOneBit"), new OptionalTypeID(USIZE)),
	UINT_HIGHEST_ZERO_BIT(UINT, MethodID.getter("highestZeroBit"), new OptionalTypeID(USIZE)),
	UINT_LOWEST_ZERO_BIT(UINT, MethodID.getter("lowestZeroBit"), new OptionalTypeID(USIZE)),
	UINT_BIT_COUNT(UINT, MethodID.getter("bitCount"), USIZE),

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
	LONG_TO_BYTE(LONG, MethodID.caster(BYTE), BYTE, LONG),
	LONG_TO_SBYTE(LONG, MethodID.caster(SBYTE), SBYTE, LONG),
	LONG_TO_SHORT(LONG, MethodID.caster(SHORT), SHORT, LONG),
	LONG_TO_USHORT(LONG, MethodID.caster(USHORT), USHORT, LONG),
	LONG_TO_INT(LONG, MethodID.caster(INT), INT, LONG),
	LONG_TO_UINT(LONG, MethodID.caster(UINT), UINT, LONG),
	LONG_TO_ULONG(LONG, MethodID.caster(ULONG), ULONG, LONG),
	LONG_TO_USIZE(LONG, MethodID.caster(USIZE), USIZE, LONG),
	LONG_TO_FLOAT(LONG, MethodID.caster(FLOAT), FLOAT, LONG),
	LONG_TO_DOUBLE(LONG, MethodID.caster(DOUBLE), DOUBLE, LONG),
	LONG_TO_CHAR(LONG, MethodID.caster(CHAR), CHAR, LONG),
	LONG_TO_STRING(LONG, MethodID.caster(STRING), STRING, LONG),
	LONG_PARSE(LONG, MethodID.staticMethod("parse"), LONG, STRING),
	LONG_PARSE_WITH_BASE(LONG, MethodID.staticMethod("parse"), LONG, STRING, INT),
	LONG_COUNT_LOW_ZEROES(LONG, MethodID.getter("countLowZeroes"), USIZE),
	LONG_COUNT_HIGH_ZEROES(LONG, MethodID.getter("countHighZeroes"), USIZE),
	LONG_COUNT_LOW_ONES(LONG, MethodID.getter("countLowOnes"), USIZE),
	LONG_COUNT_HIGH_ONES(LONG, MethodID.getter("countHighOnes"), USIZE),
	LONG_HIGHEST_ONE_BIT(LONG, MethodID.getter("highestOneBit"), new OptionalTypeID(USIZE)),
	LONG_LOWEST_ONE_BIT(LONG, MethodID.getter("lowestOneBit"), new OptionalTypeID(USIZE)),
	LONG_HIGHEST_ZERO_BIT(LONG, MethodID.getter("highestZeroBit"), new OptionalTypeID(USIZE)),
	LONG_LOWEST_ZERO_BIT(LONG, MethodID.getter("lowestZeroBit"), new OptionalTypeID(USIZE)),
	LONG_BIT_COUNT(LONG, MethodID.getter("bitCount"), USIZE),

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
	ULONG_TO_BYTE(ULONG, MethodID.caster(BYTE), BYTE, ULONG),
	ULONG_TO_SBYTE(ULONG, MethodID.caster(SBYTE), SBYTE, ULONG),
	ULONG_TO_SHORT(ULONG, MethodID.caster(SHORT), SHORT, ULONG),
	ULONG_TO_USHORT(ULONG, MethodID.caster(USHORT), USHORT, ULONG),
	ULONG_TO_INT(ULONG, MethodID.caster(INT), INT, ULONG),
	ULONG_TO_UINT(ULONG, MethodID.caster(UINT), UINT, ULONG),
	ULONG_TO_LONG(ULONG, MethodID.caster(LONG), LONG, ULONG),
	ULONG_TO_USIZE(ULONG, MethodID.caster(USIZE), USIZE, ULONG),
	ULONG_TO_FLOAT(ULONG, MethodID.caster(FLOAT), FLOAT, ULONG),
	ULONG_TO_DOUBLE(ULONG, MethodID.caster(DOUBLE), DOUBLE, ULONG),
	ULONG_TO_CHAR(ULONG, MethodID.caster(CHAR), CHAR, ULONG),
	ULONG_TO_STRING(ULONG, MethodID.caster(STRING), STRING, ULONG),
	ULONG_PARSE(ULONG, MethodID.staticMethod("parse"), ULONG, STRING),
	ULONG_PARSE_WITH_BASE(ULONG, MethodID.staticMethod("parse"), ULONG, STRING, INT),
	ULONG_COUNT_LOW_ZEROES(ULONG, MethodID.getter("countLowZeroes"), USIZE),
	ULONG_COUNT_HIGH_ZEROES(ULONG, MethodID.getter("countHighZeroes"), USIZE),
	ULONG_COUNT_LOW_ONES(ULONG, MethodID.getter("countLowOnes"), USIZE),
	ULONG_COUNT_HIGH_ONES(ULONG, MethodID.getter("countHighOnes"), USIZE),
	ULONG_HIGHEST_ONE_BIT(ULONG, MethodID.getter("highestOneBit"), new OptionalTypeID(USIZE)),
	ULONG_LOWEST_ONE_BIT(ULONG, MethodID.getter("lowestOneBit"), new OptionalTypeID(USIZE)),
	ULONG_HIGHEST_ZERO_BIT(ULONG, MethodID.getter("highestZeroBit"), new OptionalTypeID(USIZE)),
	ULONG_LOWEST_ZERO_BIT(ULONG, MethodID.getter("lowestZeroBit"), new OptionalTypeID(USIZE)),
	ULONG_BIT_COUNT(ULONG, MethodID.getter("bitCount"), USIZE),

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
	USIZE_TO_BYTE(USIZE, MethodID.caster(BYTE), BYTE, USIZE),
	USIZE_TO_SBYTE(USIZE, MethodID.caster(SBYTE), SBYTE, USIZE),
	USIZE_TO_SHORT(USIZE, MethodID.caster(SHORT), SHORT, USIZE),
	USIZE_TO_USHORT(USIZE, MethodID.caster(USHORT), USHORT, USIZE),
	USIZE_TO_INT(USIZE, MethodID.caster(INT), INT, USIZE),
	USIZE_TO_UINT(USIZE, MethodID.caster(UINT), UINT, USIZE),
	USIZE_TO_LONG(USIZE, MethodID.caster(LONG), LONG, USIZE),
	USIZE_TO_ULONG(USIZE, MethodID.caster(ULONG), ULONG, USIZE),
	USIZE_TO_FLOAT(USIZE, MethodID.caster(FLOAT), FLOAT, USIZE),
	USIZE_TO_DOUBLE(USIZE, MethodID.caster(DOUBLE), DOUBLE, USIZE),
	USIZE_TO_CHAR(USIZE, MethodID.caster(CHAR), CHAR, USIZE),
	USIZE_TO_STRING(USIZE, MethodID.caster(STRING), STRING, USIZE),
	USIZE_PARSE(USIZE, MethodID.staticMethod("parse"), USIZE, STRING),
	USIZE_PARSE_WITH_BASE(USIZE, MethodID.staticMethod("parse"), USIZE, STRING, INT),
	USIZE_COUNT_LOW_ZEROES(USIZE, MethodID.getter("countLowZeroes"), USIZE),
	USIZE_COUNT_HIGH_ZEROES(USIZE, MethodID.getter("countHighZeroes"), USIZE),
	USIZE_COUNT_LOW_ONES(USIZE, MethodID.getter("countLowOnes"), USIZE),
	USIZE_COUNT_HIGH_ONES(USIZE, MethodID.getter("countHighOnes"), USIZE),
	USIZE_HIGHEST_ONE_BIT(USIZE, MethodID.getter("highestOneBit"), new OptionalTypeID(USIZE)),
	USIZE_LOWEST_ONE_BIT(USIZE, MethodID.getter("lowestOneBit"), new OptionalTypeID(USIZE)),
	USIZE_HIGHEST_ZERO_BIT(USIZE, MethodID.getter("highestZeroBit"), new OptionalTypeID(USIZE)),
	USIZE_LOWEST_ZERO_BIT(USIZE, MethodID.getter("lowestZeroBit"), new OptionalTypeID(USIZE)),
	USIZE_BIT_COUNT(USIZE, MethodID.getter("bitCount"), USIZE),

	FLOAT_INVERT(FLOAT, INVERT, FLOAT, FLOAT),
	FLOAT_INC(FLOAT, INCREMENT, FLOAT, FLOAT),
	FLOAT_DEC(FLOAT, DECREMENT, FLOAT, FLOAT),
	FLOAT_ADD_FLOAT(FLOAT, ADD, FLOAT, FLOAT),
	FLOAT_SUB_FLOAT(FLOAT, SUB, FLOAT, FLOAT),
	FLOAT_MUL_FLOAT(FLOAT, MUL, FLOAT, FLOAT),
	FLOAT_DIV_FLOAT(FLOAT, DIV, FLOAT, FLOAT),
	FLOAT_MOD_FLOAT(FLOAT, MOD, FLOAT, FLOAT),
	FLOAT_COMPARE(FLOAT, COMPARE, INT, FLOAT),
	FLOAT_TO_BYTE(FLOAT, MethodID.caster(BYTE), BYTE, FLOAT),
	FLOAT_TO_SBYTE(FLOAT, MethodID.caster(SBYTE), SBYTE, FLOAT),
	FLOAT_TO_SHORT(FLOAT, MethodID.caster(SHORT), SHORT, FLOAT),
	FLOAT_TO_USHORT(FLOAT, MethodID.caster(USHORT), USHORT, FLOAT),
	FLOAT_TO_INT(FLOAT, MethodID.caster(INT), INT, FLOAT),
	FLOAT_TO_UINT(FLOAT, MethodID.caster(UINT), UINT, FLOAT),
	FLOAT_TO_LONG(FLOAT, MethodID.caster(LONG), LONG, FLOAT),
	FLOAT_TO_ULONG(FLOAT, MethodID.caster(ULONG), ULONG, FLOAT),
	FLOAT_TO_USIZE(FLOAT, MethodID.caster(USIZE), USIZE, FLOAT),
	FLOAT_TO_DOUBLE(FLOAT, MethodID.caster(DOUBLE), DOUBLE, FLOAT),
	FLOAT_TO_STRING(FLOAT, MethodID.caster(STRING), STRING, FLOAT),
	FLOAT_BITS(FLOAT, MethodID.getter("bits"), FLOAT, UINT),
	FLOAT_FROM_BITS(FLOAT, MethodID.staticMethod("fromBits"), FLOAT, UINT),
	FLOAT_PARSE(FLOAT, MethodID.staticMethod("parse"), FLOAT, STRING),

	DOUBLE_INVERT(DOUBLE, INVERT, DOUBLE, DOUBLE),
	DOUBLE_INC(DOUBLE, INCREMENT, DOUBLE, DOUBLE),
	DOUBLE_DEC(DOUBLE, DECREMENT, DOUBLE, DOUBLE),
	DOUBLE_ADD_DOUBLE(DOUBLE, ADD, DOUBLE, DOUBLE),
	DOUBLE_SUB_DOUBLE(DOUBLE, SUB, DOUBLE, DOUBLE),
	DOUBLE_MUL_DOUBLE(DOUBLE, MUL, DOUBLE, DOUBLE),
	DOUBLE_DIV_DOUBLE(DOUBLE, DIV, DOUBLE, DOUBLE),
	DOUBLE_MOD_DOUBLE(DOUBLE, MOD, DOUBLE, DOUBLE),
	DOUBLE_COMPARE(DOUBLE, COMPARE, INT, DOUBLE),
	DOUBLE_TO_BYTE(DOUBLE, MethodID.caster(BYTE), BYTE, DOUBLE),
	DOUBLE_TO_SBYTE(DOUBLE, MethodID.caster(SBYTE), SBYTE, DOUBLE),
	DOUBLE_TO_SHORT(DOUBLE, MethodID.caster(SHORT), SHORT, DOUBLE),
	DOUBLE_TO_USHORT(DOUBLE, MethodID.caster(USHORT), USHORT, DOUBLE),
	DOUBLE_TO_INT(DOUBLE, MethodID.caster(INT), INT, DOUBLE),
	DOUBLE_TO_UINT(DOUBLE, MethodID.caster(UINT), UINT, DOUBLE),
	DOUBLE_TO_LONG(DOUBLE, MethodID.caster(LONG), LONG, DOUBLE),
	DOUBLE_TO_ULONG(DOUBLE, MethodID.caster(ULONG), ULONG, DOUBLE),
	DOUBLE_TO_USIZE(DOUBLE, MethodID.caster(USIZE), USIZE, DOUBLE),
	DOUBLE_TO_FLOAT(DOUBLE, MethodID.caster(FLOAT), FLOAT, DOUBLE),
	DOUBLE_TO_STRING(DOUBLE, MethodID.caster(STRING), STRING, DOUBLE),
	DOUBLE_BITS(DOUBLE, MethodID.getter("bits"), ULONG),
	DOUBLE_FROM_BITS(DOUBLE, MethodID.staticMethod("fromBits"), DOUBLE, ULONG),
	DOUBLE_PARSE(DOUBLE, MethodID.staticMethod("parse"), DOUBLE, STRING),

	CHAR_ADD_INT(CHAR, ADD, CHAR, CHAR),
	CHAR_SUB_INT(CHAR, SUB, CHAR, CHAR),
	CHAR_SUB_CHAR(CHAR, SUB, CHAR, CHAR),
	CHAR_COMPARE(CHAR, COMPARE, CHAR, CHAR),
	CHAR_TO_BYTE(CHAR, MethodID.caster(BYTE), BYTE, CHAR),
	CHAR_TO_SBYTE(CHAR, MethodID.caster(SBYTE), SBYTE, CHAR),
	CHAR_TO_SHORT(CHAR, MethodID.caster(SHORT), SHORT, CHAR),
	CHAR_TO_USHORT(CHAR, MethodID.caster(USHORT), USHORT, CHAR),
	CHAR_TO_INT(CHAR, MethodID.caster(INT), INT, CHAR),
	CHAR_TO_UINT(CHAR, MethodID.caster(UINT), UINT, CHAR),
	CHAR_TO_LONG(CHAR, MethodID.caster(LONG), LONG, CHAR),
	CHAR_TO_ULONG(CHAR, MethodID.caster(ULONG), ULONG, CHAR),
	CHAR_TO_USIZE(CHAR, MethodID.caster(USIZE), USIZE, CHAR),
	CHAR_TO_STRING(CHAR, MethodID.caster(STRING), STRING, CHAR),
	CHAR_REMOVE_DIACRITICS(CHAR, MethodID.instanceMethod("removeDiacritics"), CHAR),
	CHAR_TO_LOWER_CASE(CHAR, MethodID.instanceMethod("toLowerCase"), CHAR),
	CHAR_TO_UPPER_CASE(CHAR, MethodID.instanceMethod("toUpperCase"), CHAR),

	STRING_CONSTRUCTOR_CHARACTERS(STRING, CONSTRUCTOR, STRING, ArrayTypeID.CHAR),
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
	STRING_LENGTH(STRING, MethodID.getter("length"), USIZE),
	STRING_INDEXGET(STRING, INDEXGET, CHAR, USIZE),
	STRING_RANGEGET(STRING, INDEXGET, STRING, RangeTypeID.USIZE),
	STRING_CHARACTERS(STRING, MethodID.getter("characters"), ArrayTypeID.CHAR),
	STRING_ISEMPTY(STRING, MethodID.getter("isEmpty"), BOOL),
	STRING_REMOVE_DIACRITICS(STRING, MethodID.instanceMethod("removeDiacritics"), STRING),
	STRING_TRIM(STRING, MethodID.instanceMethod("trim"), STRING),
	STRING_TO_LOWER_CASE(STRING, MethodID.instanceMethod("toLowerCase"), STRING),
	STRING_TO_UPPER_CASE(STRING, MethodID.instanceMethod("toUpperCase"), STRING),
	STRING_CONTAINS_CHAR(STRING, CONTAINS, BOOL, CHAR),
	STRING_CONTAINS_STRING(STRING, CONTAINS, BOOL, STRING),

	ASSOC_CONSTRUCTOR(MapTypeSymbol.INSTANCE, CONSTRUCTOR, new AssocTypeID(MapTypeSymbol.KEY_TYPE, MapTypeSymbol.VALUE_TYPE)),
	ASSOC_INDEXGET(MapTypeSymbol.INSTANCE, INDEXGET, new OptionalTypeID(MapTypeSymbol.VALUE_TYPE), MapTypeSymbol.KEY_TYPE),
	ASSOC_INDEXSET(MapTypeSymbol.INSTANCE, INDEXSET, VOID, MapTypeSymbol.KEY_TYPE, MapTypeSymbol.VALUE_TYPE),
	ASSOC_CONTAINS(MapTypeSymbol.INSTANCE, CONTAINS, BOOL, MapTypeSymbol.KEY_TYPE),
	ASSOC_GETORDEFAULT(MapTypeSymbol.INSTANCE, MethodID.instanceMethod("getOrDefault"), MapTypeSymbol.VALUE_TYPE, MapTypeSymbol.KEY_TYPE, MapTypeSymbol.VALUE_TYPE),
	ASSOC_SIZE(MapTypeSymbol.INSTANCE, MethodID.getter("size"), USIZE),
	ASSOC_ISEMPTY(MapTypeSymbol.INSTANCE, MethodID.getter("isEmpty"), BOOL),
	ASSOC_KEYS(MapTypeSymbol.INSTANCE, MethodID.getter("keys"), new ArrayTypeID(MapTypeSymbol.KEY_TYPE)),
	ASSOC_VALUES(MapTypeSymbol.INSTANCE, MethodID.getter("values"), new ArrayTypeID(MapTypeSymbol.VALUE_TYPE)),
	ASSOC_HASHCODE(MapTypeSymbol.INSTANCE, MethodID.getter("objectHashCode"), UINT),
	ASSOC_EQUALS(MapTypeSymbol.INSTANCE, EQUALS, new AssocTypeID(MapTypeSymbol.KEY_TYPE, MapTypeSymbol.VALUE_TYPE)),
	ASSOC_NOTEQUALS(MapTypeSymbol.INSTANCE, NOTEQUALS, new AssocTypeID(MapTypeSymbol.KEY_TYPE, MapTypeSymbol.VALUE_TYPE)),
	ASSOC_SAME(MapTypeSymbol.INSTANCE, SAME, new AssocTypeID(MapTypeSymbol.KEY_TYPE, MapTypeSymbol.VALUE_TYPE)),
	ASSOC_NOTSAME(MapTypeSymbol.INSTANCE, NOTSAME, new AssocTypeID(MapTypeSymbol.KEY_TYPE, MapTypeSymbol.VALUE_TYPE)),

	GENERICMAP_CONSTRUCTOR(GenericMapTypeSymbol.INSTANCE, CONSTRUCTOR, GenericMapTypeSymbol.PROTOTYPE),
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
	GENERICMAP_ADDALL(GenericMapTypeSymbol.INSTANCE, MethodID.instanceMethod("addAll"), FunctionHeader.PLACEHOLDER),
	GENERICMAP_SIZE(GenericMapTypeSymbol.INSTANCE, MethodID.getter("size"), USIZE),
	GENERICMAP_ISEMPTY(GenericMapTypeSymbol.INSTANCE, MethodID.getter("isEmpty"), BOOL),
	GENERICMAP_HASHCODE(GenericMapTypeSymbol.INSTANCE, MethodID.getter("hashCode"), UINT),
	GENERICMAP_SAME(GenericMapTypeSymbol.INSTANCE, SAME, BOOL),
	GENERICMAP_NOTSAME(GenericMapTypeSymbol.INSTANCE, NOTSAME, BOOL),

	ARRAY_CONSTRUCTOR_SIZED(ArrayTypeSymbol.ARRAY, CONSTRUCTOR, FunctionHeader.PLACEHOLDER),
	ARRAY_CONSTRUCTOR_INITIAL_VALUE(ArrayTypeSymbol.ARRAY, CONSTRUCTOR, FunctionHeader.PLACEHOLDER),
	ARRAY_CONSTRUCTOR_LAMBDA(ArrayTypeSymbol.ARRAY, CONSTRUCTOR, FunctionHeader.PLACEHOLDER),
	ARRAY_CONSTRUCTOR_PROJECTED(ArrayTypeSymbol.ARRAY, CONSTRUCTOR, FunctionHeader.PLACEHOLDER),
	ARRAY_CONSTRUCTOR_PROJECTED_INDEXED(ArrayTypeSymbol.ARRAY, CONSTRUCTOR, FunctionHeader.PLACEHOLDER),
	ARRAY_INDEXGET(ArrayTypeSymbol.ARRAY, INDEXGET, FunctionHeader.PLACEHOLDER),
	ARRAY_INDEXSET(ArrayTypeSymbol.ARRAY, INDEXSET, FunctionHeader.PLACEHOLDER),
	// 1D arrays only
	ARRAY_INDEXGETRANGE(ArrayTypeSymbol.ARRAY, INDEXGET, new RangeTypeID(ArrayTypeSymbol.ELEMENT_TYPE)),
	ARRAY_CONTAINS(ArrayTypeSymbol.ARRAY, CONTAINS, BOOL, ArrayTypeSymbol.ELEMENT_TYPE),
	ARRAY_LENGTH1D(ArrayTypeSymbol.ARRAY, MethodID.getter("length"), USIZE),
	ARRAY_LENGTHMD(ArrayTypeSymbol.ARRAY, MethodID.getter("length"), new ArrayTypeID(USIZE)),
	ARRAY_ISEMPTY(ArrayTypeSymbol.ARRAY, MethodID.getter("isEmpty"), BOOL),
	ARRAY_HASHCODE(ArrayTypeSymbol.ARRAY, MethodID.getter("hashCode"), UINT),
	ARRAY_EQUALS(ArrayTypeSymbol.ARRAY, EQUALS, FunctionHeader.PLACEHOLDER),
	ARRAY_NOTEQUALS(ArrayTypeSymbol.ARRAY, NOTEQUALS, FunctionHeader.PLACEHOLDER),
	ARRAY_SAME(ArrayTypeSymbol.ARRAY, SAME, FunctionHeader.PLACEHOLDER),
	ARRAY_NOTSAME(ArrayTypeSymbol.ARRAY, NOTSAME, FunctionHeader.PLACEHOLDER),

	SBYTE_ARRAY_AS_BYTE_ARRAY(ArrayTypeSymbol.ARRAY, MethodID.caster(new ArrayTypeID(BYTE)), ArrayTypeID.BYTE),
	BYTE_ARRAY_AS_SBYTE_ARRAY(ArrayTypeSymbol.ARRAY, MethodID.caster(new ArrayTypeID(SBYTE)), ArrayTypeID.SBYTE),
	SHORT_ARRAY_AS_USHORT_ARRAY(ArrayTypeSymbol.ARRAY, MethodID.caster(new ArrayTypeID(USHORT)), ArrayTypeID.USHORT),
	USHORT_ARRAY_AS_SHORT_ARRAY(ArrayTypeSymbol.ARRAY, MethodID.caster(new ArrayTypeID(SHORT)), ArrayTypeID.SHORT),
	INT_ARRAY_AS_UINT_ARRAY(ArrayTypeSymbol.ARRAY, MethodID.caster(new ArrayTypeID(UINT)), ArrayTypeID.UINT),
	UINT_ARRAY_AS_INT_ARRAY(ArrayTypeSymbol.ARRAY, MethodID.caster(new ArrayTypeID(INT)), ArrayTypeID.INT),
	LONG_ARRAY_AS_ULONG_ARRAY(ArrayTypeSymbol.ARRAY, MethodID.caster(new ArrayTypeID(ULONG)), ArrayTypeID.ULONG),
	ULONG_ARRAY_AS_LONG_ARRAY(ArrayTypeSymbol.ARRAY, MethodID.caster(new ArrayTypeID(LONG)), ArrayTypeID.LONG),

	FUNCTION_CALL(FunctionTypeSymbol.PLACEHOLDER, CALL, FunctionHeader.PLACEHOLDER),
	FUNCTION_SAME(FunctionTypeSymbol.PLACEHOLDER, SAME, FunctionHeader.PLACEHOLDER),
	FUNCTION_NOTSAME(FunctionTypeSymbol.PLACEHOLDER, NOTSAME, FunctionHeader.PLACEHOLDER),

	CLASS_DEFAULT_CONSTRUCTOR(FunctionTypeSymbol.PLACEHOLDER, CONSTRUCTOR, FunctionHeader.PLACEHOLDER),
	CLASS_EMPTY_CONSTRUCTOR(FunctionTypeSymbol.PLACEHOLDER, CONSTRUCTOR, FunctionHeader.EMPTY),
	STRUCT_EMPTY_CONSTRUCTOR(FunctionTypeSymbol.PLACEHOLDER, CONSTRUCTOR, VOID),
	STRUCT_DEFAULT_CONSTRUCTOR(FunctionTypeSymbol.PLACEHOLDER, CONSTRUCTOR, VOID),
	ENUM_EMPTY_CONSTRUCTOR(FunctionTypeSymbol.PLACEHOLDER, CONSTRUCTOR, VOID),
	ENUM_NAME(FunctionTypeSymbol.PLACEHOLDER, MethodID.instanceMethod("name"), STRING),
	ENUM_ORDINAL(FunctionTypeSymbol.PLACEHOLDER, MethodID.instanceMethod("ordinal"), USIZE),
	ENUM_VALUES(FunctionTypeSymbol.PLACEHOLDER, MethodID.staticGetter("values"), FunctionHeader.PLACEHOLDER),
	ENUM_TO_STRING(FunctionTypeSymbol.PLACEHOLDER, MethodID.caster(STRING), STRING),
	ENUM_COMPARE(FunctionTypeSymbol.PLACEHOLDER, COMPARE, FunctionHeader.PLACEHOLDER),


	METHOD_CALL(FunctionTypeSymbol.PLACEHOLDER, MethodID.staticOperator(CALL), FunctionHeader.PLACEHOLDER),

	OBJECT_HASHCODE(FunctionTypeSymbol.PLACEHOLDER, MethodID.instanceMethod("hashCode"), UINT),
	OBJECT_SAME(FunctionTypeSymbol.PLACEHOLDER, SAME, BOOL),
	OBJECT_NOTSAME(FunctionTypeSymbol.PLACEHOLDER, NOTSAME, BOOL),

	RANGE_FROM(RangeTypeSymbol.INSTANCE, MethodID.getter("from"), new GenericTypeID(RangeTypeSymbol.PARAMETER)),
	RANGE_TO(RangeTypeSymbol.INSTANCE, MethodID.getter("to"), new GenericTypeID(RangeTypeSymbol.PARAMETER)),

	OPTIONAL_IS_NULL(OptionalTypeSymbol.INSTANCE, EQUALS, NULL),
	OPTIONAL_IS_NOT_NULL(OptionalTypeSymbol.INSTANCE, NOTEQUALS, NULL),

	ITERATOR_INT_RANGE(RangeTypeSymbol.INSTANCE, MethodID.iterator(1), FunctionHeader.PLACEHOLDER),
	ITERATOR_ARRAY_VALUES(ArrayTypeSymbol.ARRAY, MethodID.iterator(1), FunctionHeader.PLACEHOLDER),
	ITERATOR_ARRAY_KEY_VALUES(ArrayTypeSymbol.ARRAY, MethodID.iterator(2), FunctionHeader.PLACEHOLDER),
	ITERATOR_ASSOC_KEYS(MapTypeSymbol.INSTANCE, MethodID.iterator(1), FunctionHeader.PLACEHOLDER),
	ITERATOR_ASSOC_KEY_VALUES(MapTypeSymbol.INSTANCE, MethodID.iterator(2), FunctionHeader.PLACEHOLDER),
	ITERATOR_STRING_CHARS(STRING, MethodID.iterator(1), FunctionHeader.PLACEHOLDER),
	/*ITERATOR_ITERABLE()*/;

	private final TypeSymbol definingType;
	private final TypeID type;
	private final MethodID id;
	private final FunctionHeader header;
	private final Modifiers modifiers;

	BuiltinMethodSymbol(TypeSymbol definingType, MethodID id, FunctionHeader header) {
		this.definingType = definingType;
		this.type = (definingType instanceof BasicTypeID) ? (BasicTypeID)definingType : DefinitionTypeID.createThis(definingType);
		this.id = id;
		this.header = header;
		this.modifiers = id.isStatic() ? Modifiers.PUBLIC_STATIC : Modifiers.PUBLIC;
	}

	BuiltinMethodSymbol(TypeSymbol definingType, MethodID id, TypeID result, TypeID... parameters) {
		this.definingType = definingType;
		this.type = (definingType instanceof BasicTypeID) ? (BasicTypeID)definingType : DefinitionTypeID.createThis(definingType);
		this.id = id;
		this.modifiers = id.isStatic() ? Modifiers.PUBLIC_STATIC : Modifiers.PUBLIC;
		header = new FunctionHeader(result, parameters);
	}

	BuiltinMethodSymbol(TypeSymbol definingType, OperatorType operator, FunctionHeader header) {
		this.definingType = definingType;
		this.type = (definingType instanceof BasicTypeID) ? (BasicTypeID)definingType : DefinitionTypeID.createThis(definingType);
		this.id = operator == OperatorType.CONSTRUCTOR ? MethodID.staticOperator(operator) : MethodID.operator(operator);
		this.modifiers = id.isStatic() ? Modifiers.PUBLIC_STATIC : Modifiers.PUBLIC;
		this.header = header;
	}

	BuiltinMethodSymbol(TypeSymbol definingType, OperatorType operator, TypeID result, TypeID... parameters) {
		this.definingType = definingType;
		this.type = (definingType instanceof BasicTypeID) ? (BasicTypeID)definingType : DefinitionTypeID.createThis(definingType);
		this.id = operator == OperatorType.CONSTRUCTOR ? MethodID.staticOperator(operator) : MethodID.operator(operator);
		this.modifiers = id.isStatic() ? Modifiers.PUBLIC_STATIC : Modifiers.PUBLIC;
		header = new FunctionHeader(result, parameters);
	}

	@Override
	public DefinitionSymbol getDefiningType() {
		return definingType;
	}

	@Override
	public TypeID getTargetType() {
		return type;
	}

	@Override
	public Modifiers getModifiers() {
		return modifiers;
	}

	@Override
	public MethodID getID() {
		return id;
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
