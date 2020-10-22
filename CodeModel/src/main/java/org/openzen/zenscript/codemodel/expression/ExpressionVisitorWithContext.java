/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

/**
 *
 * @author Hoofdgebruiker
 */
public interface ExpressionVisitorWithContext<C, R> {
	R visitAndAnd(C context, AndAndExpression expression);
	
	R visitArray(C context, ArrayExpression expression);
	
	R visitCompare(C context, CompareExpression expression);
	
	R visitCall(C context, CallExpression expression);
	
	R visitCallStatic(C context, CallStaticExpression expression);
	
	R visitCapturedClosure(C context, CapturedClosureExpression expression);
	
	R visitCapturedDirect(C context, CapturedDirectExpression expression);
	
	R visitCapturedLocalVariable(C context, CapturedLocalVariableExpression expression);
	
	R visitCapturedParameter(C context, CapturedParameterExpression expression);
	
	R visitCapturedThis(C context, CapturedThisExpression expression);
	
	R visitCast(C context, CastExpression expression);
	
	R visitCheckNull(C context, CheckNullExpression expression);
	
	R visitCoalesce(C context, CoalesceExpression expression);
	
	R visitConditional(C context, ConditionalExpression expression);
	
	R visitConst(C context, ConstExpression expression);
	
	R visitConstantBool(C context, ConstantBoolExpression expression);
	
	R visitConstantByte(C context, ConstantByteExpression expression);
	
	R visitConstantChar(C context, ConstantCharExpression expression);
	
	R visitConstantDouble(C context, ConstantDoubleExpression expression);
	
	R visitConstantFloat(C context, ConstantFloatExpression expression);
	
	R visitConstantInt(C context, ConstantIntExpression expression);

	R visitConstantLong(C context, ConstantLongExpression expression);
	
	R visitConstantSByte(C context, ConstantSByteExpression expression);
	
	R visitConstantShort(C context, ConstantShortExpression expression);
	
	R visitConstantString(C context, ConstantStringExpression expression);
	
	R visitConstantUInt(C context, ConstantUIntExpression expression);
	
	R visitConstantULong(C context, ConstantULongExpression expression);
	
	R visitConstantUShort(C context, ConstantUShortExpression expression);
	
	R visitConstantUSize(C context, ConstantUSizeExpression expression);
	
	R visitConstructorThisCall(C context, ConstructorThisCallExpression expression);
	
	R visitConstructorSuperCall(C context, ConstructorSuperCallExpression expression);
	
	R visitEnumConstant(C context, EnumConstantExpression expression);
	
	R visitFunction(C context, FunctionExpression expression);
	
	R visitGetField(C context, GetFieldExpression expression);
	
	R visitGetFunctionParameter(C context, GetFunctionParameterExpression expression);
	
	R visitGetLocalVariable(C context, GetLocalVariableExpression expression);
	
	R visitGetMatchingVariantField(C context, GetMatchingVariantField expression);
	
	R visitGetStaticField(C context, GetStaticFieldExpression expression);
	
	R visitGetter(C context, GetterExpression expression);
	
	R visitGlobal(C context, GlobalExpression expression);
	
	R visitGlobalCall(C context, GlobalCallExpression expression);
	
	R visitInterfaceCast(C context, InterfaceCastExpression expression);
	
	R visitInvalid(C context, InvalidExpression expression);
	
	R visitInvalidAssign(C context, InvalidAssignExpression expression);
	
	R visitIs(C context, IsExpression expression);
	
	R visitMakeConst(C context, MakeConstExpression expression);
	
	R visitMap(C context, MapExpression expression);
	
	R visitMatch(C context, MatchExpression expression);
	
	R visitNew(C context, NewExpression expression);
	
	R visitNull(C context, NullExpression expression);
	
	R visitOrOr(C context, OrOrExpression expression);
	
	R visitPanic(C context, PanicExpression expression);

	R visitPlatformSpecific(C context, Expression expression);

	R visitPostCall(C context, PostCallExpression expression);
	
	R visitRange(C context, RangeExpression expression);
	
	R visitSameObject(C context, SameObjectExpression expression);
	
	R visitSetField(C context, SetFieldExpression expression);
	
	R visitSetFunctionParameter(C context, SetFunctionParameterExpression expression);
	
	R visitSetLocalVariable(C context, SetLocalVariableExpression expression);
	
	R visitSetStaticField(C context, SetStaticFieldExpression expression);
	
	R visitSetter(C context, SetterExpression expression);
	
	R visitStaticGetter(C context, StaticGetterExpression expression);
	
	R visitStaticSetter(C context, StaticSetterExpression expression);

	R visitSupertypeCast(C context, SupertypeCastExpression expression);
	
	R visitThis(C context, ThisExpression expression);
	
	R visitThrow(C context, ThrowExpression expression);
	
	R visitTryConvert(C context, TryConvertExpression expression);
	
	R visitTryRethrowAsException(C context, TryRethrowAsExceptionExpression expression);
	
	R visitTryRethrowAsResult(C context, TryRethrowAsResultExpression expression);
	
	R visitVariantValue(C context, VariantValueExpression expression);
	
	R visitWrapOptional(C context, WrapOptionalExpression expression);
}
