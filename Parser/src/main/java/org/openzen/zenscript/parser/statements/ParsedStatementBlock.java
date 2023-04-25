package org.openzen.zenscript.parser.statements;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.WhitespaceInfo;
import org.openzen.zenscript.codemodel.WhitespacePostComment;
import org.openzen.zenscript.codemodel.compilation.statement.CompilingStatement;
import org.openzen.zenscript.codemodel.compilation.StatementCompiler;
import org.openzen.zenscript.codemodel.ssa.CodeBlock;
import org.openzen.zenscript.codemodel.statement.BlockStatement;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.parser.ParsedAnnotation;

import java.util.ArrayList;
import java.util.List;

public class ParsedStatementBlock extends ParsedStatement {
	private final List<ParsedStatement> statements;
	private final WhitespacePostComment postComment;

	public ParsedStatementBlock(CodePosition position, ParsedAnnotation[] annotations, WhitespaceInfo whitespace, WhitespacePostComment postComment, List<ParsedStatement> statements) {
		super(position, annotations, whitespace);

		this.statements = statements;
		this.postComment = postComment;
	}

	@Override
	public CompilingStatement compile(StatementCompiler compiler, CodeBlock lastBlock) {
		StatementCompiler blockCompiler = compiler.forBlock();
		List<CompilingStatement> compiling = new ArrayList<>();
		for (ParsedStatement statement : statements) {
			CompilingStatement compiled1 = statement.compile(blockCompiler, lastBlock);
			lastBlock = compiled1.getTail();
			compiling.add(compiled1);
		}
		return new Compiling(compiler, compiling, lastBlock);
	}

	private class Compiling implements CompilingStatement {
		private final StatementCompiler compiler;
		private final List<CompilingStatement> statements;
		private final CodeBlock tail;

		public Compiling(StatementCompiler compiler, List<CompilingStatement> statements, CodeBlock tail) {
			this.compiler = compiler;
			this.statements = statements;
			this.tail = tail;
		}

		@Override
		public Statement complete() {
			Statement[] compiled = new Statement[statements.size()];
			int i = 0;
			for (CompilingStatement statement : statements) {
				Statement compiled1 = statement.complete();
				compiled[i++] = compiled1;
			}
			BlockStatement block = new BlockStatement(position, compiled);
			result(block, compiler);
			block.setTag(WhitespacePostComment.class, postComment);
			return block;
		}

		@Override
		public CodeBlock getTail() {
			return tail;
		}
	}
}
