package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.compilation.TypeBuilder;
import static org.openzen.zenscript.codemodel.type.BasicTypeID.*;

import org.openzen.zenscript.codemodel.type.ArrayTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Optional;

public class UnaryExpression extends Expression {
	public enum OperatorGroup {
		BOOLEAN_NOT,
		BITWISE_NOT,
		NEG,
		PARSE,
		CAST_IMPLICIT,
		CAST_EXPLICIT,

		COUNT_LOW_ZEROES,
		COUNT_HIGH_ZEROES,
		COUNT_LOW_ONES,
		COUNT_HIGH_ONES,
		HIGHEST_ONE_BIT,
		LOWEST_ONE_BIT,
		HIGHEST_ZERO_BIT,
		LOWEST_ZERO_BIT,
		BIT_COUNT,

		OTHER
	}

	public enum Operator {
		BOOL_NOT(OperatorGroup.BOOLEAN_NOT, BOOL, BOOL),
		BOOL_PARSE(OperatorGroup.PARSE, BOOL, BOOL),
		BOOL_TO_STRING(OperatorGroup.CAST_IMPLICIT, BOOL, STRING),

		BYTE_NOT(OperatorGroup.BITWISE_NOT, BYTE, BYTE),
		BYTE_TO_SBYTE(OperatorGroup.CAST_IMPLICIT, BYTE, SBYTE),
		BYTE_TO_SHORT(OperatorGroup.CAST_IMPLICIT, BYTE, SHORT),
		BYTE_TO_USHORT(OperatorGroup.CAST_IMPLICIT, BYTE, USHORT),
		BYTE_TO_INT(OperatorGroup.CAST_IMPLICIT, BYTE, INT),
		BYTE_TO_UINT(OperatorGroup.CAST_IMPLICIT, BYTE, UINT),
		BYTE_TO_LONG(OperatorGroup.CAST_IMPLICIT, BYTE, LONG),
		BYTE_TO_ULONG(OperatorGroup.CAST_IMPLICIT, BYTE, ULONG),
		BYTE_TO_USIZE(OperatorGroup.CAST_IMPLICIT, BYTE, USIZE),
		BYTE_TO_FLOAT(OperatorGroup.CAST_IMPLICIT, BYTE, FLOAT),
		BYTE_TO_DOUBLE(OperatorGroup.CAST_IMPLICIT, BYTE, DOUBLE),
		BYTE_TO_STRING(OperatorGroup.CAST_IMPLICIT, BYTE, STRING),
		BYTE_PARSE(OperatorGroup.PARSE, STRING, BYTE),

		SBYTE_NOT(OperatorGroup.BITWISE_NOT, SBYTE, SBYTE),
		SBYTE_NEG(OperatorGroup.NEG, SBYTE, SBYTE),
		SBYTE_TO_BYTE(OperatorGroup.CAST_IMPLICIT, SBYTE, BYTE),
		SBYTE_TO_SHORT(OperatorGroup.CAST_IMPLICIT, SBYTE, SHORT),
		SBYTE_TO_USHORT(OperatorGroup.CAST_IMPLICIT, SBYTE, USHORT),
		SBYTE_TO_INT(OperatorGroup.CAST_IMPLICIT, SBYTE, INT),
		SBYTE_TO_UINT(OperatorGroup.CAST_IMPLICIT, SBYTE, UINT),
		SBYTE_TO_LONG(OperatorGroup.CAST_IMPLICIT, SBYTE, LONG),
		SBYTE_TO_ULONG(OperatorGroup.CAST_IMPLICIT, SBYTE, ULONG),
		SBYTE_TO_USIZE(OperatorGroup.CAST_IMPLICIT, SBYTE, USIZE),
		SBYTE_TO_FLOAT(OperatorGroup.CAST_IMPLICIT, SBYTE, FLOAT),
		SBYTE_TO_DOUBLE(OperatorGroup.CAST_IMPLICIT, SBYTE, DOUBLE),
		SBYTE_TO_STRING(OperatorGroup.CAST_IMPLICIT, SBYTE, STRING),
		SBYTE_PARSE(OperatorGroup.PARSE, STRING, SBYTE),

