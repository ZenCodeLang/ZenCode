/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.moduleserializer.encoder;

import org.openzen.zenscript.codemodel.context.StatementContext;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.CompareType;
import org.openzen.zenscript.codemodel.expression.*;
import org.openzen.zenscript.codemodel.serialization.CodeSerializationOutput;
import org.openzen.zenscript.moduleserialization.ExpressionEncoding;

/**
 *
 * @author Hoofdgebruiker
 */
public class ExpressionSerializer implements ExpressionVisitorWithContext<StatementContext, Void> {
	private final CodeSerializationOutput output;
	private final boolean positions;
	
	public ExpressionSerializer(CodeSerializationOutput output, boolean positions) {
		this.output = output;
		this.positions = positions;
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
	public Void visitAndAnd(StatementContext context, AndAndExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_AND_AND);
		int flags = getFlags(expression);
		serialize(flags, expression);
		
		expression.left.accept(context, this);
		expression.right.accept(context, this);
		return null;
	}

	@Override
	public Void visitArray(StatementContext context, ArrayExpression expression) {
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
	public Void visitCompare(StatementContext context, CompareExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_COMPARE);
		int flags = getFlags(expression);
		serialize(flags, expression);
		
		output.writeUInt(getCompareType(expression.comparison));
		output.serialize(context, expression.left);
		output.serialize(context, expression.right);
		return null;
	}

	@Override
	public Void visitCall(StatementContext context, CallExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_CALL);
		int flags = getFlags(expression);
		serialize(flags, expression);
		
