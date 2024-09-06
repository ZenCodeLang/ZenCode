/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javabytecode.compiler;

import org.objectweb.asm.Label;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.expression.*;
import org.openzen.zenscript.codemodel.identifiers.MethodSymbol;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.builtin.BuiltinMethodSymbol;
import org.openzen.zenscript.javabytecode.JavaBytecodeContext;
import org.openzen.zenscript.javabytecode.JavaLocalVariableInfo;
import org.openzen.zenscript.javabytecode.JavaMangler;
import org.openzen.zenscript.javabytecode.compiler.JavaModificationExpressionVisitor.PushOption;
import org.openzen.zenscript.javashared.JavaCompiledModule;
import org.openzen.zenscript.javashared.JavaParameterInfo;
import org.openzen.zenscript.javashared.expressions.JavaFunctionInterfaceCastExpression;

import java.util.Optional;

/**
 * @author Hoofdgebruiker
 */
public class JavaNonPushingExpressionVisitor implements ExpressionVisitor<Void> {
	private final JavaBytecodeContext context;
	private final JavaCompiledModule module;
	private final JavaWriter javaWriter;
	private final JavaExpressionVisitor original;
	private final JavaFieldBytecodeCompiler fieldCompiler;
	private final JavaMangler mangler;

	public JavaNonPushingExpressionVisitor(JavaBytecodeContext context, JavaCompiledModule module, JavaWriter javaWriter, JavaMangler mangler, JavaExpressionVisitor original) {
		this.context = context;
		this.module = module;
		this.javaWriter = javaWriter;
		this.original = original;
		this.mangler = mangler;
		fieldCompiler = new JavaFieldBytecodeCompiler(javaWriter, original, false);
	}

	@Override
	public Void visitAndAnd(AndAndExpression expression) {
		Label end = new Label();
		expression.left.accept(original);
		javaWriter.ifEQ(end);
		expression.right.accept(this);
		javaWriter.label(end);
		return null;
	}

	private void fallback(Expression expression) {
		expression.accept(original);
		if (expression.type != BasicTypeID.VOID)
			javaWriter.pop(CompilerUtils.isLarge(expression.type));
	}

	@Override
	public Void visitArray(ArrayExpression expression) {
		for (Expression value : expression.expressions)
			value.accept(this);

		return null;
	}

	@Override
	public Void visitCompare(CompareExpression expression) {
		expression.left.accept(this);
		expression.right.accept(this);
		return null;
	}

	@Override
	public Void visitCall(CallExpression expression) {
		fallback(expression);
		return null;
	}

	@Override
	public Void visitCallStatic(CallStaticExpression expression) {
		fallback(expression);
		return null;
	}

	@Override
	public Void visitCallSuper(CallSuperExpression expression) {
		fallback(expression);
		return null;
	}

	@Override
	public Void visitCapturedClosure(CapturedClosureExpression expression) {
		return expression.value.accept(this);
	}

	@Override
	public Void visitCapturedLocalVariable(CapturedLocalVariableExpression expression) {
		return null;
	}

	@Override
	public Void visitCapturedParameter(CapturedParameterExpression expression) {
		return null;
	}

	@Override
	public Void visitCapturedThis(CapturedThisExpression expression) {
		return null;
	}

	@Override
	public Void visitCheckNull(CheckNullExpression expression) {
		return expression.value.accept(this);
	}

	@Override
	public Void visitCoalesce(CoalesceExpression expression) {
		final Label end = new Label();
		expression.left.accept(original);
		javaWriter.ifNonNull(end);
		expression.right.accept(this);
		javaWriter.label(end);
		return null;
	}

	@Override
	public Void visitConditional(ConditionalExpression expression) {
		final Label end = new Label();
		final Label onElse = new Label();
		expression.condition.accept(original);
		javaWriter.ifEQ(onElse);
		expression.ifThen.accept(this);
		javaWriter.goTo(end);
		javaWriter.label(onElse);
		expression.ifElse.accept(this);
		javaWriter.label(end);
		return null;
	}