		SHORT_NOT(OperatorGroup.BITWISE_NOT, SHORT, SHORT),
		SHORT_NEG(OperatorGroup.NEG, SHORT, SHORT),
		SHORT_TO_BYTE(OperatorGroup.CAST_EXPLICIT, SHORT, BYTE),
		SHORT_TO_SBYTE(OperatorGroup.CAST_EXPLICIT, SHORT, SBYTE),
		SHORT_TO_USHORT(OperatorGroup.CAST_IMPLICIT, SHORT, USHORT),
		SHORT_TO_INT(OperatorGroup.CAST_IMPLICIT, SHORT, INT),
		SHORT_TO_UINT(OperatorGroup.CAST_IMPLICIT, SHORT, UINT),
		SHORT_TO_LONG(OperatorGroup.CAST_IMPLICIT, SHORT, LONG),
		SHORT_TO_ULONG(OperatorGroup.CAST_IMPLICIT, SHORT, ULONG),
		SHORT_TO_USIZE(OperatorGroup.CAST_IMPLICIT, SHORT, USIZE),
		SHORT_TO_FLOAT(OperatorGroup.CAST_IMPLICIT, SHORT, FLOAT),
		SHORT_TO_DOUBLE(OperatorGroup.CAST_IMPLICIT, SHORT, DOUBLE),
		SHORT_TO_STRING(OperatorGroup.CAST_IMPLICIT, SHORT, STRING),
		SHORT_PARSE(OperatorGroup.PARSE, STRING, SHORT),

		USHORT_NOT(OperatorGroup.BITWISE_NOT, USHORT, USHORT),
		USHORT_TO_BYTE(OperatorGroup.CAST_EXPLICIT, USHORT, BYTE),
		USHORT_TO_SBYTE(OperatorGroup.CAST_EXPLICIT, USHORT, SBYTE),
		USHORT_TO_SHORT(OperatorGroup.CAST_IMPLICIT, USHORT, SHORT),
		USHORT_TO_INT(OperatorGroup.CAST_IMPLICIT, USHORT, INT),
		USHORT_TO_UINT(OperatorGroup.CAST_IMPLICIT, USHORT, UINT),
		USHORT_TO_LONG(OperatorGroup.CAST_IMPLICIT, USHORT, LONG),
		USHORT_TO_ULONG(OperatorGroup.CAST_IMPLICIT, USHORT, ULONG),
		USHORT_TO_USIZE(OperatorGroup.CAST_IMPLICIT, USHORT, USIZE),
		USHORT_TO_FLOAT(OperatorGroup.CAST_IMPLICIT, USHORT, FLOAT),
		USHORT_TO_DOUBLE(OperatorGroup.CAST_IMPLICIT, USHORT, DOUBLE),
		USHORT_TO_STRING(OperatorGroup.CAST_IMPLICIT, USHORT, STRING),
		USHORT_PARSE(OperatorGroup.PARSE, STRING, USHORT),

		INT_NOT(OperatorGroup.BITWISE_NOT, INT, INT),
		INT_NEG(OperatorGroup.NEG, INT, INT),
		INT_TO_BYTE(OperatorGroup.CAST_EXPLICIT, INT, BYTE),
		INT_TO_SBYTE(OperatorGroup.CAST_EXPLICIT, INT, SBYTE),
		INT_TO_SHORT(OperatorGroup.CAST_EXPLICIT, INT, SHORT),
		INT_TO_USHORT(OperatorGroup.CAST_EXPLICIT, INT, USHORT),
		INT_TO_UINT(OperatorGroup.CAST_IMPLICIT, INT, UINT),
		INT_TO_LONG(OperatorGroup.CAST_IMPLICIT, INT, LONG),
		INT_TO_ULONG(OperatorGroup.CAST_IMPLICIT, INT, ULONG),
		INT_TO_USIZE(OperatorGroup.CAST_IMPLICIT, INT, USIZE),
		INT_TO_FLOAT(OperatorGroup.CAST_IMPLICIT, INT, FLOAT),
		INT_TO_DOUBLE(OperatorGroup.CAST_IMPLICIT, INT, DOUBLE),
		INT_TO_STRING(OperatorGroup.CAST_IMPLICIT, INT, STRING),
		INT_PARSE(OperatorGroup.PARSE, STRING, INT),
		INT_COUNT_LOW_ZEROES(OperatorGroup.COUNT_LOW_ZEROES, INT, INT),
		INT_COUNT_HIGH_ZEROES(OperatorGroup.COUNT_HIGH_ZEROES, INT, INT),
		INT_COUNT_LOW_ONES(OperatorGroup.COUNT_LOW_ONES, INT, INT),
		INT_COUNT_HIGH_ONES(OperatorGroup.COUNT_HIGH_ONES, INT, INT),
		INT_HIGHEST_ONE_BIT(OperatorGroup.HIGHEST_ONE_BIT, INT, INT),
		INT_LOWEST_ONE_BIT(OperatorGroup.LOWEST_ONE_BIT, INT, INT),
		INT_HIGHEST_ZERO_BIT(OperatorGroup.HIGHEST_ZERO_BIT, INT, INT),
		INT_LOWEST_ZERO_BIT(OperatorGroup.LOWEST_ZERO_BIT, INT, INT),
		INT_BIT_COUNT(OperatorGroup.BIT_COUNT, INT, INT),

