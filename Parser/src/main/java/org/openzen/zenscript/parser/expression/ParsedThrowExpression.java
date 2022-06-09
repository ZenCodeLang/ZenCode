package org.openzen.zenscript.parser.expression;

import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.compilation.expression.AbstractCompilingExpression;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.type.BasicTypeID;

public class ParsedThrowExpression extends ParsedExpression {
	public final CompilableExpression value;

	public ParsedThrowExpression(CodePosition position, CompilableExpression value) {
		super(position);

		this.value = value;
	}

	@Override
	public CompilingExpression compile(ExpressionCompiler compiler) {
		return new Compiling(compiler, position, value.compile(compiler));
	}

	private static class Compiling extends AbstractCompilingExpression {
		private final CompilingExpression value;

		public Compiling(ExpressionCompiler compiler, CodePosition position, CompilingExpression value) {
			super(compiler, position);

			this.value = value;
		}

		@Override
		public Expression eval() {
			return compiler.getThrowableType()
					.map(t -> compiler.at(position).throw_(BasicTypeID.VOID, value.cast(cast(t)).value))
					.orElseGet(() -> compiler.at(position).invalid(CompileErrors.cannotThrowHere()));
		}

		@Override
		public CastedExpression cast(CastedEval cast) {
			return CastedExpression.exact(compiler.getThrowableType()
					.map(t -> compiler.at(position).throw_(cast.type, value.cast(cast(t)).value))
					.orElseGet(() -> compiler.at(position).invalid(CompileErrors.cannotThrowHere(), cast.type)));
		}
	}
}