	@Override
	public Void visitConstantBool(ConstantBoolExpression expression) {
		return null;
	}

	@Override
	public Void visitConstantByte(ConstantByteExpression expression) {
		return null;
	}

	@Override
	public Void visitConstantChar(ConstantCharExpression expression) {
		return null;
	}

	@Override
	public Void visitConstantDouble(ConstantDoubleExpression expression) {
		return null;
	}

	@Override
	public Void visitConstantFloat(ConstantFloatExpression expression) {
		return null;
	}

	@Override
	public Void visitConstantInt(ConstantIntExpression expression) {
		return null;
	}

	@Override
	public Void visitConstantLong(ConstantLongExpression expression) {
		return null;
	}

	@Override
	public Void visitConstantSByte(ConstantSByteExpression expression) {
		return null;
	}

	@Override
	public Void visitConstantShort(ConstantShortExpression expression) {
		return null;
	}

	@Override
	public Void visitConstantString(ConstantStringExpression expression) {
		return null;
	}

	@Override
	public Void visitConstantUInt(ConstantUIntExpression expression) {
		return null;
	}

	@Override
	public Void visitConstantULong(ConstantULongExpression expression) {
		return null;
	}

	@Override
	public Void visitConstantUShort(ConstantUShortExpression expression) {
		return null;
	}

	@Override
	public Void visitConstantUSize(ConstantUSizeExpression expression) {
		return null;
	}

	@Override
	public Void visitConstructorThisCall(ConstructorThisCallExpression expression) {
		javaWriter.loadObject(0);
		if (javaWriter.forDefinition.isEnum()) {
			javaWriter.loadObject(1);
			javaWriter.loadInt(2);
		}

		for (Expression argument : expression.arguments.arguments) {
			argument.accept(original);
		}
		String internalName = context.getInternalName(expression.objectType);
		javaWriter.invokeSpecial(internalName, "<init>", javaWriter.forDefinition.isEnum()
				? context.getEnumConstructorDescriptor(expression.constructor.getHeader())
				: context.getMethodDescriptor(expression.constructor.getHeader().withReturnType(BasicTypeID.VOID)));
		return null;
	}

	@Override
	public Void visitConstructorSuperCall(ConstructorSuperCallExpression expression) {
		javaWriter.loadObject(0);
		for (Expression argument : expression.arguments.arguments) {
			argument.accept(original);
		}
		//No super calls in enums possible, and that's already handled in the enum constructor itself.
		javaWriter.invokeSpecial(
				context.getInternalName(expression.constructor.getTarget()),
				"<init>",
				context.getMethodDescriptor(expression.constructor.getHeader().withReturnType(BasicTypeID.VOID)));

		CompilerUtils.writeDefaultFieldInitializers(context, javaWriter, javaWriter.forDefinition, mangler, false);
		return null;
	}

	@Override
	public Void visitEnumConstant(EnumConstantExpression expression) {
		return null;
	}

	@Override
	public Void visitFunction(FunctionExpression expression) {
		return null;
	}

	@Override
	public Void visitGetField(GetFieldExpression expression) {
		return expression.target.accept(this);
	}

	@Override
	public Void visitGetFunctionParameter(GetFunctionParameterExpression expression) {
		return null;
	}

	@Override
	public Void visitGetLocalVariable(GetLocalVariableExpression expression) {
		return null;
	}

	@Override
	public Void visitGetMatchingVariantField(GetMatchingVariantField expression) {
		fallback(expression);
		return null;
	}

	@Override
	public Void visitGetStaticField(GetStaticFieldExpression expression) {
		return null;
	}

	@Override
	public Void visitGlobal(GlobalExpression expression) {
		return null;
	}

	@Override
	public Void visitGlobalCall(GlobalCallExpression expression) {
		fallback(expression);
		return null;
	}

	@Override
	public Void visitInterfaceCast(InterfaceCastExpression expression) {
		return expression.value.accept(this);
	}

