package org.openzen.zenscript.parser.expression;

import org.openzen.zenscript.codemodel.compilation.expression.AbstractCompilingExpression;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;

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
		public Expression eval() {
			if (suffix.equals("f") || suffix.equals("F"))
				return compiler.at(position).constant((float) value);
			if (suffix.equals("d") || suffix.equals("D"))
				return compiler.at(position).constant(value);

			if (suffix.isEmpty()) {
				return compiler.at(position).constant(value);
			} else {
				return compiler.at(position).invalid(CompileErrors.invalidFloatSuffix(suffix));
			}
		}

		@Override
		public CastedExpression cast(CastedEval cast) {
			TypeID actualType = cast.type.simplified();

			if (actualType == BasicTypeID.FLOAT || actualType == BasicTypeID.DOUBLE)
				return cast.of(eval());

			ResolvedType resolvedType = compiler.resolve(actualType);
			if (suffix.isEmpty()) {
				return resolvedType.findImplicitConstructor()
						.map(constructor -> constructor.casted(compiler, position, cast, null, this))
						.orElseGet(() -> cast.invalid(CompileErrors.cannotCompileFloatLiteralAs(cast.type)));
			} else {
				return resolvedType.findSuffixConstructor(suffix)
						.map(method -> method.casted(compiler, position, cast, null, this))
						.orElseGet(() -> cast.invalid(CompileErrors.cannotCompileFloatLiteralAs(cast.type, suffix)));
			}
		}
	}
}
