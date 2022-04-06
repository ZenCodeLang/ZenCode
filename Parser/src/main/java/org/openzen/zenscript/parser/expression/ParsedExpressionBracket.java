package org.openzen.zenscript.parser.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.compiler.InferredType;
import org.openzen.zenscript.compiler.expression.AbstractCompilingExpression;
import org.openzen.zenscript.compiler.expression.CompilingExpression;
import org.openzen.zenscript.compiler.expression.ExpressionCompiler;
import org.openzen.zenscript.compiler.expression.TypeMatch;
import org.openzen.zenscript.lexer.ParseException;
import org.openzen.zenscript.parser.definitions.ParsedFunctionHeader;
import org.openzen.zenscript.parser.definitions.ParsedFunctionParameter;
import org.openzen.zenscript.parser.type.ParsedTypeBasic;

import java.util.ArrayList;
import java.util.List;

public class ParsedExpressionBracket extends ParsedExpression {
	public List<ParsedExpression> expressions;

	public ParsedExpressionBracket(CodePosition position, List<ParsedExpression> expressions) {
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
		for (ParsedExpression expression : expressions)
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
		public Expression as(TypeID type) {
			if (compiling.length != 1) {
				return compiler.at(position, type).invalid(CompileExceptionCode.BRACKET_MULTIPLE_EXPRESSIONS, "Bracket expression may have only one expression");
			} else {
				return compiling[0].as(type);
			}
		}

		@Override
		public TypeMatch matches(TypeID returnType) {
			return compiling[0].matches(returnType);
		}

		@Override
		public InferredType inferType() {
			return compiling[0].inferType();
		}
	}
}
