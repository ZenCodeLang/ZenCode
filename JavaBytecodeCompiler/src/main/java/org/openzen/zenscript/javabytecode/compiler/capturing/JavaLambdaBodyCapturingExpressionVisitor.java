package org.openzen.zenscript.javabytecode.compiler.capturing;

import org.openzen.zenscript.codemodel.expression.*;
import org.openzen.zenscript.codemodel.expression.captured.*;
import org.openzen.zenscript.javabytecode.JavaBytecodeContext;
import org.openzen.zenscript.javabytecode.JavaMangler;
import org.openzen.zenscript.javabytecode.compiler.JavaWriter;
import org.openzen.zenscript.javashared.JavaCompiledModule;

import java.util.stream.Stream;

public class JavaLambdaBodyCapturingExpressionVisitor implements CapturedExpressionVisitor<Void> {
	private final String lambdaClassName;
	private final FunctionExpression functionExpression;
	private final JavaMangler javaMangler;
	private final JavaBytecodeContext context;
	private final JavaCompiledModule module;
	private final JavaWriter javaWriter;

	public JavaLambdaBodyCapturingExpressionVisitor(
			final JavaBytecodeContext context,
			final JavaCompiledModule module,
			final JavaWriter javaWriter,
			final JavaMangler javaMangler,
			final String lambdaClassName,
			final FunctionExpression functionExpression
	) {
		this.context = context;
		this.module = module;
		this.javaWriter = javaWriter;
		this.lambdaClassName = lambdaClassName;
		this.functionExpression = functionExpression;
		this.javaMangler = javaMangler;
	}


	@Override
	public Void visitCapturedParameter(CapturedParameterExpression varExpression) {
		final int position = calculateMemberPosition(varExpression, this.functionExpression);
		javaWriter.load(context.getType(varExpression.type), position);
		return null;
	}

	@Override
	public Void visitCapturedLocal(CapturedLocalVariableExpression expression) {
		return null;
	}

	@Override
	public Void visitRecaptured(CapturedClosureExpression expression) {
		return null;
	}

	@Override
	public Void visitCapturedThis(CapturedThisExpression expression) {
		javaWriter.loadObject(0);
		javaWriter.getField(lambdaClassName, this.javaMangler.mangleCapturedParameter(1, true), context.getDescriptor(expression.type));
		return null;
	}

	private static int calculateMemberPosition(GetLocalVariableExpression localVariableExpression, FunctionExpression expression) {
		int h = 1;
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
		int h = 1;

		Iterable<? extends CapturedExpression> captures = Stream.concat(
				expression.closure.captures.stream(),
				functionParameterExpression.closure.captures.stream()
		)::iterator;

		for (CapturedExpression capture : captures) {
			if (capture instanceof CapturedParameterExpression && ((CapturedParameterExpression) capture).parameter == functionParameterExpression.parameter)
				return h;
			h++;
		}
		throw new RuntimeException(functionParameterExpression.position.toString() + ": Captured Statement error");
	}
}
