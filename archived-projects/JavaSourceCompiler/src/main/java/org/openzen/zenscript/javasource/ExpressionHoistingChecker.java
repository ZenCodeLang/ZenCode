/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javasource;

import org.openzen.zenscript.codemodel.expression.*;
import org.openzen.zenscript.codemodel.expression.captured.CapturedClosureExpression;
import org.openzen.zenscript.codemodel.expression.captured.CapturedLocalVariableExpression;
import org.openzen.zenscript.codemodel.expression.captured.CapturedParameterExpression;
import org.openzen.zenscript.codemodel.expression.captured.CapturedThisExpression;

/**
 * @author Hoofdgebruiker
 */
public class ExpressionHoistingChecker implements ExpressionVisitor<Boolean> {
	public static final ExpressionHoistingChecker INSTANCE = new ExpressionHoistingChecker();

	private ExpressionHoistingChecker() {
	}

	@Override
	public Boolean visitAndAnd(AndAndExpression expression) {
		return true;
	}

	@Override
	public Boolean visitArray(ArrayExpression expression) {
		return true;
	}

	@Override
	public Boolean visitCompare(CompareExpression expression) {
		return true;
	}

	@Override
	public Boolean visitCall(CallExpression expression) {
		return true;
	}

	@Override
	public Boolean visitCallStatic(CallStaticExpression expression) {
		return true;
	}

	@Override
	public Boolean visitCapturedClosure(CapturedClosureExpression expression) {
		return false;
	}

	@Override
	public Boolean visitCapturedLocalVariable(CapturedLocalVariableExpression expression) {
		return false;
	}

	@Override
	public Boolean visitCapturedParameter(CapturedParameterExpression expression) {
		return false;
	}

	@Override
	public Boolean visitCapturedThis(CapturedThisExpression expression) {
		return false;
	}

	@Override
	public Boolean visitCheckNull(CheckNullExpression expression) {
		return expression.value.accept(this);
	}

	@Override
	public Boolean visitCoalesce(CoalesceExpression expression) {
		return true;
	}

	@Override
	public Boolean visitConditional(ConditionalExpression expression) {
		return true;
	}

	@Override
	public Boolean visitConstantBool(ConstantBoolExpression expression) {
		return false;
	}

	@Override
	public Boolean visitConstantByte(ConstantByteExpression expression) {
		return false;
	}

	@Override
	public Boolean visitConstantChar(ConstantCharExpression expression) {
		return false;
	}

	@Override
	public Boolean visitConstantDouble(ConstantDoubleExpression expression) {
		return false;
	}

	@Override
	public Boolean visitConstantFloat(ConstantFloatExpression expression) {
		return false;
	}

	@Override
	public Boolean visitConstantInt(ConstantIntExpression expression) {
		return false;
	}

	@Override
	public Boolean visitConstantLong(ConstantLongExpression expression) {
		return false;
	}

	@Override
	public Boolean visitConstantSByte(ConstantSByteExpression expression) {
		return false;
	}

	@Override
	public Boolean visitConstantShort(ConstantShortExpression expression) {
		return false;
	}

	@Override
	public Boolean visitConstantString(ConstantStringExpression expression) {
		return false;
	}

	@Override
	public Boolean visitConstantUInt(ConstantUIntExpression expression) {
		return false;
	}

	@Override
	public Boolean visitConstantULong(ConstantULongExpression expression) {
		return false;
	}

	@Override
	public Boolean visitConstantUShort(ConstantUShortExpression expression) {
		return false;
	}

	@Override
	public Boolean visitConstantUSize(ConstantUSizeExpression expression) {
		return false;
	}

	@Override
	public Boolean visitConstructorThisCall(ConstructorThisCallExpression expression) {
		return true;
	}

	@Override
	public Boolean visitConstructorSuperCall(ConstructorSuperCallExpression expression) {
		return true;
	}

	@Override
	public Boolean visitEnumConstant(EnumConstantExpression expression) {
		return false;
	}

	@Override
	public Boolean visitFunction(FunctionExpression expression) {
		return true;
	}

	@Override
	public Boolean visitGetField(GetFieldExpression expression) {
		return false;
	}

	@Override
	public Boolean visitGetFunctionParameter(GetFunctionParameterExpression expression) {
		return false;
	}

	@Override
	public Boolean visitGetLocalVariable(GetLocalVariableExpression expression) {
		return false;
	}

	@Override
	public Boolean visitGetMatchingVariantField(GetMatchingVariantField expression) {
		return false;
	}

	@Override
	public Boolean visitGetStaticField(GetStaticFieldExpression expression) {
		return false;
	}

	@Override
	public Boolean visitGlobal(GlobalExpression expression) {
		return false;
	}

	@Override
	public Boolean visitGlobalCall(GlobalCallExpression expression) {
		return true;
	}

	@Override
	public Boolean visitInterfaceCast(InterfaceCastExpression expression) {
		return false;
	}

	@Override
	public Boolean visitIs(IsExpression expression) {
		return true;
	}

	@Override
	public Boolean visitMakeConst(MakeConstExpression expression) {
		return true;
	}

	@Override
	public Boolean visitMap(MapExpression expression) {
		return true;
	}

	@Override
	public Boolean visitMatch(MatchExpression expression) {
		return true;
	}

	@Override
	public Boolean visitNull(NullExpression expression) {
		return false;
	}

	@Override
	public Boolean visitOrOr(OrOrExpression expression) {
		return true;
	}

	@Override
	public Boolean visitPanic(PanicExpression expression) {
		return true;
	}

	@Override
	public Boolean visitPlatformSpecific(Expression expression) {
		return false;
	}

	@Override
	public Boolean visitModification(ModificationExpression expression) {
		return true;
	}

	@Override
	public Boolean visitRange(RangeExpression expression) {
		return true;
	}

	@Override
	public Boolean visitSameObject(SameObjectExpression expression) {
		return true;
	}

	@Override
	public Boolean visitSetField(SetFieldExpression expression) {
		return true;
	}

	@Override
	public Boolean visitSetFunctionParameter(SetFunctionParameterExpression expression) {
		return true;
	}

	@Override
	public Boolean visitSetLocalVariable(SetLocalVariableExpression expression) {
		return true;
	}

	@Override
	public Boolean visitSetStaticField(SetStaticFieldExpression expression) {
		return true;
	}

	@Override
	public Boolean visitSupertypeCast(SupertypeCastExpression expression) {
		return true;
	}

	@Override
	public Boolean visitSubtypeCast(SubtypeCastExpression expression) {
		return true;
	}

	@Override
	public Boolean visitThis(ThisExpression expression) {
		return false;
	}

	@Override
	public Boolean visitThrow(ThrowExpression expression) {
		return true;
	}

	@Override
	public Boolean visitTryConvert(TryConvertExpression expression) {
		return true;
	}

	@Override
	public Boolean visitTryRethrowAsException(TryRethrowAsExceptionExpression expression) {
		return true;
	}

	@Override
	public Boolean visitTryRethrowAsResult(TryRethrowAsResultExpression expression) {
		return true;
	}

	@Override
	public Boolean visitVariantValue(VariantValueExpression expression) {
		return false;
	}

	@Override
	public Boolean visitWrapOptional(WrapOptionalExpression expression) {
		return true;
	}
}