		UINT_NOT(OperatorGroup.BITWISE_NOT, UINT, UINT),
		UINT_TO_BYTE(OperatorGroup.CAST_EXPLICIT, UINT, BYTE),
		UINT_TO_SBYTE(OperatorGroup.CAST_EXPLICIT, UINT, SBYTE),
		UINT_TO_SHORT(OperatorGroup.CAST_EXPLICIT, UINT, SHORT),
		UINT_TO_USHORT(OperatorGroup.CAST_EXPLICIT, UINT, USHORT),
		UINT_TO_INT(OperatorGroup.CAST_IMPLICIT, UINT, INT),
		UINT_TO_LONG(OperatorGroup.CAST_IMPLICIT, UINT, LONG),
		UINT_TO_ULONG(OperatorGroup.CAST_IMPLICIT, UINT, ULONG),
		UINT_TO_USIZE(OperatorGroup.CAST_IMPLICIT, UINT, USIZE),
		UINT_TO_FLOAT(OperatorGroup.CAST_IMPLICIT, UINT, FLOAT),
		UINT_TO_DOUBLE(OperatorGroup.CAST_IMPLICIT, UINT, DOUBLE),
		UINT_TO_STRING(OperatorGroup.CAST_IMPLICIT, UINT, STRING),
		UINT_PARSE(OperatorGroup.PARSE, UINT, UINT),
		UINT_COUNT_LOW_ZEROES(OperatorGroup.COUNT_LOW_ZEROES, UINT, INT),
		UINT_COUNT_HIGH_ZEROES(OperatorGroup.COUNT_HIGH_ZEROES, UINT, INT),
		UINT_COUNT_LOW_ONES(OperatorGroup.COUNT_LOW_ONES, UINT, INT),
		UINT_COUNT_HIGH_ONES(OperatorGroup.COUNT_HIGH_ONES, UINT, INT),
		UINT_HIGHEST_ONE_BIT(OperatorGroup.HIGHEST_ONE_BIT, UINT, INT),
		UINT_LOWEST_ONE_BIT(OperatorGroup.LOWEST_ONE_BIT, UINT, INT),
		UINT_HIGHEST_ZERO_BIT(OperatorGroup.HIGHEST_ZERO_BIT, UINT, INT),
		UINT_LOWEST_ZERO_BIT(OperatorGroup.LOWEST_ZERO_BIT, UINT, INT),
		UINT_BIT_COUNT(OperatorGroup.BIT_COUNT, UINT, INT),

