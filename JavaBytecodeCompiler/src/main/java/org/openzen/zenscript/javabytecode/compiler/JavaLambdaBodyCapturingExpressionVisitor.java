package org.openzen.zenscript.javabytecode.compiler;

import org.objectweb.asm.Label;
import org.openzen.zenscript.codemodel.expression.*;
import org.openzen.zenscript.javabytecode.JavaBytecodeContext;
import org.openzen.zenscript.javabytecode.JavaLocalVariableInfo;
import org.openzen.zenscript.javabytecode.JavaMangler;
import org.openzen.zenscript.javashared.JavaCompiledModule;

public class JavaLambdaBodyCapturingExpressionVisitor extends JavaExpressionVisitor {
	private final String lambdaClassName;
	private final FunctionExpression functionExpression;
	private final JavaMangler javaMangler;

	public JavaLambdaBodyCapturingExpressionVisitor(
			final JavaBytecodeContext context,
			final JavaCompiledModule module,
			final JavaWriter javaWriter,
			final JavaMangler javaMangler,
			final String lambdaClassName,
			final FunctionExpression functionExpression
	) {
		super(context, module, javaWriter, javaMangler);
		this.lambdaClassName = lambdaClassName;
		this.functionExpression = functionExpression;
		this.javaMangler = javaMangler;
	}

	@Override
	public Void visitGetLocalVariable(GetLocalVariableExpression varExpression) {
		final JavaLocalVariableInfo localVariable = javaWriter.tryGetLocalVariable(varExpression.variable.id);
		if (localVariable != null) {
			final Label label = new Label();
			localVariable.end = label;
			javaWriter.label(label);
			javaWriter.load(localVariable);
			return null;
		}

		final int position = calculateMemberPosition(varExpression, this.functionExpression);
		javaWriter.loadObject(0);
		javaWriter.getField(lambdaClassName, this.javaMangler.mangleCapturedParameter(position, false), context.getDescriptor(varExpression.variable.type));
		return null;
	}

	@Override
	public Void visitCapturedParameter(CapturedParameterExpression varExpression) {
		final int position = calculateMemberPosition(varExpression, this.functionExpression);
		javaWriter.loadObject(0);
		javaWriter.getField(lambdaClassName, this.javaMangler.mangleCapturedParameter(position, false), context.getDescriptor(varExpression.parameter.type));
		return null;
	}

	@Override
	public Void visitCapturedThis(CapturedThisExpression expression) {
		javaWriter.loadObject(0);
		javaWriter.getField(lambdaClassName, this.javaMangler.mangleCapturedParameter(1, true), context.getDescriptor(expression.type));
		return null;
	}

	//TODO replace with visitor?
	private static int calculateMemberPosition(GetLocalVariableExpression localVariableExpression, FunctionExpression expression) {
		int h = 1;//expression.header.parameters.length;
		for (CapturedExpression capture : expression.closure.captures) {
			if (capture instanceof CapturedLocalVariableExpression && ((CapturedLocalVariableExpression) capture).variable == localVariableExpression.variable)
				return h;
			if (capture instanceof CapturedClosureExpression && ((CapturedClosureExpression) capture).value instanceof CapturedLocalVariableExpression && ((CapturedLocalVariableExpression) ((CapturedClosureExpression) capture).value).variable == localVariableExpression.variable)
				return h;
			h++;
		}
		throw new RuntimeException(localVariableExpression.position.toString() + ": Captured Statement error");
	}

	private static int calculateMemberPosition(CapturedParameterExpression functionParameterExpression, FunctionExpression expression) {
		int h = 1;//expression.header.parameters.length;
		for (CapturedExpression capture : expression.closure.captures) {
			if (capture instanceof CapturedParameterExpression && ((CapturedParameterExpression) capture).parameter == functionParameterExpression.parameter)
				return h;
			h++;
		}
		throw new RuntimeException(functionParameterExpression.position.toString() + ": Captured Statement error");
	}
}
