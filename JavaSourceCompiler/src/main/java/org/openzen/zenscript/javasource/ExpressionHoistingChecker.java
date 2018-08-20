/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javasource;

import org.openzen.zenscript.codemodel.expression.AndAndExpression;
import org.openzen.zenscript.codemodel.expression.ArrayExpression;
import org.openzen.zenscript.codemodel.expression.CallExpression;
import org.openzen.zenscript.codemodel.expression.CallStaticExpression;
import org.openzen.zenscript.codemodel.expression.CapturedClosureExpression;
import org.openzen.zenscript.codemodel.expression.CapturedDirectExpression;
import org.openzen.zenscript.codemodel.expression.CapturedLocalVariableExpression;
import org.openzen.zenscript.codemodel.expression.CapturedParameterExpression;
import org.openzen.zenscript.codemodel.expression.CapturedThisExpression;
import org.openzen.zenscript.codemodel.expression.CastExpression;
import org.openzen.zenscript.codemodel.expression.CheckNullExpression;
import org.openzen.zenscript.codemodel.expression.CoalesceExpression;
import org.openzen.zenscript.codemodel.expression.CompareExpression;
import org.openzen.zenscript.codemodel.expression.ConditionalExpression;
import org.openzen.zenscript.codemodel.expression.ConstExpression;
import org.openzen.zenscript.codemodel.expression.ConstantBoolExpression;
import org.openzen.zenscript.codemodel.expression.ConstantByteExpression;
import org.openzen.zenscript.codemodel.expression.ConstantCharExpression;
import org.openzen.zenscript.codemodel.expression.ConstantDoubleExpression;
import org.openzen.zenscript.codemodel.expression.ConstantFloatExpression;
import org.openzen.zenscript.codemodel.expression.ConstantIntExpression;
import org.openzen.zenscript.codemodel.expression.ConstantLongExpression;
import org.openzen.zenscript.codemodel.expression.ConstantSByteExpression;
import org.openzen.zenscript.codemodel.expression.ConstantShortExpression;
import org.openzen.zenscript.codemodel.expression.ConstantStringExpression;
import org.openzen.zenscript.codemodel.expression.ConstantUIntExpression;
import org.openzen.zenscript.codemodel.expression.ConstantULongExpression;
import org.openzen.zenscript.codemodel.expression.ConstantUShortExpression;
import org.openzen.zenscript.codemodel.expression.ConstructorSuperCallExpression;
import org.openzen.zenscript.codemodel.expression.ConstructorThisCallExpression;
import org.openzen.zenscript.codemodel.expression.EnumConstantExpression;
import org.openzen.zenscript.codemodel.expression.ExpressionVisitor;
import org.openzen.zenscript.codemodel.expression.FunctionExpression;
import org.openzen.zenscript.codemodel.expression.GetFieldExpression;
import org.openzen.zenscript.codemodel.expression.GetFunctionParameterExpression;
import org.openzen.zenscript.codemodel.expression.GetLocalVariableExpression;
import org.openzen.zenscript.codemodel.expression.GetMatchingVariantField;
import org.openzen.zenscript.codemodel.expression.GetStaticFieldExpression;
import org.openzen.zenscript.codemodel.expression.GetterExpression;
import org.openzen.zenscript.codemodel.expression.GlobalCallExpression;
import org.openzen.zenscript.codemodel.expression.GlobalExpression;
import org.openzen.zenscript.codemodel.expression.InterfaceCastExpression;
import org.openzen.zenscript.codemodel.expression.IsExpression;
import org.openzen.zenscript.codemodel.expression.MakeConstExpression;
import org.openzen.zenscript.codemodel.expression.MapExpression;
import org.openzen.zenscript.codemodel.expression.MatchExpression;
import org.openzen.zenscript.codemodel.expression.NewExpression;
import org.openzen.zenscript.codemodel.expression.NullExpression;
import org.openzen.zenscript.codemodel.expression.OrOrExpression;
import org.openzen.zenscript.codemodel.expression.PanicExpression;
import org.openzen.zenscript.codemodel.expression.PostCallExpression;
import org.openzen.zenscript.codemodel.expression.RangeExpression;
import org.openzen.zenscript.codemodel.expression.SameObjectExpression;
import org.openzen.zenscript.codemodel.expression.SetFieldExpression;
import org.openzen.zenscript.codemodel.expression.SetFunctionParameterExpression;
import org.openzen.zenscript.codemodel.expression.SetLocalVariableExpression;
import org.openzen.zenscript.codemodel.expression.SetStaticFieldExpression;
import org.openzen.zenscript.codemodel.expression.SetterExpression;
import org.openzen.zenscript.codemodel.expression.StaticGetterExpression;
import org.openzen.zenscript.codemodel.expression.StaticSetterExpression;
import org.openzen.zenscript.codemodel.expression.SupertypeCastExpression;
import org.openzen.zenscript.codemodel.expression.ThisExpression;
import org.openzen.zenscript.codemodel.expression.ThrowExpression;
import org.openzen.zenscript.codemodel.expression.TryConvertExpression;
import org.openzen.zenscript.codemodel.expression.TryRethrowAsExceptionExpression;
import org.openzen.zenscript.codemodel.expression.TryRethrowAsResultExpression;
import org.openzen.zenscript.codemodel.expression.VariantValueExpression;
import org.openzen.zenscript.codemodel.expression.WrapOptionalExpression;

/**
 *
 * @author Hoofdgebruiker
 */
public class ExpressionHoistingChecker implements ExpressionVisitor<Boolean> {
	public static final ExpressionHoistingChecker INSTANCE = new ExpressionHoistingChecker();
	
	private ExpressionHoistingChecker() {}

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
	public Boolean visitCapturedDirect(CapturedDirectExpression expression) {
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
	public Boolean visitCast(CastExpression expression) {
		return true;
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
	public Boolean visitConst(ConstExpression expression) {
		return false;
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
	public Boolean visitGetter(GetterExpression expression) {
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
	public Boolean visitNew(NewExpression expression) {
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
	public Boolean visitPostCall(PostCallExpression expression) {
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
	public Boolean visitSetter(SetterExpression expression) {
		return true;
	}

	@Override
	public Boolean visitStaticGetter(StaticGetterExpression expression) {
		return false;
	}

	@Override
	public Boolean visitStaticSetter(StaticSetterExpression expression) {
		return true;
	}

	@Override
	public Boolean visitSupertypeCast(SupertypeCastExpression expression) {
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
