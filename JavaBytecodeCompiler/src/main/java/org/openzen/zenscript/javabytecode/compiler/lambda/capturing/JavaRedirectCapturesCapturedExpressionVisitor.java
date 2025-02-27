package org.openzen.zenscript.javabytecode.compiler.lambda.capturing;

import org.objectweb.asm.Type;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.expression.FunctionExpression;
import org.openzen.zenscript.codemodel.expression.GetLocalVariableExpression;
import org.openzen.zenscript.codemodel.expression.captured.*;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.javabytecode.compiler.JavaWriter;
import org.openzen.zenscript.javabytecode.compiler.lambda.LambdaClosureInfo;

import java.util.function.Predicate;

public final class JavaRedirectCapturesCapturedExpressionVisitor implements CapturedExpressionVisitor<Void> {
	private static final class MemberData {
		private final int position;
		private final Type type;

		MemberData(final int position, final Type type) {
			this.position = position;
			this.type = type;
		}

		void load(final JavaWriter writer) {
			writer.load(this.type, this.position);
		}
	}

	private final FunctionExpression functionExpression;
	private final JavaWriter javaWriter;
	private final LambdaClosureInfo closureInfo;

	public JavaRedirectCapturesCapturedExpressionVisitor(final JavaWriter javaWriter, final FunctionExpression functionExpression, final LambdaClosureInfo closureInfo) {
		this.javaWriter = javaWriter;
		this.functionExpression = functionExpression;
		this.closureInfo = closureInfo;
	}

	@Override
	public Void visitCapturedThis(final CapturedThisExpression expression) {
		// TODO("Remove null-check as this method should never return null")
		final Type type = this.closureInfo.thisType() == null? Type.getType(Void.class) : this.getTypeFrom(this.closureInfo.thisType());
		return this.loadByMemberData(new MemberData(0, type));
	}

	@Override
	public Void visitCapturedParameter(CapturedParameterExpression expression) {
		return this.loadByMemberData(this.calculateMemberData(expression, this.functionExpression));
	}

	@Override
	public Void visitCapturedLocal(CapturedLocalVariableExpression expression) {
		return this.loadByMemberData(this.calculateMemberData(new GetLocalVariableExpression(expression.position, expression.variable), this.functionExpression));
	}

	@Override
	public Void visitRecaptured(CapturedClosureExpression expression) {
		return this.loadByMemberData(this.findIndex(expression, this.functionExpression));
	}

	private Void loadByMemberData(final MemberData memberData) {
		memberData.load(this.javaWriter);
		return null;
	}

	private MemberData findIndex(final CapturedExpression capturedExpression, final FunctionExpression expression) {
		return this.calculateMemberData(
				expression,
				capturedExpression.type,
				capturedExpression::equals
		);
	}

	private MemberData calculateMemberData(final GetLocalVariableExpression localVariableExpression, final FunctionExpression expression) {
		return this.calculateMemberData(
				expression,
				localVariableExpression.type,
				capture -> {
					if (capture instanceof CapturedLocalVariableExpression) {
						return ((CapturedLocalVariableExpression) capture).variable == localVariableExpression.variable;
					} else if (capture instanceof CapturedClosureExpression) {
						final CapturedExpression value = ((CapturedClosureExpression) capture).value;
						return value instanceof CapturedLocalVariableExpression && ((CapturedLocalVariableExpression) value).variable == localVariableExpression.variable;
					}
					return false;
				});
	}

	private MemberData calculateMemberData(final CapturedParameterExpression functionParameterExpression, final FunctionExpression expression) {
		return this.calculateMemberData(
				expression,
				functionParameterExpression.parameter.type,
				capture -> capture instanceof CapturedParameterExpression && ((CapturedParameterExpression) capture).parameter == functionParameterExpression.parameter
		);
	}

	private MemberData calculateMemberData(final FunctionExpression expression, final TypeID varType, final Predicate<CapturedExpression> predicate) {
		final Type type = this.getTypeFrom(varType);
		int h = this.findFirstValidCaptureIndex(expression);
		for (final CapturedExpression capture : expression.closure.captures) {
			if (predicate.test(capture)) {
				return new MemberData(h, type);
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
		if (type instanceof BasicTypeID) {
			switch ((BasicTypeID) type) {
				case VOID: return Type.VOID_TYPE;
				case BOOL: return Type.BOOLEAN_TYPE;
				case BYTE: return Type.BYTE_TYPE;
				case SBYTE: return Type.BYTE_TYPE; // TODO()
				case SHORT: return Type.SHORT_TYPE;
				case USHORT: return Type.SHORT_TYPE; // TODO()
				case INT: return Type.INT_TYPE;
				case UINT: return Type.INT_TYPE; // TODO()
				case LONG: return Type.LONG_TYPE;
				case ULONG: return Type.LONG_TYPE; // TODO()
				case USIZE: return Type.INT_TYPE;
				case FLOAT: return Type.FLOAT_TYPE;
				case DOUBLE: return Type.DOUBLE_TYPE;
				case CHAR: return Type.CHAR_TYPE;
			}
		}

		// We don't care about the actual class contained in the type here, we only care that Java treats it as an
		// object, so we simply grab a random Java class
		return Type.getType(Void.class);
	}
}
