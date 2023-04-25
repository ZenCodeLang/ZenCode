package org.openzen.zenscript.parser.statements;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.WhitespaceInfo;
import org.openzen.zenscript.codemodel.compilation.CompilableExpression;
import org.openzen.zenscript.codemodel.compilation.CompilingExpression;
import org.openzen.zenscript.codemodel.compilation.StatementCompiler;
import org.openzen.zenscript.codemodel.compilation.statement.CompilingExpressionCodeStatement;
import org.openzen.zenscript.codemodel.compilation.statement.CompilingStatement;
import org.openzen.zenscript.codemodel.ssa.CodeBlock;
import org.openzen.zenscript.codemodel.statement.ExpressionStatement;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.parser.ParsedAnnotation;

public class ParsedStatementExpression extends ParsedStatement {
	private final CompilableExpression expression;

	public ParsedStatementExpression(CodePosition position, ParsedAnnotation[] annotations, WhitespaceInfo whitespace, CompilableExpression expression) {
		super(position, annotations, whitespace);

		this.expression = expression;
	}

	@Override
	public CompilingStatement compile(StatementCompiler compiler, CodeBlock lastBlock) {
		return new Compiling(compiler, expression.compile(compiler.expressions()), lastBlock);
	}

	private class Compiling implements CompilingStatement {
		private final StatementCompiler compiler;
		private final CompilingExpression expression;
		private final CodeBlock block;

		public Compiling(StatementCompiler compiler, CompilingExpression expression, CodeBlock block) {
			this.compiler = compiler;
			this.expression = expression;
			this.block = block;

			block.add(new CompilingExpressionCodeStatement(expression));
		}

		@Override
		public Statement complete() {
			ExpressionStatement statement = new ExpressionStatement(position, expression.eval());
			return result(statement, compiler);
		}

		@Override
		public CodeBlock getTail() {
			return block;
		}
	}
}
