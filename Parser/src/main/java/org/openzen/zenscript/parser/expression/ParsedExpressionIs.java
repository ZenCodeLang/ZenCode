package org.openzen.zenscript.parser.expression;

import org.openzen.zenscript.codemodel.compilation.expression.AbstractCompilingExpression;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.ssa.CodeBlockStatement;
import org.openzen.zenscript.codemodel.ssa.SSAVariableCollector;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.parser.type.IParsedType;

public class ParsedExpressionIs extends ParsedExpression {
	private final CompilableExpression expression;
	private final IParsedType type;

	public ParsedExpressionIs(CodePosition position, CompilableExpression expression, IParsedType type) {
		super(position);

		this.expression = expression;
		this.type = type;
	}

	@Override
	public CompilingExpression compile(ExpressionCompiler compiler) {
		return new Compiling(compiler, position, expression.compile(compiler), type.compile(compiler.types()));
	}

	private static class Compiling extends AbstractCompilingExpression {
		private final CompilingExpression expression;
		private final TypeID type;

		public Compiling(ExpressionCompiler compiler, CodePosition position, CompilingExpression expression, TypeID type) {
			super(compiler, position);

			this.expression = expression;
			this.type = type;
		}

		@Override
		public Expression eval() {
			return compiler.at(position).is(expression.eval(), type);
		}

		@Override
		public CastedExpression cast(CastedEval cast) {
			return cast.of(eval());
		}

		@Override
		public void collect(SSAVariableCollector collector) {
			expression.collect(collector);
		}

		@Override
		public void linkVariables(CodeBlockStatement.VariableLinker linker) {
			expression.linkVariables(linker);
		}
	}
}