	@Override
	public Void visitIs(IsExpression expression) {
		return expression.value.accept(this);
	}

	@Override
	public Void visitMakeConst(MakeConstExpression expression) {
		return expression.value.accept(this);
	}

	@Override
	public Void visitMap(MapExpression expression) {
		fallback(expression);
		return null;
	}

	@Override
	public Void visitMatch(MatchExpression expression) {
		fallback(expression);
		return null;
	}

	@Override
	public Void visitNull(NullExpression expression) {
		return null;
	}

	@Override
	public Void visitOrOr(OrOrExpression expression) {
		Label end = new Label();
		expression.left.accept(original);
		javaWriter.ifNE(end);
		expression.right.accept(this);
		javaWriter.label(end);
		return null;
	}

	@Override
	public Void visitPanic(PanicExpression expression) {
		javaWriter.newObject("java/lang/AssertionError");
		javaWriter.dup();
		expression.value.accept(original);
		javaWriter.invokeSpecial(AssertionError.class, "<init>", "(Ljava/lang/Object;)V");
		javaWriter.aThrow();
		return null;
	}

	@Override
	public Void visitPlatformSpecific(Expression expression) {
		if (expression instanceof JavaFunctionInterfaceCastExpression) {
			((JavaFunctionInterfaceCastExpression) expression).value.accept(this);
		} else {
			throw new AssertionError("Unrecognized platform specific expression: " + expression);
		}
		return null;
	}

	@Override
	public Void visitModification(ModificationExpression expression) {
		original.modify(expression, PushOption.NONE);
		return null;
	}

	@Override
	public Void visitRange(RangeExpression expression) {
		expression.from.accept(this);
		expression.to.accept(this);
		return null;
	}

	@Override
	public Void visitSameObject(SameObjectExpression expression) {
		expression.left.accept(this);
		expression.right.accept(this);
		return null;
	}

	@Override
	public Void visitSetField(SetFieldExpression expression) {
		context.getJavaField(expression.field).compileInstanceSet(fieldCompiler, expression.target, expression.value);
		return null;
	}

	@Override
	public Void visitSetFunctionParameter(SetFunctionParameterExpression expression) {
		expression.value.accept(original);
		JavaParameterInfo parameter = module.getParameterInfo(expression.parameter);
		javaWriter.store(context.getType(expression.type), parameter.index);
		return null;
	}

	@Override
	public Void visitSetLocalVariable(SetLocalVariableExpression expression) {
		expression.value.accept(original);
		Label label = new Label();
		javaWriter.label(label);
		final JavaLocalVariableInfo tag = javaWriter.getLocalVariable(expression.variable.id);
		tag.end = label;

		javaWriter.store(tag.type, tag.local);
		return null;
	}

	@Override
	public Void visitSetStaticField(SetStaticFieldExpression expression) {
		context.getJavaField(expression.field).compileStaticSet(fieldCompiler, expression.value);
		return null;
	}

	@Override
	public Void visitSupertypeCast(SupertypeCastExpression expression) {
		return expression.value.accept(this);
	}

	@Override
	public Void visitSubtypeCast(SubtypeCastExpression expression) {
		return expression.value.accept(this);
	}

	@Override
	public Void visitThis(ThisExpression expression) {
		return null;
	}

	@Override
	public Void visitThrow(ThrowExpression expression) {
		expression.value.accept(this);
		javaWriter.aThrow();
		return null;
	}

	@Override
	public Void visitTryConvert(TryConvertExpression expression) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public Void visitTryRethrowAsException(TryRethrowAsExceptionExpression expression) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public Void visitTryRethrowAsResult(TryRethrowAsResultExpression expression) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public Void visitVariantValue(VariantValueExpression expression) {
		fallback(expression);
		return null;
	}

	@Override
	public Void visitWrapOptional(WrapOptionalExpression expression) {
		return expression.value.accept(this);
	}

	@Override
	public Void visitMemoized(MemoizedExpression expression) {
		fallback(expression);
		return null;
	}
}
