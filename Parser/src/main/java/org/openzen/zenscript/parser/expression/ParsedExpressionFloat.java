package org.openzen.zenscript.parser.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.expression.*;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.compiler.InferredType;
import org.openzen.zenscript.compiler.expression.AbstractCompilingExpression;
import org.openzen.zenscript.compiler.expression.CompilingExpression;
import org.openzen.zenscript.compiler.expression.ExpressionCompiler;
import org.openzen.zenscript.compiler.expression.TypeMatch;
import org.openzen.zenscript.compiler.types.ResolvedType;

import java.util.ArrayList;
import java.util.List;

public class ParsedExpressionFloat extends ParsedExpression {
	public final double value;
	public final String suffix;

	public ParsedExpressionFloat(CodePosition position, String value) {
		super(position);

		int split = value.length();
		while (isLetter(value.charAt(split - 1)))
			split--;

		this.value = Double.parseDouble(value.substring(0, split));
		suffix = value.substring(split);
	}

	private static boolean isLetter(char c) {
		return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
	}

	@Override
	public CompilingExpression compile(ExpressionCompiler compiler) {
		return new Compiling(compiler, position, value, suffix);
	}

	private static class Compiling extends AbstractCompilingExpression {
		private final double value;
		private final String suffix;

		public Compiling(ExpressionCompiler compiler, CodePosition position, double value, String suffix) {
			super(compiler, position);
			this.value = value;
			this.suffix = suffix;
		}

		@Override
		public Expression as(TypeID type) {
			if (type == BasicTypeID.FLOAT)
				return compiler.at(position, type).constant((float)value);
			else if (type == BasicTypeID.DOUBLE)
				return compiler.at(position, type).constant(value);

			ResolvedType resolvedType = compiler.resolve(type);
			if (suffix.isEmpty()) {
				return resolvedType.findConstructor(method -> method.isImplicit() && method.isCompatible(type, BasicTypeID.DOUBLE))
						.map(constructor -> compiler.at(position, type).call(constructor, this))
						.orElseGet(() -> compiler.at(position, type).invalid(
								CompileExceptionCode.NO_SUCH_MEMBER,
								"Compiling float constant to a non-float type without implicit constructor accepting float"));
			} else {
				return resolvedType.findStaticMethod(suffix, method -> method.isImplicit() && method.isCompatible(type, BasicTypeID.DOUBLE))
						.map(method -> compiler.at(position, type).call(method, this))
						.orElseGet(() -> compiler.at(position, type).invalid(
								CompileExceptionCode.NO_SUCH_MEMBER,
								"Compiling float constant to a non-float type without matching suffix"));
			}
		}

		@Override
		public TypeMatch matches(TypeID returnType) {
			List<TypeID> matching = new ArrayList<>();
			if (suffix.isEmpty()) {
				// No suffix but expression to be known as Float or Double -> That type
				if (returnType == BasicTypeID.DOUBLE)
					return TypeMatch.EXACT;
				else if (returnType == BasicTypeID.FLOAT)
					return TypeMatch.EXACT;
				else
					return TypeMatch.max(
							compiler.matchType(BasicTypeID.DOUBLE, returnType),
							compiler.matchType(BasicTypeID.FLOAT, returnType));
			} else {
				// Suffix and TypeHint given
				// Check <TypeHint>.<Suffix>(<value>)
				// E.g. 10.0s as TimeSpan -> TimeSpan.s(10.0)
				ResolvedType type = compiler.resolve(returnType);
				if (type.findStaticMethod(suffix, method -> method.isImplicit() && method.isCompatible(returnType, BasicTypeID.DOUBLE)).isPresent())
					return TypeMatch.EXACT;

				if (suffix.equals("f") || suffix.equals("F"))
					return compiler.matchType(BasicTypeID.FLOAT, returnType);
				else if (suffix.equals("d") || suffix.equals("D"))
					return compiler.matchType(BasicTypeID.DOUBLE, returnType);
				else
					return TypeMatch.NONE;
			}
		}

		@Override
		public InferredType inferType() {
			if (suffix.equals("f") || suffix.equals("F"))
				return InferredType.success(BasicTypeID.FLOAT);
			if (suffix.equals("d") || suffix.equals("D"))
				return InferredType.success(BasicTypeID.DOUBLE);

			if (suffix.isEmpty()) {
				return InferredType.success(BasicTypeID.DOUBLE);
			} else {
				return InferredType.failure(CompileExceptionCode.INVALID_SUFFIX, "Invalid suffix: " + suffix);
			}
		}
	}
}
