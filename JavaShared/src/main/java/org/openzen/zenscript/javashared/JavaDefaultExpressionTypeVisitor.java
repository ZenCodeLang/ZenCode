package org.openzen.zenscript.javashared;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.expression.*;
import org.openzen.zenscript.codemodel.type.*;

public class JavaDefaultExpressionTypeVisitor implements TypeVisitor<Expression> {

	public static final JavaDefaultExpressionTypeVisitor INSTANCE = new JavaDefaultExpressionTypeVisitor();

	private JavaDefaultExpressionTypeVisitor(){}

	@Override
	public Expression visitBasic(BasicTypeID basic) {


		if (basic == null)
			throw new IllegalStateException("Null basic type!");

		switch (basic) {
			case UNDETERMINED:
			case VOID:
				throw new IllegalStateException("Void and Undetermined have no default type");
			case NULL:
				return new NullExpression(CodePosition.UNKNOWN);
			case BOOL:
				return new ConstantBoolExpression(CodePosition.UNKNOWN, false);
			case BYTE:
				return new ConstantByteExpression(CodePosition.UNKNOWN, 0);
			case SBYTE:
				return new ConstantSByteExpression(CodePosition.UNKNOWN, (byte) 0);
			case SHORT:
				return new ConstantShortExpression(CodePosition.UNKNOWN, (short) 0);
			case USHORT:
				return new ConstantUShortExpression(CodePosition.UNKNOWN, 0);
			case INT:
				return new ConstantIntExpression(CodePosition.UNKNOWN, 0);
			case UINT:
				return new ConstantUIntExpression(CodePosition.UNKNOWN, 0);
			case LONG:
				return new ConstantLongExpression(CodePosition.UNKNOWN, 0L);
			case ULONG:
				return new ConstantULongExpression(CodePosition.UNKNOWN, 0L);
			case USIZE:
				return new ConstantUSizeExpression(CodePosition.UNKNOWN, 0L);
			case FLOAT:
				return new ConstantFloatExpression(CodePosition.UNKNOWN, 0.0f);
			case DOUBLE:
				return new ConstantDoubleExpression(CodePosition.UNKNOWN, 0.0d);
			case CHAR:
				return new ConstantCharExpression(CodePosition.UNKNOWN, '\0');
			default:
				throw new IllegalStateException("Unknown basic type!");
		}


	}

	@Override
	public Expression visitString(StringTypeID string) {
		return new NullExpression(CodePosition.UNKNOWN);
	}

	@Override
	public Expression visitArray(ArrayTypeID array) {
		return new NullExpression(CodePosition.UNKNOWN);
	}

	@Override
	public Expression visitAssoc(AssocTypeID assoc) {
		return new NullExpression(CodePosition.UNKNOWN);
	}

	@Override
	public Expression visitGenericMap(GenericMapTypeID map) {
		return new NullExpression(CodePosition.UNKNOWN);
	}

	@Override
	public Expression visitIterator(IteratorTypeID iterator) {
		return new NullExpression(CodePosition.UNKNOWN);
	}

	@Override
	public Expression visitFunction(FunctionTypeID function) {
		return new NullExpression(CodePosition.UNKNOWN);
	}

	@Override
	public Expression visitDefinition(DefinitionTypeID definition) {
		return new NullExpression(CodePosition.UNKNOWN);
	}

	@Override
	public Expression visitGeneric(GenericTypeID generic) {
		return new NullExpression(CodePosition.UNKNOWN);
	}

	@Override
	public Expression visitRange(RangeTypeID range) {
		return new NullExpression(CodePosition.UNKNOWN);
	}

	@Override
	public Expression visitOptional(OptionalTypeID type) {
		return new NullExpression(CodePosition.UNKNOWN);
	}

	@Override
	public Expression visitInvalid(InvalidTypeID type) {
		return new NullExpression(CodePosition.UNKNOWN);
	}
}
