package org.openzen.zenscript.parser.expression;

import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.compilation.expression.AbstractCompilingExpression;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.type.BasicTypeID;

public class ParsedPanicExpression extends ParsedExpression {
	public final CompilableExpression value;

	public ParsedPanicExpression(CodePosition position, CompilableExpression value) {
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
			return compiler.at(position).panic(BasicTypeID.VOID, value.cast(cast(BasicTypeID.STRING)).value);
		}

		@Override
		public CastedExpression cast(CastedEval cast) {
			return CastedExpression.exact(compiler.at(position).panic(cast.type, value.cast(cast(BasicTypeID.STRING)).value));
		}
	}
}
