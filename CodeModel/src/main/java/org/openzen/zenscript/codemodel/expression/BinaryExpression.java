package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.RangeTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Optional;

import static org.openzen.zenscript.codemodel.type.BasicTypeID.*;

public class BinaryExpression extends Expression {
	public enum OperatorGroup {
		ADD,
		SUB,
		MUL,
		DIV,
		MOD,
		AND,
		OR,
		XOR,
		SHL,
		SHR,
		EQUALS,
		NOTEQUALS,
		COMPARE,
		PARSE_WITH_BASE,

		OTHER,
	}

	public enum Operator {
		AND_AND(OperatorGroup.OTHER, BOOL, BOOL, BOOL),
		OR_OR(OperatorGroup.OTHER, BOOL, BOOL, BOOL),

		BOOL_AND(OperatorGroup.AND, BOOL, BOOL, BOOL),
		BOOL_OR(OperatorGroup.OR, BOOL, BOOL, BOOL),
		BOOL_XOR(OperatorGroup.XOR, BOOL, BOOL, BOOL),
		BOOL_EQUALS(OperatorGroup.EQUALS, BOOL, BOOL, BOOL),
		BOOL_NOTEQUALS(OperatorGroup.NOTEQUALS, BOOL, BOOL, BOOL),

		BYTE_ADD_BYTE(OperatorGroup.ADD, BYTE, BYTE, BYTE),
		BYTE_SUB_BYTE(OperatorGroup.SUB, BYTE, BYTE, BYTE),
		BYTE_MUL_BYTE(OperatorGroup.MUL, BYTE, BYTE, BYTE),
		BYTE_DIV_BYTE(OperatorGroup.DIV, BYTE, BYTE, BYTE),
		BYTE_MOD_BYTE(OperatorGroup.MOD, BYTE, BYTE, BYTE),
		BYTE_AND_BYTE(OperatorGroup.AND, BYTE, BYTE, BYTE),
		BYTE_OR_BYTE(OperatorGroup.OR, BYTE, BYTE, BYTE),
		BYTE_XOR_BYTE(OperatorGroup.XOR, BYTE, BYTE, BYTE),
		BYTE_SHL(OperatorGroup.SHL, BYTE, BYTE, BYTE),
		BYTE_SHR(OperatorGroup.SHR, BYTE, BYTE, BYTE),
		BYTE_COMPARE(OperatorGroup.COMPARE, BYTE, BYTE, INT),
		BYTE_PARSE_WITH_BASE(OperatorGroup.PARSE_WITH_BASE, STRING, INT, BYTE),

		SBYTE_ADD_SBYTE(OperatorGroup.ADD, SBYTE, SBYTE, SBYTE),
		SBYTE_SUB_SBYTE(OperatorGroup.SUB, SBYTE, SBYTE, SBYTE),
		SBYTE_MUL_SBYTE(OperatorGroup.MUL, SBYTE, SBYTE, SBYTE),
		SBYTE_DIV_SBYTE(OperatorGroup.DIV, SBYTE, SBYTE, SBYTE),
		SBYTE_MOD_SBYTE(OperatorGroup.MOD, SBYTE, SBYTE, SBYTE),
		SBYTE_AND_SBYTE(OperatorGroup.AND, SBYTE, SBYTE, SBYTE),
		SBYTE_OR_SBYTE(OperatorGroup.OR, SBYTE, SBYTE, SBYTE),
		SBYTE_XOR_SBYTE(OperatorGroup.XOR, SBYTE, SBYTE, SBYTE),
		SBYTE_SHL(OperatorGroup.SHL, SBYTE, SBYTE, SBYTE),
		SBYTE_SHR(OperatorGroup.SHR, SBYTE, SBYTE, SBYTE),
		SBYTE_COMPARE(OperatorGroup.COMPARE, SBYTE, SBYTE, BOOL),
		SBYTE_PARSE_WITH_BASE(OperatorGroup.PARSE_WITH_BASE, STRING, INT, SBYTE),

