package org.openzen.zenscript.javabytecode.compiler.lambda.capturing;

import org.objectweb.asm.Type;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.expression.FunctionExpression;
import org.openzen.zenscript.codemodel.expression.captured.*;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.javabytecode.JavaBytecodeContext;
import org.openzen.zenscript.javabytecode.compiler.lambda.LambdaClosureInfo;

import java.util.function.Predicate;
import java.util.function.Supplier;

public final class JavaCaptureDataFinderCapturedExpressionVisitor implements CapturedExpressionVisitor<LambdaCaptureData> {
	private static final class ExtractedInfo {
		private final boolean matches;
		private final String name;

		private ExtractedInfo(final boolean matches, final String name) {
			this.matches = matches;
			this.name = name;
		}

		static ExtractedInfo noMatch() {
			return new ExtractedInfo(false, null);
		}

		static ExtractedInfo match(final String name) {
			return new ExtractedInfo(true, name);
		}

		boolean matches() {
			return this.matches;
		}

		String name() {
			return this.name;
		}
	}

	@FunctionalInterface
	private interface InfoExtractor<T extends CapturedExpression> {
		static <T extends CapturedExpression> InfoExtractor<T> byPredicating(final Supplier<String> name, final Predicate<T> predicate) {
			return it -> predicate.test(it)? ExtractedInfo.match(name.get()) : ExtractedInfo.noMatch();
		}

		ExtractedInfo extractInfo(final T t);
	}

	private final FunctionExpression functionExpression;
	private final LambdaClosureInfo closureInfo;
	private final JavaBytecodeContext context;

	public JavaCaptureDataFinderCapturedExpressionVisitor(final FunctionExpression functionExpression, final LambdaClosureInfo closureInfo, final JavaBytecodeContext context) {
		this.functionExpression = functionExpression;
		this.closureInfo = closureInfo;
		this.context = context;
	}

	@Override
	public LambdaCaptureData visitCapturedThis(final CapturedThisExpression expression) {
		// TODO("Remove null-check as this method should never return null")
		final Type type = this.closureInfo.thisType() == null? Type.getType(Void.class) : this.getTypeFrom(this.closureInfo.thisType());
		return LambdaCaptureData.of(0, type, "$this");
	}

	@Override
	public LambdaCaptureData visitCapturedParameter(final CapturedParameterExpression expression) {
		return this.computeData(
				this.functionExpression,
				expression.parameter.type,
				InfoExtractor.byPredicating(
						() -> expression.parameter.name,
						capture -> capture instanceof CapturedParameterExpression && ((CapturedParameterExpression) capture).parameter == expression.parameter
				)
		);
	}

	@Override
	public LambdaCaptureData visitCapturedLocal(final CapturedLocalVariableExpression expression) {
		final Predicate<CapturedExpression> matchVariable =
				capture -> capture instanceof CapturedLocalVariableExpression && ((CapturedLocalVariableExpression) capture).variable == expression.variable;

		return this.computeData(
				this.functionExpression,
				expression.variable.type,
				InfoExtractor.byPredicating(
						() -> expression.variable.name,
						matchVariable.or(capture -> capture instanceof CapturedClosureExpression && matchVariable.test(((CapturedClosureExpression) capture).value))
				)
		);
	}

	@Override
	public LambdaCaptureData visitRecaptured(final CapturedClosureExpression expression) {
		return this.computeData(
				this.functionExpression,
				expression.type,
				InfoExtractor.byPredicating(
						() -> expression.value.accept(this).name(),
						expression::equals
				)
		);
	}

	private LambdaCaptureData computeData(
			final FunctionExpression expression,
			final TypeID varType,
			final InfoExtractor<CapturedExpression> extractor
	) {
		final Type type = this.getTypeFrom(varType);
		int h = this.findFirstValidCaptureIndex(expression);
		for (final CapturedExpression capture : expression.closure.captures) {
			final ExtractedInfo info = extractor.extractInfo(capture);
			if (info.matches()) {
				return LambdaCaptureData.of(h, type, info.name());
			}
			h += type.getSize();
		}
		throw new IllegalStateException(expression.position.toString() + ": Captured Statement error");
	}

	private int findFirstValidCaptureIndex(final FunctionExpression expression) {
		int h = 1;
		for (final FunctionParameter parameter : expression.header.parameters) {
			h += this.getTypeFrom(parameter.type).getSize();
		}
		return h;
	}

	private Type getTypeFrom(final TypeID type) {
		return this.context.getType(type);
	}
}
