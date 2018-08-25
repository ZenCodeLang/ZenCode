/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javabytecode.compiler;

import org.objectweb.asm.Type;
import org.openzen.zenscript.codemodel.expression.*;
import org.openzen.zenscript.javabytecode.JavaBytecodeContext;
import org.openzen.zenscript.javabytecode.JavaLocalVariableInfo;
import org.openzen.zenscript.javashared.JavaParameterInfo;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaPreDecrementVisitor implements ExpressionVisitor<Void> {
	private final JavaExpressionVisitor expressionCompiler;
	private final JavaWriter javaWriter;
	private final JavaBytecodeContext context;
	
	public JavaPreDecrementVisitor(JavaBytecodeContext context, JavaExpressionVisitor expressionCompiler) {
		this.expressionCompiler = expressionCompiler;
		this.context = context;
		javaWriter = expressionCompiler.getJavaWriter();
	}

	@Override
	public Void visitAndAnd(AndAndExpression expression) {
		throw new UnsupportedOperationException("Invalid increment target");
	}

	@Override
	public Void visitArray(ArrayExpression expression) {
		throw new UnsupportedOperationException("Invalid increment target");
	}

	@Override
	public Void visitCompare(CompareExpression expression) {
		throw new UnsupportedOperationException("Invalid increment target");
	}

	@Override
	public Void visitCall(CallExpression expression) {
		throw new UnsupportedOperationException("Invalid increment target");
	}

	@Override
	public Void visitCallStatic(CallStaticExpression expression) {
		throw new UnsupportedOperationException("Invalid increment target");
	}

	@Override
	public Void visitCapturedClosure(CapturedClosureExpression expression) {
		throw new UnsupportedOperationException("Invalid increment target");
	}

	@Override
	public Void visitCapturedDirect(CapturedDirectExpression expression) {
		throw new UnsupportedOperationException("Invalid increment target");
	}

	@Override
	public Void visitCapturedLocalVariable(CapturedLocalVariableExpression expression) {
		throw new UnsupportedOperationException("Invalid increment target");
	}

	@Override
	public Void visitCapturedParameter(CapturedParameterExpression expression) {
		throw new UnsupportedOperationException("Invalid increment target");
	}

	@Override
	public Void visitCapturedThis(CapturedThisExpression expression) {
		throw new UnsupportedOperationException("Invalid increment target");
	}

	@Override
	public Void visitCast(CastExpression expression) {
		throw new UnsupportedOperationException("Invalid increment target");
	}

	@Override
	public Void visitCheckNull(CheckNullExpression expression) {
		throw new UnsupportedOperationException("Invalid increment target");
	}

	@Override
	public Void visitCoalesce(CoalesceExpression expression) {
		throw new UnsupportedOperationException("Invalid increment target");
	}

	@Override
	public Void visitConditional(ConditionalExpression expression) {
		throw new UnsupportedOperationException("Invalid increment target");
	}
	
	@Override
	public Void visitConst(ConstExpression expression) {
		throw new UnsupportedOperationException("Invalid increment target");
	}

	@Override
	public Void visitConstantBool(ConstantBoolExpression expression) {
		throw new UnsupportedOperationException("Invalid increment target");
	}

	@Override
	public Void visitConstantByte(ConstantByteExpression expression) {
		throw new UnsupportedOperationException("Invalid increment target");
	}

	@Override
	public Void visitConstantChar(ConstantCharExpression expression) {
		throw new UnsupportedOperationException("Invalid increment target");
	}

	@Override
	public Void visitConstantDouble(ConstantDoubleExpression expression) {
		throw new UnsupportedOperationException("Invalid increment target");
	}

	@Override
	public Void visitConstantFloat(ConstantFloatExpression expression) {
		throw new UnsupportedOperationException("Invalid increment target");
	}

	@Override
	public Void visitConstantInt(ConstantIntExpression expression) {
		throw new UnsupportedOperationException("Invalid increment target");
	}

	@Override
	public Void visitConstantLong(ConstantLongExpression expression) {
		throw new UnsupportedOperationException("Invalid increment target");
	}

	@Override
	public Void visitConstantSByte(ConstantSByteExpression expression) {
		throw new UnsupportedOperationException("Invalid increment target");
	}

	@Override
	public Void visitConstantShort(ConstantShortExpression expression) {
		throw new UnsupportedOperationException("Invalid increment target");
	}

	@Override
	public Void visitConstantString(ConstantStringExpression expression) {
		throw new UnsupportedOperationException("Invalid increment target");
	}

	@Override
	public Void visitConstantUInt(ConstantUIntExpression expression) {
		throw new UnsupportedOperationException("Invalid increment target");
	}

	@Override
	public Void visitConstantULong(ConstantULongExpression expression) {
		throw new UnsupportedOperationException("Invalid increment target");
	}

	@Override
	public Void visitConstantUShort(ConstantUShortExpression expression) {
		throw new UnsupportedOperationException("Invalid increment target");
	}

	@Override
	public Void visitConstantUSize(ConstantUSizeExpression expression) {
		throw new UnsupportedOperationException("Invalid increment target");
	}

	@Override
	public Void visitConstructorThisCall(ConstructorThisCallExpression expression) {
		throw new UnsupportedOperationException("Invalid increment target");
	}

	@Override
	public Void visitConstructorSuperCall(ConstructorSuperCallExpression expression) {
		throw new UnsupportedOperationException("Invalid increment target");
	}

	@Override
	public Void visitEnumConstant(EnumConstantExpression expression) {
		throw new UnsupportedOperationException("Invalid increment target");
	}

	@Override
	public Void visitFunction(FunctionExpression expression) {
		throw new UnsupportedOperationException("Invalid increment target");
	}

	@Override
	public Void visitGetField(GetFieldExpression expression) {
		Type objectType = context.getType(expression.target.type);
		
		int local = javaWriter.local(objectType);
		expression.target.accept(expressionCompiler);
		javaWriter.dup();
		javaWriter.store(objectType, local);
		
        if (!expressionCompiler.checkAndGetFieldInfo(expression.field, false))
            throw new IllegalStateException("Missing field info on a field member!");
		
		javaWriter.iConst1();
		javaWriter.iSub();
		
		javaWriter.load(objectType, local);
        if (!expressionCompiler.checkAndPutFieldInfo(expression.field, false))
            throw new IllegalStateException("Missing field info on a field member!");
		
		return null;
	}

	@Override
	public Void visitGetFunctionParameter(GetFunctionParameterExpression expression) {
		javaWriter.idec(expression.parameter.getTag(JavaParameterInfo.class).index);
		return null;
	}
	
	@Override
	public Void visitGetLocalVariable(GetLocalVariableExpression expression) {
		javaWriter.idec(expression.variable.getTag(JavaLocalVariableInfo.class).local);
		return null;
	}

	@Override
	public Void visitGetMatchingVariantField(GetMatchingVariantField expression) {
		throw new UnsupportedOperationException("Invalid increment target");
	}

	@Override
	public Void visitGetStaticField(GetStaticFieldExpression expression) {
		if (!expressionCompiler.checkAndGetFieldInfo(expression.field, false))
            throw new IllegalStateException("Missing field info on a field member!");
		
		javaWriter.iConst1();
		javaWriter.iAdd();
		
		if (!expressionCompiler.checkAndPutFieldInfo(expression.field, false))
            throw new IllegalStateException("Missing field info on a field member!");
		
		return null;
	}

	@Override
	public Void visitGetter(GetterExpression expression) {
		throw new UnsupportedOperationException("Not yet supported");
	}

	@Override
	public Void visitGlobal(GlobalExpression expression) {
		throw new UnsupportedOperationException("Invalid increment target");
	}

	@Override
	public Void visitGlobalCall(GlobalCallExpression expression) {
		throw new UnsupportedOperationException("Invalid increment target");
	}

	@Override
	public Void visitInterfaceCast(InterfaceCastExpression expression) {
		throw new UnsupportedOperationException("Invalid increment target");
	}

	@Override
	public Void visitIs(IsExpression expression) {
		throw new UnsupportedOperationException("Invalid increment target");
	}

	@Override
	public Void visitMakeConst(MakeConstExpression expression) {
		throw new UnsupportedOperationException("Invalid increment target");
	}

	@Override
	public Void visitMap(MapExpression expression) {
		throw new UnsupportedOperationException("Invalid increment target");
	}

	@Override
	public Void visitMatch(MatchExpression expression) {
		throw new UnsupportedOperationException("Invalid increment target");
	}

	@Override
	public Void visitNew(NewExpression expression) {
		throw new UnsupportedOperationException("Invalid increment target");
	}

	@Override
	public Void visitNull(NullExpression expression) {
		throw new UnsupportedOperationException("Invalid increment target");
	}

	@Override
	public Void visitOrOr(OrOrExpression expression) {
		throw new UnsupportedOperationException("Invalid increment target");
	}
	
	@Override
	public Void visitPanic(PanicExpression expression) {
		throw new UnsupportedOperationException("Invalid increment target");
	}

	@Override
	public Void visitPostCall(PostCallExpression expression) {
		throw new UnsupportedOperationException("Invalid increment target");
	}

	@Override
	public Void visitRange(RangeExpression expression) {
		throw new UnsupportedOperationException("Invalid increment target");
	}
	
	@Override
	public Void visitSameObject(SameObjectExpression expression) {
		throw new UnsupportedOperationException("Invalid increment target");
	}

	@Override
	public Void visitSetField(SetFieldExpression expression) {
		throw new UnsupportedOperationException("Invalid increment target");
	}

	@Override
	public Void visitSetFunctionParameter(SetFunctionParameterExpression expression) {
		throw new UnsupportedOperationException("Invalid increment target");
	}

	@Override
	public Void visitSetLocalVariable(SetLocalVariableExpression expression) {
		throw new UnsupportedOperationException("Invalid increment target");
	}

	@Override
	public Void visitSetStaticField(SetStaticFieldExpression expression) {
		throw new UnsupportedOperationException("Invalid increment target");
	}

	@Override
	public Void visitSetter(SetterExpression expression) {
		throw new UnsupportedOperationException("Invalid increment target");
	}

	@Override
	public Void visitStaticGetter(StaticGetterExpression expression) {
		throw new UnsupportedOperationException("Invalid increment target");
	}

	@Override
	public Void visitStaticSetter(StaticSetterExpression expression) {
		throw new UnsupportedOperationException("Invalid increment target");
	}
	
	@Override
	public Void visitSupertypeCast(SupertypeCastExpression expression) {
		throw new UnsupportedOperationException("Invalid increment target");
	}

	@Override
	public Void visitThis(ThisExpression expression) {
		throw new UnsupportedOperationException("Invalid increment target");
	}

	@Override
	public Void visitThrow(ThrowExpression expression) {
		throw new UnsupportedOperationException("Invalid increment target");
	}

	@Override
	public Void visitTryConvert(TryConvertExpression expression) {
		throw new UnsupportedOperationException("Invalid increment target");
	}

	@Override
	public Void visitTryRethrowAsException(TryRethrowAsExceptionExpression expression) {
		throw new UnsupportedOperationException("Invalid increment target");
	}

	@Override
	public Void visitTryRethrowAsResult(TryRethrowAsResultExpression expression) {
		throw new UnsupportedOperationException("Invalid increment target");
	}

	@Override
	public Void visitVariantValue(VariantValueExpression expression) {
		throw new UnsupportedOperationException("Invalid increment target");
	}

	@Override
	public Void visitWrapOptional(WrapOptionalExpression expression) {
		throw new UnsupportedOperationException("Invalid increment target");
	}
}