		LONG_NOT(OperatorGroup.BITWISE_NOT, LONG, LONG),
		LONG_NEG(OperatorGroup.NEG, LONG, LONG),
		LONG_TO_BYTE(OperatorGroup.CAST_EXPLICIT, LONG, BYTE),
		LONG_TO_SBYTE(OperatorGroup.CAST_EXPLICIT, LONG, SBYTE),
		LONG_TO_SHORT(OperatorGroup.CAST_EXPLICIT, LONG, SHORT),
		LONG_TO_USHORT(OperatorGroup.CAST_EXPLICIT, LONG, USHORT),
		LONG_TO_INT(OperatorGroup.CAST_EXPLICIT, LONG, INT),
		LONG_TO_UINT(OperatorGroup.CAST_EXPLICIT, LONG, UINT),
		LONG_TO_ULONG(OperatorGroup.CAST_IMPLICIT, LONG, ULONG),
		LONG_TO_USIZE(OperatorGroup.CAST_EXPLICIT, LONG, USIZE),
		LONG_TO_FLOAT(OperatorGroup.CAST_IMPLICIT, LONG, FLOAT),
		LONG_TO_DOUBLE(OperatorGroup.CAST_IMPLICIT, LONG, DOUBLE),
		LONG_TO_STRING(OperatorGroup.CAST_IMPLICIT, LONG, STRING),
		LONG_PARSE(OperatorGroup.PARSE, STRING, LONG),
		LONG_COUNT_LOW_ZEROES(OperatorGroup.COUNT_LOW_ZEROES, LONG, INT),
		LONG_COUNT_HIGH_ZEROES(OperatorGroup.COUNT_HIGH_ZEROES, LONG, INT),
		LONG_COUNT_LOW_ONES(OperatorGroup.COUNT_LOW_ONES, LONG, INT),
		LONG_COUNT_HIGH_ONES(OperatorGroup.COUNT_HIGH_ONES, LONG, INT),
		LONG_HIGHEST_ONE_BIT(OperatorGroup.HIGHEST_ONE_BIT, LONG, INT),
		LONG_LOWEST_ONE_BIT(OperatorGroup.LOWEST_ONE_BIT, LONG, INT),
		LONG_HIGHEST_ZERO_BIT(OperatorGroup.HIGHEST_ZERO_BIT, LONG, INT),
		LONG_LOWEST_ZERO_BIT(OperatorGroup.LOWEST_ZERO_BIT, LONG, INT),
		LONG_BIT_COUNT(OperatorGroup.BIT_COUNT, LONG, INT),

		ULONG_NOT(OperatorGroup.BITWISE_NOT, ULONG, ULONG),
		ULONG_TO_BYTE(OperatorGroup.CAST_EXPLICIT, ULONG, BYTE),
		ULONG_TO_SBYTE(OperatorGroup.CAST_EXPLICIT, ULONG, SBYTE),
		ULONG_TO_SHORT(OperatorGroup.CAST_EXPLICIT, ULONG, SHORT),
		ULONG_TO_USHORT(OperatorGroup.CAST_EXPLICIT, ULONG, USHORT),
		ULONG_TO_INT(OperatorGroup.CAST_EXPLICIT, ULONG, INT),
		ULONG_TO_UINT(OperatorGroup.CAST_EXPLICIT, ULONG, UINT),
		ULONG_TO_LONG(OperatorGroup.CAST_IMPLICIT, ULONG, LONG),
		ULONG_TO_USIZE(OperatorGroup.CAST_EXPLICIT, ULONG, USIZE),
		ULONG_TO_FLOAT(OperatorGroup.CAST_IMPLICIT, ULONG, FLOAT),
		ULONG_TO_DOUBLE(OperatorGroup.CAST_IMPLICIT, ULONG, DOUBLE),
		ULONG_TO_STRING(OperatorGroup.CAST_IMPLICIT, ULONG, STRING),
		ULONG_PARSE(OperatorGroup.PARSE, STRING, ULONG),
		ULONG_COUNT_LOW_ZEROES(OperatorGroup.COUNT_LOW_ZEROES, ULONG, INT),
		ULONG_COUNT_HIGH_ZEROES(OperatorGroup.COUNT_HIGH_ZEROES, ULONG, INT),
		ULONG_COUNT_LOW_ONES(OperatorGroup.COUNT_LOW_ONES, ULONG, INT),
		ULONG_COUNT_HIGH_ONES(OperatorGroup.COUNT_HIGH_ONES, ULONG, INT),
		ULONG_HIGHEST_ONE_BIT(OperatorGroup.HIGHEST_ONE_BIT, ULONG, INT),
		ULONG_LOWEST_ONE_BIT(OperatorGroup.LOWEST_ONE_BIT, ULONG, INT),
		ULONG_HIGHEST_ZERO_BIT(OperatorGroup.HIGHEST_ZERO_BIT, ULONG, INT),
		ULONG_LOWEST_ZERO_BIT(OperatorGroup.LOWEST_ZERO_BIT, ULONG, INT),
		ULONG_BIT_COUNT(OperatorGroup.BIT_COUNT, ULONG, INT),

