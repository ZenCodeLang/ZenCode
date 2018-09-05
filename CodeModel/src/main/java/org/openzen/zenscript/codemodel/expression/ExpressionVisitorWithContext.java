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
	public R visitAndAnd(C context, AndAndExpression expression);
	
	public R visitArray(C context, ArrayExpression expression);
	
	public R visitCompare(C context, CompareExpression expression);
	
	public R visitCall(C context, CallExpression expression);
	
	public R visitCallStatic(C context, CallStaticExpression expression);
	
	public R visitCapturedClosure(C context, CapturedClosureExpression expression);
	
	public R visitCapturedDirect(C context, CapturedDirectExpression expression);
	
	public R visitCapturedLocalVariable(C context, CapturedLocalVariableExpression expression);
	
	public R visitCapturedParameter(C context, CapturedParameterExpression expression);
	
	public R visitCapturedThis(C context, CapturedThisExpression expression);
	
	public R visitCast(C context, CastExpression expression);
	
	public R visitCheckNull(C context, CheckNullExpression expression);
	
	public R visitCoalesce(C context, CoalesceExpression expression);
	
	public R visitConditional(C context, ConditionalExpression expression);
	
	public R visitConst(C context, ConstExpression expression);
	
	public R visitConstantBool(C context, ConstantBoolExpression expression);
	
	public R visitConstantByte(C context, ConstantByteExpression expression);
	
	public R visitConstantChar(C context, ConstantCharExpression expression);
	
	public R visitConstantDouble(C context, ConstantDoubleExpression expression);
	
	public R visitConstantFloat(C context, ConstantFloatExpression expression);
	
	public R visitConstantInt(C context, ConstantIntExpression expression);
	
	public R visitConstantLong(C context, ConstantLongExpression expression);
	
	public R visitConstantSByte(C context, ConstantSByteExpression expression);
	
	public R visitConstantShort(C context, ConstantShortExpression expression);
	
	public R visitConstantString(C context, ConstantStringExpression expression);
	
	public R visitConstantUInt(C context, ConstantUIntExpression expression);
	
	public R visitConstantULong(C context, ConstantULongExpression expression);
	
	public R visitConstantUShort(C context, ConstantUShortExpression expression);
	
	public R visitConstantUSize(C context, ConstantUSizeExpression expression);
	
	public R visitConstructorThisCall(C context, ConstructorThisCallExpression expression);
	
	public R visitConstructorSuperCall(C context, ConstructorSuperCallExpression expression);
	
	public R visitEnumConstant(C context, EnumConstantExpression expression);
	
	public R visitFunction(C context, FunctionExpression expression);
	
	public R visitGetField(C context, GetFieldExpression expression);
	
	public R visitGetFunctionParameter(C context, GetFunctionParameterExpression expression);
	
	public R visitGetLocalVariable(C context, GetLocalVariableExpression expression);
	
	public R visitGetMatchingVariantField(C context, GetMatchingVariantField expression);
	
	public R visitGetStaticField(C context, GetStaticFieldExpression expression);
	
	public R visitGetter(C context, GetterExpression expression);
	
	public R visitGlobal(C context, GlobalExpression expression);
	
	public R visitGlobalCall(C context, GlobalCallExpression expression);
	
	public R visitInterfaceCast(C context, InterfaceCastExpression expression);
	
	public R visitIs(C context, IsExpression expression);
	
	public R visitMakeConst(C context, MakeConstExpression expression);
	
	public R visitMap(C context, MapExpression expression);
	
	public R visitMatch(C context, MatchExpression expression);
	
	public R visitNew(C context, NewExpression expression);
	
	public R visitNull(C context, NullExpression expression);
	
	public R visitOrOr(C context, OrOrExpression expression);
	
	public R visitPanic(C context, PanicExpression expression);
	
	public R visitPostCall(C context, PostCallExpression expression);
	
	public R visitRange(C context, RangeExpression expression);
	
	public R visitSameObject(C context, SameObjectExpression expression);
	
	public R visitSetField(C context, SetFieldExpression expression);
	
	public R visitSetFunctionParameter(C context, SetFunctionParameterExpression expression);
	
	public R visitSetLocalVariable(C context, SetLocalVariableExpression expression);
	
	public R visitSetStaticField(C context, SetStaticFieldExpression expression);
	
	public R visitSetter(C context, SetterExpression expression);
	
	public R visitStaticGetter(C context, StaticGetterExpression expression);
	
	public R visitStaticSetter(C context, StaticSetterExpression expression);
	
	public R visitSupertypeCast(C context, SupertypeCastExpression expression);
	
	public R visitThis(C context, ThisExpression expression);
	
	public R visitThrow(C context, ThrowExpression expression);
	
	public R visitTryConvert(C context, TryConvertExpression expression);
	
	public R visitTryRethrowAsException(C context, TryRethrowAsExceptionExpression expression);
	
	public R visitTryRethrowAsResult(C context, TryRethrowAsResultExpression expression);
	
	public R visitVariantValue(C context, VariantValueExpression expression);
	
	public R visitWrapOptional(C context, WrapOptionalExpression expression);
}
