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
import org.openzen.zenscript.codemodel.statement.InvalidStatement;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.statement.ThrowStatement;
import org.openzen.zenscript.parser.ParsedAnnotation;

import java.util.Optional;

public class ParsedStatementThrow extends ParsedStatement {
	private final CompilableExpression expression;

	public ParsedStatementThrow(CodePosition position, ParsedAnnotation[] annotations, WhitespaceInfo whitespace, CompilableExpression expression) {
		super(position, annotations, whitespace);

		this.expression = expression;
	}

	@Override
	public CompilingStatement compile(StatementCompiler compiler, CodeBlock lastBlock) {
		Optional<FunctionHeader> maybeHeader = compiler.getFunctionHeader();
		if (!compiler.getThrownType().isPresent())
			return new InvalidCompilingStatement(position, lastBlock, CompileErrors.cannotThrowHere());

		FunctionHeader header = maybeHeader.orElse(null);
		return new Compiling(compiler, expression.compile(compiler.expressions()), lastBlock, header);
	}

	private class Compiling implements CompilingStatement {
		private final StatementCompiler compiler;
		private final CompilingExpression value;
		private final CodeBlock block;
		private final FunctionHeader header;

		Compiling(StatementCompiler compiler, CompilingExpression value, CodeBlock block, FunctionHeader header) {
			this.compiler = compiler;
			this.value = value;
			this.block = block;
			this.header = header;
		}

		@Override
		public Statement complete() {
			if (header.thrownType == null)
				return new InvalidStatement(position, CompileErrors.cannotThrowHere());

			return result(new ThrowStatement(position, value.as(header.thrownType)), compiler);
		}

		@Override
		public CodeBlock getTail() {
			return block;
		}
	}
}
