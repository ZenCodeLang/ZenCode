package org.openzen.zenscript.parser.expression;

import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.compilation.expression.AbstractCompilingExpression;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.parser.type.IParsedType;

public class ParsedNewExpression extends ParsedExpression {
	private final IParsedType type;
	private final ParsedCallArguments arguments;

	public ParsedNewExpression(CodePosition position, IParsedType type, ParsedCallArguments arguments) {
		super(position);

		this.type = type;
		this.arguments = arguments;
	}

	@Override
	public CompilingExpression compile(ExpressionCompiler compiler) {
		TypeID type = this.type.compile(compiler.types());
		return new Compiling(compiler, position, type, arguments.compile(compiler));
	}

	private static class Compiling extends AbstractCompilingExpression {
		private final TypeID type;
		private final CompilingExpression[] arguments;

		public Compiling(ExpressionCompiler compiler, CodePosition position, TypeID type, CompilingExpression[] arguments) {
			super(compiler, position);

			this.type = type;
			this.arguments = arguments;
		}

		@Override
		public Expression eval() {
			return compiler.resolve(type).getConstructor().call(compiler.at(position), arguments);
		}

		@Override
		public CastedExpression cast(CastedEval cast) {
			return cast.of(eval());
		}
	}
}
