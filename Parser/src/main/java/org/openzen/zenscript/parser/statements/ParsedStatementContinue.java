package org.openzen.zenscript.parser.statements;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.WhitespaceInfo;
import org.openzen.zenscript.codemodel.compilation.CompileErrors;
import org.openzen.zenscript.codemodel.compilation.StatementCompiler;
import org.openzen.zenscript.codemodel.compilation.statement.CompilingLoopStatement;
import org.openzen.zenscript.codemodel.compilation.statement.CompilingStatement;
import org.openzen.zenscript.codemodel.compilation.statement.InvalidCompilingStatement;
import org.openzen.zenscript.codemodel.ssa.CodeBlock;
import org.openzen.zenscript.codemodel.ssa.CodeBlockStatement;
import org.openzen.zenscript.codemodel.ssa.SSAVariableCollector;
import org.openzen.zenscript.codemodel.statement.ContinueStatement;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.parser.ParsedAnnotation;

public class ParsedStatementContinue extends ParsedStatement {
	public final String name;

	public ParsedStatementContinue(CodePosition position, ParsedAnnotation[] annotations, WhitespaceInfo whitespace, String name) {
		super(position, annotations, whitespace);

		this.name = name;
	}

	@Override
	public CompilingStatement compile(StatementCompiler compiler, CodeBlock lastBlock) {
		return compiler.getLoop(name)
				.<CompilingStatement>map(loop -> new Compiling(compiler, loop, lastBlock))
				.orElseGet(() -> new InvalidCompilingStatement(position, lastBlock, CompileErrors.continueOutsideLoop(name)));
	}

	private class Compiling implements CompilingStatement, CodeBlockStatement {
		private final StatementCompiler compiler;
		private final CompilingLoopStatement loop;
		private final CodeBlock block;

		public Compiling(StatementCompiler compiler, CompilingLoopStatement loop, CodeBlock block) {
			this.compiler = compiler;
			this.loop = loop;
			this.block = block;
			block.addSuccessor(loop.getContinueTarget());
		}

		@Override
		public Statement complete() {
			return result(new ContinueStatement(position, loop.getCompiled()), compiler);
		}

		@Override
		public CodeBlock getTail() {
			return block;
		}

		@Override
		public void collect(SSAVariableCollector collector) {

		}

		@Override
		public void linkVariables(VariableLinker linker) {

		}
	}
}
