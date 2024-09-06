/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.moduleserializer.encoder;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.CompareType;
import org.openzen.zenscript.codemodel.expression.*;
import org.openzen.zenscript.codemodel.serialization.CodeSerializationOutput;
import org.openzen.zenscript.codemodel.serialization.StatementSerializationContext;
import org.openzen.zenscript.moduleserialization.ExpressionEncoding;

/**
 * @author Hoofdgebruiker
 */
public class ExpressionSerializer implements ExpressionVisitorWithContext<StatementSerializationContext, Void> {
	private final CodeSerializationOutput output;
	private final boolean positions;
	private final boolean localVariableNames;

	public ExpressionSerializer(
			CodeSerializationOutput output,
			boolean positions,
			boolean localVariableNames) {
		this.output = output;
		this.positions = positions;
		this.localVariableNames = localVariableNames;
	}

	private int getFlags(Expression expression) {
		int flags = 0;
		if (expression.position != CodePosition.UNKNOWN && positions)
			flags |= ExpressionEncoding.FLAG_POSITION;
		return flags;
	}

	private void serialize(int flags, Expression expression) {
		output.writeUInt(flags);
		if ((flags & ExpressionEncoding.FLAG_POSITION) > 0)
			output.serialize(expression.position);
	}

	@Override
	public Void visitAndAnd(StatementSerializationContext context, AndAndExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_AND_AND);
		int flags = getFlags(expression);
		serialize(flags, expression);

