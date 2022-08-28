package org.openzen.zenscript.codemodel.expression;

public interface ExpressionVisitor<T> {
	T visitAndAnd(AndAndExpression expression);

	T visitArray(ArrayExpression expression);

	T visitCompare(CompareExpression expression);

	T visitCall(CallExpression expression);

	T visitCallStatic(CallStaticExpression expression);

	T visitCapturedClosure(CapturedClosureExpression expression);

	T visitCapturedLocalVariable(CapturedLocalVariableExpression expression);

	T visitCapturedParameter(CapturedParameterExpression expression);

	T visitCapturedThis(CapturedThisExpression expression);

	T visitCheckNull(CheckNullExpression expression);

	T visitCoalesce(CoalesceExpression expression);

	T visitConditional(ConditionalExpression expression);

	T visitConstantBool(ConstantBoolExpression expression);

	T visitConstantByte(ConstantByteExpression expression);

	T visitConstantChar(ConstantCharExpression expression);

	T visitConstantDouble(ConstantDoubleExpression expression);

	T visitConstantFloat(ConstantFloatExpression expression);

	T visitConstantInt(ConstantIntExpression expression);

	T visitConstantLong(ConstantLongExpression expression);

	T visitConstantSByte(ConstantSByteExpression expression);

	T visitConstantShort(ConstantShortExpression expression);

	T visitConstantString(ConstantStringExpression expression);

	T visitConstantUInt(ConstantUIntExpression expression);

	T visitConstantULong(ConstantULongExpression expression);

	T visitConstantUShort(ConstantUShortExpression expression);

	T visitConstantUSize(ConstantUSizeExpression expression);

	T visitConstructorThisCall(ConstructorThisCallExpression expression);

	T visitConstructorSuperCall(ConstructorSuperCallExpression expression);

	T visitEnumConstant(EnumConstantExpression expression);

	T visitFunction(FunctionExpression expression);

	T visitGetField(GetFieldExpression expression);

	T visitGetFunctionParameter(GetFunctionParameterExpression expression);

	T visitGetLocalVariable(GetLocalVariableExpression expression);

	T visitGetMatchingVariantField(GetMatchingVariantField expression);

	T visitGetStaticField(GetStaticFieldExpression expression);

	T visitGlobal(GlobalExpression expression);

	T visitGlobalCall(GlobalCallExpression expression);

	T visitInterfaceCast(InterfaceCastExpression expression);

	default T visitInvalid(InvalidExpression expression) {
		throw new RuntimeException("Invalid expression @ " + expression.position + ": " + expression.error.description);
	}

	default T visitInvalidAssign(InvalidAssignExpression expression) {
		throw new RuntimeException("Invalid expression @ " + expression.position + ": " + expression.target.error.description);
	}

	T visitIs(IsExpression expression);

	T visitMakeConst(MakeConstExpression expression);

	T visitMap(MapExpression expression);

	T visitMatch(MatchExpression expression);

	T visitNew(NewExpression expression);

	T visitNull(NullExpression expression);

	T visitOrOr(OrOrExpression expression);

	T visitPanic(PanicExpression expression);

	T visitPlatformSpecific(Expression expression);

	T visitPostCall(PostCallExpression expression);

	T visitRange(RangeExpression expression);

	T visitSameObject(SameObjectExpression expression);

	T visitSetField(SetFieldExpression expression);

	T visitSetFunctionParameter(SetFunctionParameterExpression expression);

	T visitSetLocalVariable(SetLocalVariableExpression expression);

	T visitSetStaticField(SetStaticFieldExpression expression);

	T visitSupertypeCast(SupertypeCastExpression expression);

	T visitSubtypeCast(SubtypeCastExpression expression);

	T visitThis(ThisExpression expression);

	T visitThrow(ThrowExpression expression);

	T visitTryConvert(TryConvertExpression expression);

	T visitTryRethrowAsException(TryRethrowAsExceptionExpression expression);

	T visitTryRethrowAsResult(TryRethrowAsResultExpression expression);

	T visitVariantValue(VariantValueExpression expression);

	T visitWrapOptional(WrapOptionalExpression expression);
}
