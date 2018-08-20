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
public interface ExpressionVisitor<T> {
	public T visitAndAnd(AndAndExpression expression);
	
	public T visitArray(ArrayExpression expression);
	
	public T visitCompare(CompareExpression expression);
	
	public T visitCall(CallExpression expression);
	
	public T visitCallStatic(CallStaticExpression expression);
	
	public T visitCapturedClosure(CapturedClosureExpression expression);
	
	public T visitCapturedDirect(CapturedDirectExpression expression);
	
	public T visitCapturedLocalVariable(CapturedLocalVariableExpression expression);
	
	public T visitCapturedParameter(CapturedParameterExpression expression);
	
	public T visitCapturedThis(CapturedThisExpression expression);
	
	public T visitCast(CastExpression expression);
	
	public T visitCheckNull(CheckNullExpression expression);
	
	public T visitCoalesce(CoalesceExpression expression);
	
	public T visitConditional(ConditionalExpression expression);
	
	public T visitConst(ConstExpression expression);
	
	public T visitConstantBool(ConstantBoolExpression expression);
	
	public T visitConstantByte(ConstantByteExpression expression);
	
	public T visitConstantChar(ConstantCharExpression expression);
	
	public T visitConstantDouble(ConstantDoubleExpression expression);
	
	public T visitConstantFloat(ConstantFloatExpression expression);
	
	public T visitConstantInt(ConstantIntExpression expression);
	
	public T visitConstantLong(ConstantLongExpression expression);
	
	public T visitConstantSByte(ConstantSByteExpression expression);
	
	public T visitConstantShort(ConstantShortExpression expression);
	
	public T visitConstantString(ConstantStringExpression expression);
	
	public T visitConstantUInt(ConstantUIntExpression expression);
	
	public T visitConstantULong(ConstantULongExpression expression);
	
	public T visitConstantUShort(ConstantUShortExpression expression);
	
	public T visitConstructorThisCall(ConstructorThisCallExpression expression);
	
	public T visitConstructorSuperCall(ConstructorSuperCallExpression expression);
	
	public T visitEnumConstant(EnumConstantExpression expression);
	
	public T visitFunction(FunctionExpression expression);
	
	public T visitGetField(GetFieldExpression expression);
	
	public T visitGetFunctionParameter(GetFunctionParameterExpression expression);
	
	public T visitGetLocalVariable(GetLocalVariableExpression expression);
	
	public T visitGetMatchingVariantField(GetMatchingVariantField expression);
	
	public T visitGetStaticField(GetStaticFieldExpression expression);
	
	public T visitGetter(GetterExpression expression);
	
	public T visitGlobal(GlobalExpression expression);
	
	public T visitGlobalCall(GlobalCallExpression expression);
	
	public T visitInterfaceCast(InterfaceCastExpression expression);
	
	public T visitIs(IsExpression expression);
	
	public T visitMakeConst(MakeConstExpression expression);
	
	public T visitMap(MapExpression expression);
	
	public T visitMatch(MatchExpression expression);
	
	public T visitNew(NewExpression expression);
	
	public T visitNull(NullExpression expression);
	
	public T visitOrOr(OrOrExpression expression);
	
	public T visitPanic(PanicExpression expression);
	
	public T visitPostCall(PostCallExpression expression);
	
	public T visitRange(RangeExpression expression);
	
	public T visitSameObject(SameObjectExpression expression);
	
	public T visitSetField(SetFieldExpression expression);
	
	public T visitSetFunctionParameter(SetFunctionParameterExpression expression);
	
	public T visitSetLocalVariable(SetLocalVariableExpression expression);
	
	public T visitSetStaticField(SetStaticFieldExpression expression);
	
	public T visitSetter(SetterExpression expression);
	
	public T visitStaticGetter(StaticGetterExpression expression);
	
	public T visitStaticSetter(StaticSetterExpression expression);
	
	public T visitSupertypeCast(SupertypeCastExpression expression);
	
	public T visitThis(ThisExpression expression);
	
	public T visitThrow(ThrowExpression expression);
	
	public T visitTryConvert(TryConvertExpression expression);
	
	public T visitTryRethrowAsException(TryRethrowAsExceptionExpression expression);
	
	public T visitTryRethrowAsResult(TryRethrowAsResultExpression expression);
	
	public T visitVariantValue(VariantValueExpression expression);
	
	public T visitWrapOptional(WrapOptionalExpression expression);
}