		output.write(context, expression.member);
		output.serialize(context, expression.target);
		output.serialize(context, expression.arguments);
		return null;
	}

	@Override
	public Void visitCallStatic(StatementContext context, CallStaticExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_CALL_STATIC);
		int flags = getFlags(expression);
		serialize(flags, expression);
		
		output.write(context, expression.member);
		output.serialize(context, expression.target);
		output.serialize(context, expression.arguments);
		return null;
	}

	@Override
	public Void visitCapturedClosure(StatementContext context, CapturedClosureExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_CAPTURED_CLOSURE);
		int flags = getFlags(expression);
		serialize(flags, expression);
		
		output.serialize(context, expression.value);
		return null;
	}

	@Override
	public Void visitCapturedDirect(StatementContext context, CapturedDirectExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_CAPTURED_DIRECT);
		int flags = getFlags(expression);
		serialize(flags, expression);
		
		output.serialize(context, expression.value);
		return null;
	}

	@Override
	public Void visitCapturedLocalVariable(StatementContext context, CapturedLocalVariableExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_CAPTURED_LOCAL_VARIABLE);
		int flags = getFlags(expression);
		serialize(flags, expression);
		
		output.writeUInt(context.getVariableId(expression.variable));
		return null;
	}

	@Override
	public Void visitCapturedParameter(StatementContext context, CapturedParameterExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_CAPTURED_PARAMETER);
		int flags = getFlags(expression);
		serialize(flags, expression);
		
		output.writeUInt(context.getParameterIndex(expression.parameter));
		return null;
	}

	@Override
	public Void visitCapturedThis(StatementContext context, CapturedThisExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_CAPTURED_THIS);
		int flags = getFlags(expression);
		serialize(flags, expression);
		
		return null;
	}

	@Override
	public Void visitCast(StatementContext context, CastExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_CAST);
		int flags = getFlags(expression);
		if (expression.isImplicit)
			flags |= ExpressionEncoding.FLAG_IMPLICIT;
		serialize(flags, expression);
		
		output.serialize(context, expression.target);
		output.write(context, expression.member);
		return null;
	}

	@Override
	public Void visitCheckNull(StatementContext context, CheckNullExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_CHECKNULL);
		int flags = getFlags(expression);
		serialize(flags, expression);
		
		output.serialize(context, expression.value);
		return null;
	}

	@Override
	public Void visitCoalesce(StatementContext context, CoalesceExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_COALESCE);
		int flags = getFlags(expression);
		serialize(flags, expression);
		
		output.serialize(context, expression.left);
		output.serialize(context, expression.right);
		return null;
	}

	@Override
	public Void visitConditional(StatementContext context, ConditionalExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_CONDITIONAL);
		int flags = getFlags(expression);
		serialize(flags, expression);
		
		output.serialize(context, expression.condition);
		output.serialize(context, expression.ifThen);
		output.serialize(context, expression.ifElse);
		return null;
	}

	@Override
	public Void visitConst(StatementContext context, ConstExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_CONST);
		int flags = getFlags(expression);
		serialize(flags, expression);
		
		output.write(context, expression.constant);
		return null;
	}

	@Override
	public Void visitConstantBool(StatementContext context, ConstantBoolExpression expression) {
		output.writeUInt(expression.value ? ExpressionEncoding.TYPE_CONSTANT_BOOL_TRUE : ExpressionEncoding.TYPE_CONSTANT_BOOL_FALSE);
		int flags = getFlags(expression);
		serialize(flags, expression);
		return null;
	}

	@Override
	public Void visitConstantByte(StatementContext context, ConstantByteExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_CONSTANT_BYTE);
		int flags = getFlags(expression);
		serialize(flags, expression);
		
		output.writeByte(expression.value);
		return null;
	}

	@Override
	public Void visitConstantChar(StatementContext context, ConstantCharExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_CONSTANT_CHAR);
		int flags = getFlags(expression);
		serialize(flags, expression);
		
		output.writeChar(expression.value);
		return null;
	}

	@Override
	public Void visitConstantDouble(StatementContext context, ConstantDoubleExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_CONSTANT_DOUBLE);
		int flags = getFlags(expression);
		serialize(flags, expression);
		
		output.writeDouble(expression.value);
		return null;
	}

	@Override
	public Void visitConstantFloat(StatementContext context, ConstantFloatExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_CONSTANT_FLOAT);
		int flags = getFlags(expression);
		serialize(flags, expression);
		
		output.writeFloat(expression.value);
		return null;
	}

	@Override
	public Void visitConstantInt(StatementContext context, ConstantIntExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_CONSTANT_INT);
		int flags = getFlags(expression);
		serialize(flags, expression);
		
		output.writeInt(expression.value);
		return null;
	}

	@Override
	public Void visitConstantLong(StatementContext context, ConstantLongExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_CONSTANT_LONG);
		int flags = getFlags(expression);
		serialize(flags, expression);
		
		output.writeLong(expression.value);
		return null;
	}

	@Override
	public Void visitConstantSByte(StatementContext context, ConstantSByteExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_CONSTANT_SBYTE);
		int flags = getFlags(expression);
		serialize(flags, expression);
		
		output.writeSByte(expression.value);
		return null;
	}

	@Override
	public Void visitConstantShort(StatementContext context, ConstantShortExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_CONSTANT_SHORT);
		int flags = getFlags(expression);
		serialize(flags, expression);
		
		output.writeShort(expression.value);
		return null;
	}

	@Override
	public Void visitConstantString(StatementContext context, ConstantStringExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_CONSTANT_STRING);
		int flags = getFlags(expression);
		serialize(flags, expression);
		
		output.writeString(expression.value);
		return null;
	}

	@Override
	public Void visitConstantUInt(StatementContext context, ConstantUIntExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_CONSTANT_UINT);
		int flags = getFlags(expression);
		serialize(flags, expression);
		
		output.writeUInt(expression.value);
		return null;
	}

	@Override
	public Void visitConstantULong(StatementContext context, ConstantULongExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_CONSTANT_ULONG);
		int flags = getFlags(expression);
		serialize(flags, expression);
		
		output.writeULong(expression.value);
		return null;
	}

	@Override
	public Void visitConstantUShort(StatementContext context, ConstantUShortExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_CONSTANT_USHORT);
		int flags = getFlags(expression);
		serialize(flags, expression);
		
		output.writeUShort(expression.value);
		return null;
	}

	@Override
	public Void visitConstantUSize(StatementContext context, ConstantUSizeExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_CONSTANT_USIZE);
		int flags = getFlags(expression);
		serialize(flags, expression);
		
		output.writeULong(expression.value);
		return null;
	}

	@Override
	public Void visitConstructorThisCall(StatementContext context, ConstructorThisCallExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_CONSTRUCTOR_THIS_CALL);
		int flags = getFlags(expression);
		serialize(flags, expression);
		
		output.write(context, expression.constructor);
		output.serialize(context, expression.arguments);
		return null;
	}

	@Override
	public Void visitConstructorSuperCall(StatementContext context, ConstructorSuperCallExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_CONSTRUCTOR_SUPER_CALL);
		int flags = getFlags(expression);
		serialize(flags, expression);
		
		output.write(context, expression.constructor);
		output.serialize(context, expression.arguments);
		return null;
	}

	@Override
	public Void visitEnumConstant(StatementContext context, EnumConstantExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_ENUM_CONSTANT);
		int flags = getFlags(expression);
		serialize(flags, expression);
		
		output.write(expression.value);
		return null;
	}

	@Override
	public Void visitFunction(StatementContext context, FunctionExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_FUNCTION);
		int flags = getFlags(expression);
		serialize(flags, expression);
		
		StatementContext innerContext = new StatementContext(context, expression.header);
		output.serialize(context, expression.header);
		output.serialize(innerContext, expression.body);
		return null;
	}

	@Override
	public Void visitGetField(StatementContext context, GetFieldExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_GET_FIELD);
		int flags = getFlags(expression);
		serialize(flags, expression);
		
		output.serialize(context, expression.target);
		output.write(context, expression.field);
		return null;
	}

	@Override
	public Void visitGetFunctionParameter(StatementContext context, GetFunctionParameterExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_GET_FUNCTION_PARAMETER);
		int flags = getFlags(expression);
		serialize(flags, expression);
		
		output.writeUInt(context.getParameterIndex(expression.parameter));
		return null;
	}

	@Override
	public Void visitGetLocalVariable(StatementContext context, GetLocalVariableExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_GET_LOCAL_VARIABLE);
		int flags = getFlags(expression);
		serialize(flags, expression);
		
		output.writeUInt(context.getVariableId(expression.variable));
		return null;
	}

	@Override
	public Void visitGetMatchingVariantField(StatementContext context, GetMatchingVariantField expression) {
		output.writeUInt(ExpressionEncoding.TYPE_GET_MATCHING_VARIANT_FIELD);
		int flags = getFlags(expression);
		serialize(flags, expression);
		
		output.writeUInt(expression.index);
		return null;
	}

	@Override
	public Void visitGetStaticField(StatementContext context, GetStaticFieldExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_GET_STATIC_FIELD);
		int flags = getFlags(expression);
		serialize(flags, expression);
		
		output.write(context, expression.field);
		return null;
	}

	@Override
	public Void visitGetter(StatementContext context, GetterExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_GETTER);
		int flags = getFlags(expression);
		serialize(flags, expression);
		
		output.serialize(context, expression.target);
		output.write(context, expression.getter);
		return null;
	}

	@Override
	public Void visitGlobal(StatementContext context, GlobalExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_GLOBAL);
		int flags = getFlags(expression);
		serialize(flags, expression);
		
		output.writeString(expression.name);
		return null;
	}

	@Override
	public Void visitGlobalCall(StatementContext context, GlobalCallExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_GLOBAL_CALL);
		int flags = getFlags(expression);
		serialize(flags, expression);
		
		output.writeString(expression.name);
		output.serialize(context, expression.arguments);
		return null;
	}

	@Override
	public Void visitInterfaceCast(StatementContext context, InterfaceCastExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_INTERFACE_CAST);
		int flags = getFlags(expression);
		serialize(flags, expression);
		
		output.serialize(context, expression.value);
		output.serialize(context, expression.type);
		return null;
	}

	@Override
	public Void visitIs(StatementContext context, IsExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_IS);
		int flags = getFlags(expression);
		serialize(flags, expression);
		
		output.serialize(context, expression.value);
		output.serialize(context, expression.isType);
		return null;
	}

	@Override
	public Void visitMakeConst(StatementContext context, MakeConstExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_MAKE_CONST);
		int flags = getFlags(expression);
		serialize(flags, expression);
		
		output.serialize(context, expression.value);
		return null;
	}

	@Override
	public Void visitMap(StatementContext context, MapExpression expression) {
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
	public Void visitMatch(StatementContext context, MatchExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_MATCH);
		int flags = getFlags(expression);
		serialize(flags, expression);
		
		output.serialize(context, expression.value);
		output.writeUInt(expression.cases.length);
		for (MatchExpression.Case case_ : expression.cases) {
			output.serialize(context, case_.key);
			output.serialize(context, case_.value);
		}
		return null;
	}

	@Override
	public Void visitNew(StatementContext context, NewExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_NEW);
		int flags = getFlags(expression);
		serialize(flags, expression);
		
		output.write(context, expression.constructor);
		output.serialize(context, expression.arguments);
		return null;
	}

	@Override
	public Void visitNull(StatementContext context, NullExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_NULL);
		int flags = getFlags(expression);
		serialize(flags, expression);
		return null;
	}

	@Override
	public Void visitOrOr(StatementContext context, OrOrExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_OR_OR);
		int flags = getFlags(expression);
		serialize(flags, expression);
		
		output.serialize(context, expression.left);
		output.serialize(context, expression.right);
		return null;
	}

	@Override
	public Void visitPanic(StatementContext context, PanicExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_PANIC);
		int flags = getFlags(expression);
		serialize(flags, expression);
		
		output.serialize(context, expression.value);
		return null;
	}

	@Override
	public Void visitPostCall(StatementContext context, PostCallExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_POST_CALL);
		int flags = getFlags(expression);
		serialize(flags, expression);
		
		output.serialize(context, expression.target);
		output.write(context, expression.member);
		return null;
	}

	@Override
	public Void visitRange(StatementContext context, RangeExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_RANGE);
		int flags = getFlags(expression);
		serialize(flags, expression);
		
		output.serialize(context, expression.from);
		output.serialize(context, expression.to);
		return null;
	}

	@Override
	public Void visitSameObject(StatementContext context, SameObjectExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_SAME_OBJECT);
		int flags = getFlags(expression);
		serialize(flags, expression);
		
		output.serialize(context, expression.left);
		output.serialize(context, expression.right);
		return null;
	}

	@Override
	public Void visitSetField(StatementContext context, SetFieldExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_SET_FIELD);
		int flags = getFlags(expression);
		serialize(flags, expression);
		
		output.serialize(context, expression.target);
		output.write(context, expression.field);
		output.serialize(context, expression.value);
		return null;
	}

	@Override
	public Void visitSetFunctionParameter(StatementContext context, SetFunctionParameterExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_SET_FUNCTION_PARAMETER);
		int flags = getFlags(expression);
		serialize(flags, expression);
		
		output.writeUInt(context.getParameterIndex(expression.parameter));
		output.serialize(context, expression.value);
		return null;
	}

	@Override
	public Void visitSetLocalVariable(StatementContext context, SetLocalVariableExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_SET_LOCAL_VARIABLE);
		int flags = getFlags(expression);
		serialize(flags, expression);
		
		output.writeUInt(context.getVariableId(expression.variable));
		output.serialize(context, expression.value);
		return null;
	}

	@Override
	public Void visitSetStaticField(StatementContext context, SetStaticFieldExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_SET_STATIC_FIELD);
		int flags = getFlags(expression);
		serialize(flags, expression);
		
		output.write(context, expression.field);
		output.serialize(context, expression.value);
		return null;
	}

	@Override
	public Void visitSetter(StatementContext context, SetterExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_SETTER);
		int flags = getFlags(expression);
		serialize(flags, expression);
		
		output.serialize(context, expression.target);
		output.write(context, expression.setter);
		output.serialize(context, expression.value);
		return null;
	}

	@Override
	public Void visitStaticGetter(StatementContext context, StaticGetterExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_STATIC_GETTER);
		int flags = getFlags(expression);
		serialize(flags, expression);
		
		output.write(context, expression.getter);
		return null;
	}

	@Override
	public Void visitStaticSetter(StatementContext context, StaticSetterExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_STATIC_SETTER);
		int flags = getFlags(expression);
		serialize(flags, expression);
		
		output.write(context, expression.setter);
		output.serialize(context, expression.value);
		return null;
	}

	@Override
	public Void visitSupertypeCast(StatementContext context, SupertypeCastExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_SUPERTYPE_CAST);
		int flags = getFlags(expression);
		serialize(flags, expression);
		
		output.serialize(context, expression.value);
		output.serialize(context, expression.type);
		return null;
	}

	@Override
	public Void visitThis(StatementContext context, ThisExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_THIS);
		int flags = getFlags(expression);
		serialize(flags, expression);
		return null;
	}

	@Override
	public Void visitThrow(StatementContext context, ThrowExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_THROW);
		int flags = getFlags(expression);
		serialize(flags, expression);
		
		output.serialize(context, expression.value);
		return null;
	}

	@Override
	public Void visitTryConvert(StatementContext context, TryConvertExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_TRY_CONVERT);
		int flags = getFlags(expression);
		serialize(flags, expression);
		
		output.serialize(context, expression.value);
		return null;
	}

	@Override
	public Void visitTryRethrowAsException(StatementContext context, TryRethrowAsExceptionExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_TRY_RETHROW_AS_EXCEPTION);
		int flags = getFlags(expression);
		serialize(flags, expression);
		
		output.serialize(context, expression.value);
		return null;
	}

	@Override
	public Void visitTryRethrowAsResult(StatementContext context, TryRethrowAsResultExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_TRY_RETHROW_AS_RESULT);
		int flags = getFlags(expression);
		serialize(flags, expression);
		
		output.serialize(context, expression.value);
		return null;
	}

	@Override
	public Void visitVariantValue(StatementContext context, VariantValueExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_VARIANT_VALUE);
		int flags = getFlags(expression);
		serialize(flags, expression);
		
		output.write(expression.option);
		output.writeUInt(expression.arguments.length);
		for (Expression argument : expression.arguments)
			output.serialize(context, argument);
		return null;
	}

	@Override
	public Void visitWrapOptional(StatementContext context, WrapOptionalExpression expression) {
		output.writeUInt(ExpressionEncoding.TYPE_WRAP_OPTIONAL);
		int flags = getFlags(expression);
		serialize(flags, expression);
		
		output.serialize(context, expression.value);
		return null;
	}
	
	private int getCompareType(CompareType type) {
		switch (type) {
			case LT: return ExpressionEncoding.COMPARATOR_LT;
			case GT: return ExpressionEncoding.COMPARATOR_GT;
			case LE: return ExpressionEncoding.COMPARATOR_LE;
			case GE: return ExpressionEncoding.COMPARATOR_GE;
			case EQ: return ExpressionEncoding.COMPARATOR_EQ;
			case NE: return ExpressionEncoding.COMPARATOR_NE;
			default: throw new IllegalArgumentException("Unknown comparison: " + type);
		}
	}
}