		SHORT_ADD_SHORT(OperatorGroup.ADD, SHORT, SHORT, SHORT),
		SHORT_SUB_SHORT(OperatorGroup.SUB, SHORT, SHORT, SHORT),
		SHORT_MUL_SHORT(OperatorGroup.MUL, SHORT, SHORT, SHORT),
		SHORT_DIV_SHORT(OperatorGroup.DIV, SHORT, SHORT, SHORT),
		SHORT_MOD_SHORT(OperatorGroup.MOD, SHORT, SHORT, SHORT),
		SHORT_AND_SHORT(OperatorGroup.AND, SHORT, SHORT, SHORT),
		SHORT_OR_SHORT(OperatorGroup.OR, SHORT, SHORT, SHORT),
		SHORT_XOR_SHORT(OperatorGroup.XOR, SHORT, SHORT, SHORT),
		SHORT_SHL(OperatorGroup.SHL, SHORT, SHORT, SHORT),
		SHORT_SHR(OperatorGroup.SHR, SHORT, SHORT, SHORT),
		SHORT_COMPARE(OperatorGroup.COMPARE, SHORT, SHORT, INT),
		SHORT_PARSE_WITH_BASE(OperatorGroup.PARSE_WITH_BASE, STRING, INT, SHORT),

		USHORT_ADD_USHORT(OperatorGroup.ADD, USHORT, USHORT, USHORT),
		USHORT_SUB_USHORT(OperatorGroup.SUB, USHORT, USHORT, USHORT),
		USHORT_MUL_USHORT(OperatorGroup.MUL, USHORT, USHORT, USHORT),
		USHORT_DIV_USHORT(OperatorGroup.DIV, USHORT, USHORT, USHORT),
		USHORT_MOD_USHORT(OperatorGroup.MOD, USHORT, USHORT, USHORT),
		USHORT_AND_USHORT(OperatorGroup.AND, USHORT, USHORT, USHORT),
		USHORT_OR_USHORT(OperatorGroup.OR, USHORT, USHORT, USHORT),
		USHORT_XOR_USHORT(OperatorGroup.XOR, USHORT, USHORT, USHORT),
		USHORT_SHL(OperatorGroup.SHL, USHORT, USHORT, USHORT),
		USHORT_SHR(OperatorGroup.SHR, USHORT, USHORT, USHORT),
		USHORT_COMPARE(OperatorGroup.COMPARE, USHORT, USHORT, INT),
		USHORT_PARSE_WITH_BASE(OperatorGroup.PARSE_WITH_BASE, STRING, INT, USHORT),

		INT_ADD_INT(OperatorGroup.ADD, INT, INT, INT),
		INT_ADD_USIZE(OperatorGroup.ADD, INT, USIZE, USIZE),
		INT_SUB_INT(OperatorGroup.SUB, INT, INT, INT),
		INT_MUL_INT(OperatorGroup.MUL, INT, INT, INT),
		INT_DIV_INT(OperatorGroup.DIV, INT, INT, INT),
		INT_MOD_INT(OperatorGroup.MOD, INT, INT, INT),
		INT_AND_INT(OperatorGroup.AND, INT, INT, INT),
		INT_OR_INT(OperatorGroup.OR, INT, INT, INT),
		INT_XOR_INT(OperatorGroup.XOR, INT, INT, INT),
		INT_SHL(OperatorGroup.SHL, INT, INT, INT),
		INT_SHR(OperatorGroup.SHR, INT, INT, INT),
		INT_COMPARE(OperatorGroup.COMPARE, INT, INT, INT),
		INT_PARSE_WITH_BASE(OperatorGroup.PARSE_WITH_BASE, STRING, INT, INT),

		UINT_ADD_UINT(OperatorGroup.ADD, UINT, UINT, UINT),
		UINT_SUB_UINT(OperatorGroup.SUB, UINT, UINT, UINT),
		UINT_MUL_UINT(OperatorGroup.MUL, UINT, UINT, UINT),
		UINT_DIV_UINT(OperatorGroup.DIV, UINT, UINT, UINT),
		UINT_MOD_UINT(OperatorGroup.MOD, UINT, UINT, UINT),
		UINT_AND_UINT(OperatorGroup.AND, UINT, UINT, UINT),
		UINT_OR_UINT(OperatorGroup.OR, UINT, UINT, UINT),
		UINT_XOR_UINT(OperatorGroup.XOR, UINT, UINT, UINT),
		UINT_SHL(OperatorGroup.SHL, UINT, UINT, UINT),
		UINT_SHR(OperatorGroup.SHR, UINT, UINT, UINT),
		UINT_COMPARE(OperatorGroup.COMPARE, UINT, UINT, INT),
		UINT_PARSE_WITH_BASE(OperatorGroup.PARSE_WITH_BASE, STRING, INT, UINT),