		USIZE_NOT(OperatorGroup.BITWISE_NOT, USIZE, USIZE),
		USIZE_TO_BYTE(OperatorGroup.CAST_EXPLICIT, USIZE, BYTE),
		USIZE_TO_SBYTE(OperatorGroup.CAST_EXPLICIT, USIZE, SBYTE),
		USIZE_TO_SHORT(OperatorGroup.CAST_EXPLICIT, USIZE, SHORT),
		USIZE_TO_USHORT(OperatorGroup.CAST_EXPLICIT, USIZE, USHORT),
		USIZE_TO_INT(OperatorGroup.CAST_EXPLICIT, USIZE, INT),
		USIZE_TO_UINT(OperatorGroup.CAST_EXPLICIT, USIZE, UINT),
		USIZE_TO_LONG(OperatorGroup.CAST_IMPLICIT, USIZE, LONG),
		USIZE_TO_ULONG(OperatorGroup.CAST_IMPLICIT, USIZE, ULONG),
		USIZE_TO_FLOAT(OperatorGroup.CAST_IMPLICIT, USIZE, FLOAT),
		USIZE_TO_DOUBLE(OperatorGroup.CAST_IMPLICIT, USIZE, DOUBLE),
		USIZE_TO_STRING(OperatorGroup.CAST_IMPLICIT, USIZE, STRING),
		USIZE_PARSE(OperatorGroup.PARSE, STRING, USIZE),
		USIZE_COUNT_LOW_ZEROES(OperatorGroup.COUNT_LOW_ZEROES, USIZE, INT),
		USIZE_COUNT_HIGH_ZEROES(OperatorGroup.COUNT_HIGH_ZEROES, USIZE, INT),
		USIZE_COUNT_LOW_ONES(OperatorGroup.COUNT_LOW_ONES, USIZE, INT),
		USIZE_COUNT_HIGH_ONES(OperatorGroup.COUNT_HIGH_ONES, USIZE, INT),
		USIZE_HIGHEST_ONE_BIT(OperatorGroup.HIGHEST_ONE_BIT, USIZE, INT),
		USIZE_LOWEST_ONE_BIT(OperatorGroup.LOWEST_ONE_BIT, USIZE, INT),
		USIZE_HIGHEST_ZERO_BIT(OperatorGroup.HIGHEST_ZERO_BIT, USIZE, INT),
		USIZE_LOWEST_ZERO_BIT(OperatorGroup.LOWEST_ZERO_BIT, USIZE, INT),
		USIZE_BIT_COUNT(OperatorGroup.BIT_COUNT, USIZE, INT),

		FLOAT_NEG(OperatorGroup.NEG, FLOAT, FLOAT),
		FLOAT_TO_BYTE(OperatorGroup.CAST_EXPLICIT, FLOAT, BYTE),
		FLOAT_TO_SBYTE(OperatorGroup.CAST_EXPLICIT, FLOAT, SBYTE),
		FLOAT_TO_SHORT(OperatorGroup.CAST_EXPLICIT, FLOAT, SHORT),
		FLOAT_TO_USHORT(OperatorGroup.CAST_EXPLICIT, FLOAT, USHORT),
		FLOAT_TO_INT(OperatorGroup.CAST_EXPLICIT, FLOAT, INT),
		FLOAT_TO_UINT(OperatorGroup.CAST_EXPLICIT, FLOAT, UINT),
		FLOAT_TO_LONG(OperatorGroup.CAST_EXPLICIT, FLOAT, LONG),
		FLOAT_TO_ULONG(OperatorGroup.CAST_EXPLICIT, FLOAT, ULONG),
		FLOAT_TO_USIZE(OperatorGroup.CAST_EXPLICIT, FLOAT, USIZE),
		FLOAT_TO_DOUBLE(OperatorGroup.CAST_IMPLICIT, FLOAT, DOUBLE),
		FLOAT_TO_STRING(OperatorGroup.CAST_IMPLICIT, FLOAT, STRING),
		FLOAT_BITS(OperatorGroup.OTHER, FLOAT, INT),
		FLOAT_FROM_BITS(OperatorGroup.OTHER, FLOAT, FLOAT),
		FLOAT_PARSE(OperatorGroup.PARSE, FLOAT, FLOAT),

