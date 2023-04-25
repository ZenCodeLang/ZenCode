package org.openzen.zenscript.parser.statements;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.WhitespaceInfo;
import org.openzen.zenscript.codemodel.compilation.CompilableExpression;
import org.openzen.zenscript.codemodel.compilation.CompileErrors;
import org.openzen.zenscript.codemodel.compilation.CompilingExpression;
import org.openzen.zenscript.codemodel.compilation.StatementCompiler;
import org.openzen.zenscript.codemodel.compilation.statement.CompilingStatement;
import org.openzen.zenscript.codemodel.compilation.statement.InvalidCompilingStatement;
import org.openzen.zenscript.codemodel.ssa.CodeBlock;
import org.openzen.zenscript.codemodel.statement.ReturnStatement;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.parser.ParsedAnnotation;

import java.util.Optional;

public class ParsedStatementReturn extends ParsedStatement {
	private final CompilableExpression expression;

	public ParsedStatementReturn(CodePosition position, ParsedAnnotation[] annotations, WhitespaceInfo whitespace, CompilableExpression expression) {
		super(position, annotations, whitespace);

		this.expression = expression;
	}

	@Override
	public CompilingStatement compile(StatementCompiler compiler, CodeBlock lastBlock) {
		Optional<FunctionHeader> maybeFunctionHeader = compiler.getFunctionHeader();
		if (!maybeFunctionHeader.isPresent())
			return new InvalidCompilingStatement(position, lastBlock, CompileErrors.returnOutsideFunction());

		return new Compiling(
				compiler,
				expression == null ? null : expression.compile(compiler.expressions()),
				maybeFunctionHeader.get(),
				lastBlock);
	}

	private class Compiling implements CompilingStatement {
		private final StatementCompiler compiler;
		private final CompilingExpression value;
		private final FunctionHeader functionHeader;
		private final CodeBlock block;

		public Compiling(StatementCompiler compiler, CompilingExpression value, FunctionHeader functionHeader, CodeBlock block) {
			this.compiler = compiler;
			this.value = value;
			this.functionHeader = functionHeader;
			this.block = block;
		}

		@Override
		public Statement complete() {
			return result(new ReturnStatement(position, value == null ? null : value.as(functionHeader.getReturnType())), compiler);
		}

		@Override
		public CodeBlock getTail() {
			return block;
		}
	}
}
