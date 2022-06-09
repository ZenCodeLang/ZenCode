package org.openzen.zenscript.parser.expression;

import org.openzen.zenscript.codemodel.compilation.expression.AbstractCompilingExpression;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.lexer.ParseException;
import org.openzen.zenscript.parser.definitions.ParsedFunctionHeader;
import org.openzen.zenscript.parser.definitions.ParsedFunctionParameter;
import org.openzen.zenscript.parser.type.ParsedTypeBasic;

import java.util.ArrayList;
import java.util.List;

public class ParsedExpressionBracket extends ParsedExpression {
	public List<CompilableExpression> expressions;

	public ParsedExpressionBracket(CodePosition position, List<CompilableExpression> expressions) {
		super(position);

		this.expressions = expressions;
	}

	@Override
	public CompilingExpression compile(ExpressionCompiler compiler) {
		return new Compiling(
				compiler,
				position,
				expressions.stream().map(e -> e.compile(compiler)).toArray(CompilingExpression[]::new));
	}

	@Override
	public ParsedFunctionHeader toLambdaHeader() throws ParseException {
		List<ParsedFunctionParameter> parameters = new ArrayList<>();
		for (CompilableExpression expression : expressions)
			parameters.add(expression.toLambdaParameter());

		return new ParsedFunctionHeader(position, parameters, ParsedTypeBasic.UNDETERMINED);
	}

	private static class Compiling extends AbstractCompilingExpression {
		private final CompilingExpression[] compiling;

		public Compiling(ExpressionCompiler compiler, CodePosition position, CompilingExpression[] compiling) {
			super(compiler, position);
			this.compiling = compiling;
		}

		@Override
		public Expression eval() {
			if (compiling.length != 1) {
				return compiler.at(position).invalid(CompileErrors.bracketMultipleExpressions());
			} else {
				return compiling[0].eval();
			}
		}

		@Override
		public CastedExpression cast(CastedEval cast) {
			if (compiling.length != 1) {
				return cast.invalid(CompileErrors.bracketMultipleExpressions());
			} else {
				return compiling[0].cast(cast);
			}
		}
	}
}