		LONG_ADD_LONG(OperatorGroup.ADD, LONG, LONG, LONG),
		LONG_SUB_LONG(OperatorGroup.SUB, LONG, LONG, LONG),
		LONG_MUL_LONG(OperatorGroup.MUL, LONG, LONG, LONG),
		LONG_DIV_LONG(OperatorGroup.DIV, LONG, LONG, LONG),
		LONG_MOD_LONG(OperatorGroup.MOD, LONG, LONG, LONG),
		LONG_AND_LONG(OperatorGroup.AND, LONG, LONG, LONG),
		LONG_OR_LONG(OperatorGroup.OR, LONG, LONG, LONG),
		LONG_XOR_LONG(OperatorGroup.XOR, LONG, LONG, LONG),
		LONG_SHL(OperatorGroup.SHL, LONG, INT, LONG),
		LONG_SHR(OperatorGroup.SHR, LONG, INT, LONG),
		LONG_COMPARE(OperatorGroup.COMPARE, LONG, LONG, INT),
		LONG_COMPARE_INT(OperatorGroup.COMPARE, LONG, INT, INT),
		LONG_PARSE_WITH_BASE(OperatorGroup.PARSE_WITH_BASE, STRING, INT, LONG),

		ULONG_ADD_ULONG(OperatorGroup.ADD, ULONG, ULONG, ULONG),
		ULONG_SUB_ULONG(OperatorGroup.SUB, ULONG, ULONG, ULONG),
		ULONG_MUL_ULONG(OperatorGroup.MUL, ULONG, ULONG, ULONG),
		ULONG_DIV_ULONG(OperatorGroup.DIV, ULONG, ULONG, ULONG),
		ULONG_MOD_ULONG(OperatorGroup.MOD, ULONG, ULONG, ULONG),
		ULONG_AND_ULONG(OperatorGroup.AND, ULONG, ULONG, ULONG),
		ULONG_OR_ULONG(OperatorGroup.OR, ULONG, ULONG, ULONG),
		ULONG_XOR_ULONG(OperatorGroup.XOR, ULONG, ULONG, ULONG),
		ULONG_SHL(OperatorGroup.SHL, ULONG, INT, ULONG),
		ULONG_SHR(OperatorGroup.SHR, ULONG, INT, ULONG),
		ULONG_COMPARE(OperatorGroup.COMPARE, ULONG, ULONG, INT),
		ULONG_COMPARE_UINT(OperatorGroup.COMPARE, ULONG, UINT, INT),
		ULONG_COMPARE_USIZE(OperatorGroup.COMPARE, ULONG, USIZE, INT),
		ULONG_PARSE_WITH_BASE(OperatorGroup.PARSE_WITH_BASE, STRING, INT, ULONG),

		USIZE_ADD_USIZE(OperatorGroup.ADD, USIZE, USIZE, USIZE),
		USIZE_SUB_USIZE(OperatorGroup.SUB, USIZE, USIZE, USIZE),
		USIZE_MUL_USIZE(OperatorGroup.MUL, USIZE, USIZE, USIZE),
		USIZE_DIV_USIZE(OperatorGroup.DIV, USIZE, USIZE, USIZE),
		USIZE_MOD_USIZE(OperatorGroup.MOD, USIZE, USIZE, USIZE),
		USIZE_AND_USIZE(OperatorGroup.AND, USIZE, USIZE, USIZE),
		USIZE_OR_USIZE(OperatorGroup.OR, USIZE, USIZE, USIZE),
		USIZE_XOR_USIZE(OperatorGroup.XOR, USIZE, USIZE, USIZE),
		USIZE_SHL(OperatorGroup.SHL, USIZE, USIZE, USIZE),
		USIZE_SHR(OperatorGroup.SHR, USIZE, USIZE, USIZE),
		USIZE_COMPARE(OperatorGroup.COMPARE, USIZE, USIZE, INT),
		USIZE_COMPARE_UINT(OperatorGroup.COMPARE, USIZE, UINT, INT),
		USIZE_PARSE_WITH_BASE(OperatorGroup.PARSE_WITH_BASE, STRING, INT, USIZE),

		FLOAT_ADD_FLOAT(OperatorGroup.ADD, FLOAT, FLOAT, FLOAT),
		FLOAT_SUB_FLOAT(OperatorGroup.SUB, FLOAT, FLOAT, FLOAT),
		FLOAT_MUL_FLOAT(OperatorGroup.MUL, FLOAT, FLOAT, FLOAT),
		FLOAT_DIV_FLOAT(OperatorGroup.DIV, FLOAT, FLOAT, FLOAT),
		FLOAT_MOD_FLOAT(OperatorGroup.MOD, FLOAT, FLOAT, FLOAT),
		FLOAT_COMPARE(OperatorGroup.COMPARE, FLOAT, FLOAT, INT),