		expression.left.accept(context, this);
		expression.right.accept(context, this);
		return null;
	}

	@Override
	public Void visitArray(StatementSerializationContext context, ArrayExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_ARRAY);
		int flags = getFlags(expression);
		serialize(flags, expression);

		output.serialize(context, expression.arrayType);
		output.writeUInt(expression.expressions.length);
		for (Expression element : expression.expressions)
			output.serialize(context, element);
		return null;
	}

	@Override
	public Void visitCompare(StatementSerializationContext context, CompareExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_COMPARE);
		int flags = getFlags(expression);
		serialize(flags, expression);

		output.writeUInt(getCompareType(expression.comparison));
		output.serialize(context, expression.left);
		output.serialize(context, expression.right);
		output.write(context, expression.operator);
		return null;
	}

	@Override
	public Void visitCall(StatementSerializationContext context, CallExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_CALL);
		int flags = getFlags(expression);
		serialize(flags, expression);

		output.serialize(context, expression.target);
		output.write(context, expression.member);
		output.serialize(context, expression.arguments);
		return null;
	}

	@Override
	public Void visitCallStatic(StatementSerializationContext context, CallStaticExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_CALL_STATIC);
		int flags = getFlags(expression);
		serialize(flags, expression);

		output.serialize(context, expression.target);
		output.write(context, expression.member);
		output.serialize(context, expression.arguments);
		return null;
	}

	@Override
	public Void visitCapturedClosure(StatementSerializationContext context, CapturedClosureExpression expression) {
		if (expression.closure != context.getLambdaClosure())
			throw new AssertionError("Closure invalid");

		output.writeUInt(ExpressionEncoding.TYPE_CAPTURED_CLOSURE);
		int flags = getFlags(expression);
		serialize(flags, expression);

		output.serialize(context.getLambdaOuter(), expression.value);
		return null;
	}

	@Override
	public Void visitCapturedLocalVariable(StatementSerializationContext context, CapturedLocalVariableExpression expression) {
		if (expression.closure != context.getLambdaClosure())
			throw new AssertionError("Closure invalid");

		output.writeUInt(ExpressionEncoding.TYPE_CAPTURED_LOCAL_VARIABLE);
		int flags = getFlags(expression);
		serialize(flags, expression);

		output.writeUInt(context.getLambdaOuter().getVariableId(expression.variable));
		return null;
	}

	@Override
	public Void visitCapturedParameter(StatementSerializationContext context, CapturedParameterExpression expression) {
		if (expression.closure != context.getLambdaClosure())
			throw new AssertionError("Closure invalid");

		output.writeUInt(ExpressionEncoding.TYPE_CAPTURED_PARAMETER);
		int flags = getFlags(expression);
		serialize(flags, expression);

		output.writeUInt(context.getParameterIndex(expression.parameter));
		return null;
	}

	@Override
	public Void visitCapturedThis(StatementSerializationContext context, CapturedThisExpression expression) {
		if (expression.closure != context.getLambdaClosure())
			throw new AssertionError("Closure invalid");

		output.writeUInt(ExpressionEncoding.TYPE_CAPTURED_THIS);
		int flags = getFlags(expression);
		serialize(flags, expression);

		return null;
	}

	@Override
	public Void visitCheckNull(StatementSerializationContext context, CheckNullExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_CHECKNULL);
		int flags = getFlags(expression);
		serialize(flags, expression);

		output.serialize(context, expression.value);
		return null;
	}

	@Override
	public Void visitCoalesce(StatementSerializationContext context, CoalesceExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_COALESCE);
		int flags = getFlags(expression);
		serialize(flags, expression);

		output.serialize(context, expression.left);
		output.serialize(context, expression.right);
		return null;
	}

	@Override
	public Void visitConditional(StatementSerializationContext context, ConditionalExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_CONDITIONAL);
		int flags = getFlags(expression);
		serialize(flags, expression);

		output.serialize(context, expression.condition);
		output.serialize(context.types(), expression.type);
		output.serialize(context, expression.ifThen);
		output.serialize(context, expression.ifElse);
		return null;
	}

	@Override
	public Void visitConst(StatementSerializationContext context, ConstExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_CONST);
		int flags = getFlags(expression);
		serialize(flags, expression);

		output.serialize(context.types(), expression.constant.getType());
		output.write(context, expression.constant);
		return null;
	}

	@Override
	public Void visitConstantBool(StatementSerializationContext context, ConstantBoolExpression expression) {
		output.writeUInt(expression.value ? ExpressionEncoding.TYPE_CONSTANT_BOOL_TRUE : ExpressionEncoding.TYPE_CONSTANT_BOOL_FALSE);
		int flags = getFlags(expression);
		serialize(flags, expression);
		return null;
	}

	@Override
	public Void visitConstantByte(StatementSerializationContext context, ConstantByteExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_CONSTANT_BYTE);
		int flags = getFlags(expression);
		serialize(flags, expression);

		output.writeByte(expression.value);
		return null;
	}

	@Override
	public Void visitConstantChar(StatementSerializationContext context, ConstantCharExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_CONSTANT_CHAR);
		int flags = getFlags(expression);
		serialize(flags, expression);

		output.writeChar(expression.value);
		return null;
	}

	@Override
	public Void visitConstantDouble(StatementSerializationContext context, ConstantDoubleExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_CONSTANT_DOUBLE);
		int flags = getFlags(expression);
		serialize(flags, expression);

		output.writeDouble(expression.value);
		return null;
	}

	@Override
	public Void visitConstantFloat(StatementSerializationContext context, ConstantFloatExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_CONSTANT_FLOAT);
		int flags = getFlags(expression);
		serialize(flags, expression);

		output.writeFloat(expression.value);
		return null;
	}

	@Override
	public Void visitConstantInt(StatementSerializationContext context, ConstantIntExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_CONSTANT_INT);
		int flags = getFlags(expression);
		serialize(flags, expression);

		output.writeInt(expression.value);
		return null;
	}

	@Override
	public Void visitConstantLong(StatementSerializationContext context, ConstantLongExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_CONSTANT_LONG);
		int flags = getFlags(expression);
		serialize(flags, expression);

		output.writeLong(expression.value);
		return null;
	}

	@Override
	public Void visitConstantSByte(StatementSerializationContext context, ConstantSByteExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_CONSTANT_SBYTE);
		int flags = getFlags(expression);
		serialize(flags, expression);

		output.writeSByte(expression.value);
		return null;
	}

	@Override
	public Void visitConstantShort(StatementSerializationContext context, ConstantShortExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_CONSTANT_SHORT);
		int flags = getFlags(expression);
		serialize(flags, expression);

		output.writeShort(expression.value);
		return null;
	}

	@Override
	public Void visitConstantString(StatementSerializationContext context, ConstantStringExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_CONSTANT_STRING);
		int flags = getFlags(expression);
		serialize(flags, expression);

		output.writeString(expression.value);
		return null;
	}

	@Override
	public Void visitConstantUInt(StatementSerializationContext context, ConstantUIntExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_CONSTANT_UINT);
		int flags = getFlags(expression);
		serialize(flags, expression);

		output.writeUInt(expression.value);
		return null;
	}

	@Override
	public Void visitConstantULong(StatementSerializationContext context, ConstantULongExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_CONSTANT_ULONG);
		int flags = getFlags(expression);
		serialize(flags, expression);

		output.writeULong(expression.value);
		return null;
	}

	@Override
	public Void visitConstantUShort(StatementSerializationContext context, ConstantUShortExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_CONSTANT_USHORT);
		int flags = getFlags(expression);
		serialize(flags, expression);

		output.writeUShort(expression.value);
		return null;
	}

	@Override
	public Void visitConstantUSize(StatementSerializationContext context, ConstantUSizeExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_CONSTANT_USIZE);
		int flags = getFlags(expression);
		serialize(flags, expression);

		output.writeULong(expression.value);
		return null;
	}

	@Override
	public Void visitConstructorThisCall(StatementSerializationContext context, ConstructorThisCallExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_CONSTRUCTOR_THIS_CALL);
		int flags = getFlags(expression);
		serialize(flags, expression);

		output.write(context, expression.constructor);
		output.serialize(context, expression.arguments);
		return null;
	}

	@Override
	public Void visitConstructorSuperCall(StatementSerializationContext context, ConstructorSuperCallExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_CONSTRUCTOR_SUPER_CALL);
		int flags = getFlags(expression);
		serialize(flags, expression);

		output.write(context, expression.constructor);
		output.serialize(context, expression.arguments);
		return null;
	}

	@Override
	public Void visitEnumConstant(StatementSerializationContext context, EnumConstantExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_ENUM_CONSTANT);
		int flags = getFlags(expression);
		serialize(flags, expression);

		output.write(expression.value);
		return null;
	}

	@Override
	public Void visitFunction(StatementSerializationContext context, FunctionExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_FUNCTION);
		int flags = getFlags(expression);
		serialize(flags, expression);

		StatementSerializationContext innerContext = new StatementSerializationContext(context, expression.header);
		output.serialize(context, expression.header);
		output.serialize(innerContext, expression.body);
		return null;
	}

	@Override
	public Void visitGetField(StatementSerializationContext context, GetFieldExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_GET_FIELD);
		int flags = getFlags(expression);
		serialize(flags, expression);

		output.serialize(context, expression.target);
		output.write(context, expression.field);
		return null;
	}

	@Override
	public Void visitGetFunctionParameter(StatementSerializationContext context, GetFunctionParameterExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_GET_FUNCTION_PARAMETER);
		int flags = getFlags(expression);
		serialize(flags, expression);

		output.writeUInt(context.getParameterIndex(expression.parameter));
		return null;
	}

	@Override
	public Void visitGetLocalVariable(StatementSerializationContext context, GetLocalVariableExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_GET_LOCAL_VARIABLE);
		int flags = getFlags(expression);
		serialize(flags, expression);

		output.writeUInt(context.getVariableId(expression.variable));
		return null;
	}

	@Override
	public Void visitGetMatchingVariantField(StatementSerializationContext context, GetMatchingVariantField expression) {
		output.writeUInt(ExpressionEncoding.TYPE_GET_MATCHING_VARIANT_FIELD);
		int flags = getFlags(expression);
		serialize(flags, expression);

		output.writeUInt(expression.index);
		return null;
	}

	@Override
	public Void visitGetStaticField(StatementSerializationContext context, GetStaticFieldExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_GET_STATIC_FIELD);
		int flags = getFlags(expression);
		serialize(flags, expression);

		output.serialize(context, expression.field.getType());
		output.write(context, expression.field);
		return null;
	}

	@Override
	public Void visitGlobal(StatementSerializationContext context, GlobalExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_GLOBAL);
		int flags = getFlags(expression);
		serialize(flags, expression);

		output.writeString(expression.name);
		output.serialize(context, expression.resolution);
		return null;
	}

	@Override
	public Void visitGlobalCall(StatementSerializationContext context, GlobalCallExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_GLOBAL_CALL);
		int flags = getFlags(expression);
		serialize(flags, expression);

		output.writeString(expression.name);
		output.serialize(context, expression.arguments);
		output.serialize(context, expression.resolution);
		return null;
	}

	@Override
	public Void visitInterfaceCast(StatementSerializationContext context, InterfaceCastExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_INTERFACE_CAST);
		int flags = getFlags(expression);
		serialize(flags, expression);

		output.serialize(context, expression.value);
		output.serialize(context, expression.type);
		return null;
	}

	@Override
	public Void visitInvalid(StatementSerializationContext context, InvalidExpression expression) {
		throw new UnsupportedOperationException("Invalid expression!");
	}

	@Override
	public Void visitInvalidAssign(StatementSerializationContext context, InvalidAssignExpression expression) {
		throw new UnsupportedOperationException("Invalid Assign Expression");
	}

	@Override
	public Void visitIs(StatementSerializationContext context, IsExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_IS);
		int flags = getFlags(expression);
		serialize(flags, expression);

		output.serialize(context, expression.value);
		output.serialize(context, expression.isType);
		return null;
	}

	@Override
	public Void visitMakeConst(StatementSerializationContext context, MakeConstExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_MAKE_CONST);
		int flags = getFlags(expression);
		serialize(flags, expression);

		output.serialize(context, expression.value);
		return null;
	}

	@Override
	public Void visitMap(StatementSerializationContext context, MapExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_MAP);
		int flags = getFlags(expression);
		serialize(flags, expression);

		output.serialize(context, expression.type);
		output.writeUInt(expression.keys.length);
		for (int i = 0; i < expression.keys.length; i++) {
			output.serialize(context, expression.keys[i]);
			output.serialize(context, expression.values[i]);
		}
		return null;
	}

	@Override
	public Void visitMatch(StatementSerializationContext context, MatchExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_MATCH);
		int flags = getFlags(expression);
		if (localVariableNames)
			flags |= ExpressionEncoding.FLAG_NAMES;
		serialize(flags, expression);

		output.serialize(context, expression.value);
		output.serialize(context, expression.type);
		output.writeUInt(expression.cases.length);
		for (MatchExpression.Case case_ : expression.cases) {
			output.serialize(context, case_.key);
			output.serialize(context, case_.value);
		}
		return null;
	}

	@Override
	public Void visitNull(StatementSerializationContext context, NullExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_NULL);
		int flags = getFlags(expression);
		serialize(flags, expression);
		output.serialize(context, expression.type);
		return null;
	}

	@Override
	public Void visitOrOr(StatementSerializationContext context, OrOrExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_OR_OR);
		int flags = getFlags(expression);
		serialize(flags, expression);

		output.serialize(context, expression.left);
		output.serialize(context, expression.right);
		return null;
	}

	@Override
	public Void visitPanic(StatementSerializationContext context, PanicExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_PANIC);
		int flags = getFlags(expression);
		serialize(flags, expression);

		output.serialize(context, expression.value);
		output.serialize(context, expression.type);
		return null;
	}

	@Override
	public Void visitPlatformSpecific(StatementSerializationContext context, Expression expression) {
		throw new UnsupportedOperationException("PlatformSpecific Expression");
	}

	@Override
	public Void visitModification(StatementSerializationContext context, ModificationExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_POST_CALL);
		int flags = getFlags(expression);
		serialize(flags, expression);

		output.serialize(context, expression.target);
		output.write(context, expression.member);
		return null;
	}

	@Override
	public Void visitRange(StatementSerializationContext context, RangeExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_RANGE);
		int flags = getFlags(expression);
		serialize(flags, expression);

		output.serialize(context, expression.from);
		output.serialize(context, expression.to);
		return null;
	}

	@Override
	public Void visitSameObject(StatementSerializationContext context, SameObjectExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_SAME_OBJECT);
		int flags = getFlags(expression);
		if (expression.inverted)
			flags |= ExpressionEncoding.FLAG_INVERTED;
		serialize(flags, expression);

		output.serialize(context, expression.left);
		output.serialize(context, expression.right);
		return null;
	}

	@Override
	public Void visitSetField(StatementSerializationContext context, SetFieldExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_SET_FIELD);
		int flags = getFlags(expression);
		serialize(flags, expression);

		output.serialize(context, expression.target);
		output.write(context, expression.field);
		output.serialize(context, expression.value);
		return null;
	}

	@Override
	public Void visitSetFunctionParameter(StatementSerializationContext context, SetFunctionParameterExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_SET_FUNCTION_PARAMETER);
		int flags = getFlags(expression);
		serialize(flags, expression);

		output.writeUInt(context.getParameterIndex(expression.parameter));
		output.serialize(context, expression.value);
		return null;
	}

	@Override
	public Void visitSetLocalVariable(StatementSerializationContext context, SetLocalVariableExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_SET_LOCAL_VARIABLE);
		int flags = getFlags(expression);
		serialize(flags, expression);

		output.writeUInt(context.getVariableId(expression.variable));
		output.serialize(context, expression.value);
		return null;
	}

	@Override
	public Void visitSetStaticField(StatementSerializationContext context, SetStaticFieldExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_SET_STATIC_FIELD);
		int flags = getFlags(expression);
		serialize(flags, expression);

		output.write(context, expression.field);
		output.serialize(context, expression.value);
		return null;
	}

	@Override
	public Void visitSupertypeCast(StatementSerializationContext context, SupertypeCastExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_SUPERTYPE_CAST);
		int flags = getFlags(expression);
		serialize(flags, expression);

		output.serialize(context, expression.type);
		output.serialize(context, expression.value);
		return null;
	}

	@Override
	public Void visitSubtypeCast(StatementSerializationContext context, SubtypeCastExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_SUBTYPE_CAST);
		int flags = getFlags(expression);
		serialize(flags, expression);

		output.serialize(context, expression.type);
		output.serialize(context, expression.value);
		return null;
	}

	@Override
	public Void visitThis(StatementSerializationContext context, ThisExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_THIS);
		int flags = getFlags(expression);
		serialize(flags, expression);
		return null;
	}

	@Override
	public Void visitThrow(StatementSerializationContext context, ThrowExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_THROW);
		int flags = getFlags(expression);
		serialize(flags, expression);

		output.serialize(context, expression.type);
		output.serialize(context, expression.value);
		return null;
	}

	@Override
	public Void visitTryConvert(StatementSerializationContext context, TryConvertExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_TRY_CONVERT);
		int flags = getFlags(expression);
		serialize(flags, expression);

		output.serialize(context, expression.type);
		output.serialize(context, expression.value);
		return null;
	}

	@Override
	public Void visitTryRethrowAsException(StatementSerializationContext context, TryRethrowAsExceptionExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_TRY_RETHROW_AS_EXCEPTION);
		int flags = getFlags(expression);
		serialize(flags, expression);

		output.serialize(context, expression.type);
		output.serialize(context, expression.thrownType);
		output.serialize(context, expression.value);
		return null;
	}

	@Override
	public Void visitTryRethrowAsResult(StatementSerializationContext context, TryRethrowAsResultExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_TRY_RETHROW_AS_RESULT);
		int flags = getFlags(expression);
		serialize(flags, expression);

		output.serialize(context, expression.type);
		output.serialize(context, expression.value);
		return null;
	}

	@Override
	public Void visitVariantValue(StatementSerializationContext context, VariantValueExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_VARIANT_VALUE);
		int flags = getFlags(expression);
		serialize(flags, expression);

		output.serialize(context, expression.option.variant);
		output.write(expression.option);
		for (Expression argument : expression.arguments)
			output.serialize(context, argument);
		return null;
	}

	@Override
	public Void visitWrapOptional(StatementSerializationContext context, WrapOptionalExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_WRAP_OPTIONAL);
		int flags = getFlags(expression);
		serialize(flags, expression);

		output.serialize(context, expression.value);
		return null;
	}

	private int getCompareType(CompareType type) {
		switch (type) {
			case LT:
				return ExpressionEncoding.COMPARATOR_LT;
			case GT:
				return ExpressionEncoding.COMPARATOR_GT;
			case LE:
				return ExpressionEncoding.COMPARATOR_LE;
			case GE:
				return ExpressionEncoding.COMPARATOR_GE;
			case EQ:
				return ExpressionEncoding.COMPARATOR_EQ;
			case NE:
				return ExpressionEncoding.COMPARATOR_NE;
			default:
				throw new IllegalArgumentException("Unknown comparison: " + type);
		}
	}
}