		DOUBLE_NEG(OperatorGroup.NEG, DOUBLE, DOUBLE),
		DOUBLE_TO_BYTE(OperatorGroup.CAST_EXPLICIT, DOUBLE, BYTE),
		DOUBLE_TO_SBYTE(OperatorGroup.CAST_EXPLICIT, DOUBLE, SBYTE),
		DOUBLE_TO_SHORT(OperatorGroup.CAST_EXPLICIT, DOUBLE, SHORT),
		DOUBLE_TO_USHORT(OperatorGroup.CAST_EXPLICIT, DOUBLE, USHORT),
		DOUBLE_TO_INT(OperatorGroup.CAST_EXPLICIT, DOUBLE, INT),
		DOUBLE_TO_UINT(OperatorGroup.CAST_EXPLICIT, DOUBLE, UINT),
		DOUBLE_TO_LONG(OperatorGroup.CAST_EXPLICIT, DOUBLE, LONG),
		DOUBLE_TO_ULONG(OperatorGroup.CAST_EXPLICIT, DOUBLE, ULONG),
		DOUBLE_TO_USIZE(OperatorGroup.CAST_EXPLICIT, DOUBLE, USIZE),
		DOUBLE_TO_FLOAT(OperatorGroup.CAST_EXPLICIT, DOUBLE, FLOAT),
		DOUBLE_TO_STRING(OperatorGroup.CAST_IMPLICIT, DOUBLE, STRING),
		DOUBLE_BITS(OperatorGroup.OTHER, DOUBLE, LONG),
		DOUBLE_FROM_BITS(OperatorGroup.OTHER, LONG, DOUBLE),
		DOUBLE_PARSE(OperatorGroup.PARSE, STRING, DOUBLE),

		CHAR_TO_UNICODE(OperatorGroup.OTHER, CHAR, UINT),
		CHAR_FROM_UNICODE(
				OperatorGroup.OTHER,
				(registry, type) -> type == UINT ? Optional.of(registry.optionalOf(CHAR)) : Optional.empty()
		),
		CHAR_TO_STRING(OperatorGroup.CAST_IMPLICIT, CHAR, STRING),
		CHAR_REMOVE_DIACRITICS(OperatorGroup.OTHER, CHAR, CHAR),
		CHAR_TO_LOWER_CASE(OperatorGroup.OTHER, CHAR, CHAR),
		CHAR_TO_UPPER_CASE(OperatorGroup.OTHER, CHAR, CHAR),

		STRING_CONSTRUCTOR_CHARACTERS(OperatorGroup.OTHER, ArrayTypeID.CHAR, STRING),
		STRING_LENGTH(OperatorGroup.OTHER, STRING, USIZE),
		STRING_CHARACTERS(OperatorGroup.OTHER, STRING, ArrayTypeID.CHAR),
		STRING_ISEMPTY(OperatorGroup.OTHER, STRING, BOOL),
		STRING_REMOVE_DIACRITICS(OperatorGroup.OTHER, STRING, STRING),
		STRING_TRIM(OperatorGroup.OTHER, STRING, STRING),
		STRING_TO_LOWER_CASE(OperatorGroup.OTHER, STRING, STRING),
		STRING_TO_UPPER_CASE(OperatorGroup.OTHER, STRING, STRING),

		ASSOC_SIZE(OperatorGroup.OTHER, (registry, type) -> type.asAssoc().map(x -> USIZE)),
		ASSOC_ISEMPTY(OperatorGroup.OTHER, (registry, type) -> type.asArray().map(x -> BOOL)),
		ASSOC_KEYS(OperatorGroup.OTHER, (registry, type) -> type.asAssoc().map(assoc -> registry.arrayOf(assoc.keyType))),
		ASSOC_VALUES(OperatorGroup.OTHER, (registry, type) -> type.asAssoc().map(assoc -> registry.arrayOf(assoc.valueType))),

		GENERICMAP_SIZE(OperatorGroup.OTHER, (registry, type) -> type.asGenericMap().map(x -> USIZE)),
		GENERICMAP_ISEMPTY(OperatorGroup.OTHER, (registry, type) -> type.asGenericMap().map(x -> BOOL)),