		DOUBLE_ADD_DOUBLE(OperatorGroup.ADD, DOUBLE, DOUBLE, DOUBLE),
		DOUBLE_SUB_DOUBLE(OperatorGroup.SUB, DOUBLE, DOUBLE, DOUBLE),
		DOUBLE_MUL_DOUBLE(OperatorGroup.MUL, DOUBLE, DOUBLE, DOUBLE),
		DOUBLE_DIV_DOUBLE(OperatorGroup.DIV, DOUBLE, DOUBLE, DOUBLE),
		DOUBLE_MOD_DOUBLE(OperatorGroup.MOD, DOUBLE, DOUBLE, DOUBLE),
		DOUBLE_COMPARE(OperatorGroup.COMPARE, DOUBLE, DOUBLE, INT),

		STRING_ADD_STRING(OperatorGroup.ADD, STRING, STRING, STRING),
		STRING_COMPARE(OperatorGroup.COMPARE, STRING, STRING, INT),
		STRING_INDEXGET(OperatorGroup.OTHER, STRING, INT, CHAR),
		STRING_RANGEGET(OperatorGroup.OTHER, STRING, RangeTypeID.USIZE, STRING),
		STRING_CONTAINS_CHAR(OperatorGroup.OTHER, STRING, CHAR, BOOL),
		STRING_CONTAINS_STRING(OperatorGroup.OTHER, STRING, STRING, BOOL),
		STRING_STARTS_WITH(OperatorGroup.OTHER, STRING, STRING, BOOL),
		STRING_ENDS_WITH(OperatorGroup.OTHER, STRING, STRING, BOOL),

		ASSOC_INDEXGET(OperatorGroup.OTHER, (value, key) -> value.asAssoc().flatMap(assoc -> assoc.keyType.equals(key) ? Optional.of(assoc.valueType) : Optional.empty())),
		ASSOC_CONTAINS(OperatorGroup.OTHER, (value, key) -> value.asAssoc().flatMap(assoc -> assoc.keyType.equals(key) ? Optional.of(BOOL) : Optional.empty())),

		ARRAY_CONTAINS(OperatorGroup.OTHER, (haystack, needle) -> haystack.asArray().flatMap(array -> array.elementType.equals(needle) ? Optional.of(BOOL) : Optional.empty())),
		ARRAY_EQUALS(OperatorGroup.EQUALS, (left, right) -> left.equals(right) ? Optional.of(BOOL) : Optional.empty()),
		ARRAY_NOTEQUALS(OperatorGroup.NOTEQUALS, (left, right) -> left.equals(right) ? Optional.of(BOOL) : Optional.empty()),

		ENUM_COMPARE(OperatorGroup.COMPARE, (left, right) -> {
			Optional<TypeSymbol> leftEnum = left.asDefinition().flatMap(x -> x.definition.isEnum() ? Optional.of(x.definition) : Optional.empty());
			Optional<TypeSymbol> rightEnum = right.asDefinition().flatMap(x -> x.definition.isEnum() ? Optional.of(x.definition) : Optional.empty());
			if (leftEnum.isPresent() && rightEnum.isPresent() && leftEnum.get() == rightEnum.get()) {
				return Optional.of(BOOL);
			} else {
				return Optional.empty();
			}
		}),

		SAME(OperatorGroup.OTHER, (left, right) -> left.equals(right) ? Optional.of(BOOL) : Optional.empty()),
		NOTSAME(OperatorGroup.OTHER, (left, right) -> left.equals(right) ? Optional.of(BOOL) : Optional.empty());

		public final OperatorGroup group;
		private final BinaryTypeMapper mapper;

		Operator(OperatorGroup group, TypeID left, TypeID right, TypeID result) {
			this.group = group;
			mapper = (actualLeft, actualRight) -> actualLeft == left && actualRight == right ? Optional.of(result) : Optional.empty();
		}

		Operator(OperatorGroup group, BinaryTypeMapper mapper) {
			this.group = group;
			this.mapper = mapper;
		}

		public Optional<TypeID> getOutputType(TypeID left, TypeID right) {
			return mapper.apply(left, right);
		}
	}

	@FunctionalInterface
	interface BinaryTypeMapper {
		Optional<TypeID> apply(TypeID left, TypeID right);
	}

	public final Expression left;
	public final Expression right;
	public final Operator operator;

	public BinaryExpression(CodePosition position, Expression left, Expression right, Operator operator) {
		super(position, operator.getOutputType(left.type, right.type).orElse(INVALID), null);

		this.left = left;
		this.right = right;
		this.operator = operator;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitBinary(this);
	}

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitBinary(context, this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		Expression left = this.left.transform(transformer);
		Expression right = this.right.transform(transformer);
		if (left == this.left && right == this.right)
			return this;

		return new BinaryExpression(position, left, right, operator);
	}
}
