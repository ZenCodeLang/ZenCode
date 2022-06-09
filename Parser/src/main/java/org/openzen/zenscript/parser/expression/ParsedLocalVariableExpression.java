package org.openzen.zenscript.parser.expression;

import org.openzen.zenscript.codemodel.compilation.CompileErrors;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.compilation.expression.AbstractCompilingExpression;
import org.openzen.zenscript.codemodel.expression.Expression;

import java.util.Optional;

public class ParsedLocalVariableExpression extends ParsedExpression {
	private final String name;

	public ParsedLocalVariableExpression(CodePosition position, String name) {
		super(position);

		this.name = name;
	}

	@Override
	public CompilingExpression compile(ExpressionCompiler compiler) {
		Optional<LocalType> localType = compiler.getLocalType();
		if (!localType.isPresent())
			return compiler.invalid(position, CompileErrors.noThisInScope());

		ResolvedType resolved = compiler.resolve(localType.get().getThisType());
		Optional<ResolvedType.Field> field = resolved.findField(name);
		if (!field.isPresent())
			return compiler.invalid(position, CompileErrors.noFieldInType(localType.get().getThisType(), name));

		return new Compiling(compiler, position, field.get());
	}

	private static class Compiling extends AbstractCompilingExpression {
		private final ResolvedType.Field field;

		public Compiling(ExpressionCompiler compiler, CodePosition position, ResolvedType.Field field) {
			super(compiler, position);
			this.field = field;
		}

		@Override
		public Expression eval() {
			return field.get(compiler.at(position));
		}

		@Override
		public CastedExpression cast(CastedEval cast) {
			return cast.of(eval());
		}

		@Override
		public CompilingExpression assign(CompilingExpression value) {
			return new CompilingAssign(compiler, position, field, value);
		}
	}

	private static class CompilingAssign extends AbstractCompilingExpression {
		private final ResolvedType.Field field;
		private final CompilingExpression value;

		public CompilingAssign(ExpressionCompiler compiler, CodePosition position, ResolvedType.Field field, CompilingExpression value) {
			super(compiler, position);

			this.field = field;
			this.value = value;
		}

		@Override
		public Expression eval() {
			return field.set(compiler.at(position), value.cast(cast(field.getType())).value);
		}

		@Override
		public CastedExpression cast(CastedEval cast) {
			return cast.of(eval());
		}
	}
}