		ARRAY_LENGTH(OperatorGroup.OTHER, (registry, type) -> type.asArray().map(x -> USIZE)),
		ARRAY_ISEMPTY(OperatorGroup.OTHER, (registry, type) -> type.asArray().map(x -> BOOL)),
		ARRAY_HASHCODE(OperatorGroup.OTHER, (registry, type) -> type.asArray().map(x -> INT)),

		ENUM_NAME(OperatorGroup.OTHER, (registry, type) -> type.asDefinition().map(x -> x.definition.isEnum()).orElse(false) ? Optional.empty() : Optional.of(STRING)),
		ENUM_ORDINAL(OperatorGroup.OTHER, (registry, type) -> type.asDefinition().map(x -> x.definition.isEnum()).orElse(false) ? Optional.empty() : Optional.of(UINT)),

		SBYTE_ARRAY_AS_BYTE_ARRAY(OperatorGroup.CAST_EXPLICIT, ArrayTypeID.SBYTE, ArrayTypeID.BYTE),
		BYTE_ARRAY_AS_SBYTE_ARRAY(OperatorGroup.CAST_EXPLICIT, ArrayTypeID.BYTE, ArrayTypeID.SBYTE),
		SHORT_ARRAY_AS_USHORT_ARRAY(OperatorGroup.CAST_EXPLICIT, ArrayTypeID.SHORT, ArrayTypeID.USHORT),
		USHORT_ARRAY_AS_SHORT_ARRAY(OperatorGroup.CAST_EXPLICIT, ArrayTypeID.USHORT, ArrayTypeID.SHORT),
		INT_ARRAY_AS_UINT_ARRAY(OperatorGroup.CAST_EXPLICIT, ArrayTypeID.INT, ArrayTypeID.UINT),
		UINT_ARRAY_AS_INT_ARRAY(OperatorGroup.CAST_EXPLICIT, ArrayTypeID.UINT, ArrayTypeID.INT),
		LONG_ARRAY_AS_ULONG_ARRAY(OperatorGroup.CAST_EXPLICIT, ArrayTypeID.LONG, ArrayTypeID.ULONG),
		ULONG_ARRAY_AS_LONG_ARRAY(OperatorGroup.CAST_EXPLICIT, ArrayTypeID.ULONG, ArrayTypeID.LONG),

		OPTIONAL_WRAP(OperatorGroup.OTHER, (registry, type) -> Optional.of(registry.optionalOf(type))),
		OPTIONAL_UNWRAP(OperatorGroup.OTHER, (registry, type) -> type.asOptional().map(x -> x.baseType)),

		RANGE_FROM(OperatorGroup.OTHER, (registry, type) -> type.asRange().map(x -> x.baseType)),
		RANGE_TO(OperatorGroup.OTHER, (registry, type) -> type.asRange().map(x -> x.baseType));

		public final OperatorGroup group;
		private final UnaryTypeMapper typeMapper;

		Operator(OperatorGroup group, TypeID inputType, TypeID outputType) {
			this.group = group;
			typeMapper = (registry, valueType) -> inputType == valueType ? Optional.of(outputType) : Optional.empty();
		}

		Operator(OperatorGroup group, UnaryTypeMapper typeMapper) {
			this.group = group;
			this.typeMapper = typeMapper;
		}

		public Optional<TypeID> getOutputType(TypeBuilder builder, TypeID type) {
			return typeMapper.apply(builder, type);
		}
	}

	@FunctionalInterface
	interface UnaryTypeMapper {
		Optional<TypeID> apply(TypeBuilder builder, TypeID type);
	}

	public final Expression target;
	public final Operator operator;

	private final TypeBuilder builder;

	public UnaryExpression(CodePosition position, Expression target, Operator operator, TypeBuilder builder) {
		super(position, operator.getOutputType(builder, target.type).orElse(INVALID), null);

		this.target = target;
		this.operator = operator;
		this.builder = builder;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitUnary(this);
	}

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitUnary(context, this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		Expression tTarget = target.transform(transformer);
		return target == tTarget ? this : new UnaryExpression(position, tTarget, operator, builder);
	}
}
