package org.openzen.zenscript.javabytecode.compiler.capturing;

import org.openzen.zenscript.codemodel.expression.FunctionExpression;
import org.openzen.zenscript.codemodel.expression.GetLocalVariableExpression;
import org.openzen.zenscript.codemodel.expression.captured.*;
import org.openzen.zenscript.javabytecode.JavaBytecodeContext;
import org.openzen.zenscript.javabytecode.JavaMangler;
import org.openzen.zenscript.javabytecode.compiler.JavaWriter;

import java.util.stream.Stream;

public class JavaCapturedExpressionVisitorToAccessCapturesInsideTheLambda implements CapturedExpressionVisitor<Void> {

	private final String lambdaClassName;
	private final FunctionExpression functionExpression;
	private final JavaMangler javaMangler;
	private final JavaBytecodeContext context;
	private final JavaWriter javaWriter;

	public JavaCapturedExpressionVisitorToAccessCapturesInsideTheLambda(
			final JavaBytecodeContext context,
			final JavaWriter javaWriter,
			final JavaMangler javaMangler,
			final String lambdaClassName,
			final FunctionExpression functionExpression
	) {
		this.context = context;
		this.javaWriter = javaWriter;
		this.lambdaClassName = lambdaClassName;
		this.functionExpression = functionExpression;
		this.javaMangler = javaMangler;
	}
	@Override
	public Void visitCapturedThis(CapturedThisExpression expression) {
		javaWriter.loadObject(0);
		javaWriter.getField(lambdaClassName, this.javaMangler.mangleCapturedParameter(1, true), context.getDescriptor(expression.type));
		return null;
	}

	@Override
	public Void visitCapturedParameter(CapturedParameterExpression expression) {
		final int position = calculateMemberPosition(expression, this.functionExpression);
		javaWriter.loadObject(0);
		javaWriter.getField(lambdaClassName, this.javaMangler.mangleCapturedParameter(position, false), context.getDescriptor(expression.parameter.type));
		return null;
	}

	@Override
	public Void visitCapturedLocal(CapturedLocalVariableExpression expression) {
		final int position = calculateMemberPosition(new GetLocalVariableExpression(expression.position, expression.variable), this.functionExpression);

		javaWriter.loadObject(0);
		javaWriter.getField(lambdaClassName, this.javaMangler.mangleCapturedParameter(position, false), context.getDescriptor(expression.type));
		return null;
	}

	@Override
	public Void visitRecaptured(CapturedClosureExpression expression) {
		throw new UnsupportedOperationException("TODO");
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
