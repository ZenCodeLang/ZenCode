package org.openzen.zenscript.javabytecode.compiler.lambda.capturing;

import org.openzen.zenscript.codemodel.expression.FunctionExpression;
import org.openzen.zenscript.codemodel.expression.captured.*;
import org.openzen.zenscript.javabytecode.JavaBytecodeContext;
import org.openzen.zenscript.javabytecode.compiler.JavaWriter;
import org.openzen.zenscript.javabytecode.compiler.lambda.LambdaClosureInfo;

public final class JavaRedirectCapturesCapturedExpressionVisitor implements CapturedExpressionVisitor<Void> {
	private final JavaWriter javaWriter;
	private final JavaCaptureDataFinderCapturedExpressionVisitor captureDataFinder;

	public JavaRedirectCapturesCapturedExpressionVisitor(
			final JavaWriter javaWriter,
			final FunctionExpression functionExpression,
			final LambdaClosureInfo closureInfo,
			final JavaBytecodeContext context
	) {
		this.javaWriter = javaWriter;
		this.captureDataFinder = new JavaCaptureDataFinderCapturedExpressionVisitor(functionExpression, closureInfo, context);
	}

	@Override
	public Void visitCapturedThis(final CapturedThisExpression expression) {
		return this.loadByCaptureData(expression);
	}

	@Override
	public Void visitCapturedParameter(CapturedParameterExpression expression) {
		return this.loadByCaptureData(expression);
	}

	@Override
	public Void visitCapturedLocal(CapturedLocalVariableExpression expression) {
		return this.loadByCaptureData(expression);
	}

	@Override
	public Void visitRecaptured(CapturedClosureExpression expression) {
		return this.loadByCaptureData(expression);
	}

	private Void loadByCaptureData(final CapturedExpression expression) {
		return this.loadByCaptureData(expression.accept(this.captureDataFinder));
	}

	private Void loadByCaptureData(final LambdaCaptureData data) {
		this.javaWriter.load(data.type(), data.position());
		return null;
	}
}
