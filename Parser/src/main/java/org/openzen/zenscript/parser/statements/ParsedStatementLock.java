package org.openzen.zenscript.parser.statements;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.WhitespaceInfo;
import org.openzen.zenscript.codemodel.compilation.CompilableExpression;
import org.openzen.zenscript.codemodel.compilation.CompilingExpression;
import org.openzen.zenscript.codemodel.compilation.StatementCompiler;
import org.openzen.zenscript.codemodel.compilation.statement.CompilingExpressionCodeStatement;
import org.openzen.zenscript.codemodel.compilation.statement.CompilingStatement;
import org.openzen.zenscript.codemodel.ssa.CodeBlock;
import org.openzen.zenscript.codemodel.statement.LockStatement;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.parser.ParsedAnnotation;

public class ParsedStatementLock extends ParsedStatement {
	public final CompilableExpression object;
	public final ParsedStatement content;

	public ParsedStatementLock(CodePosition position, ParsedAnnotation[] annotations, WhitespaceInfo whitespace, CompilableExpression object, ParsedStatement content) {
		super(position, annotations, whitespace);
		this.object = object;
		this.content = content;
	}

	@Override
	public CompilingStatement compile(StatementCompiler compiler, CodeBlock lastBlock) {
		CompilingExpression object = this.object.compile(compiler.expressions());
		lastBlock.add(new CompilingExpressionCodeStatement(object));
		return new Compiling(compiler, object, content.compile(compiler, lastBlock));
	}

	private class Compiling implements CompilingStatement {
		private final StatementCompiler compiler;
		private final CompilingExpression object;
		private final CompilingStatement content;

		Compiling(StatementCompiler compiler, CompilingExpression object, CompilingStatement content) {
			this.compiler = compiler;
			this.object = object;
			this.content = content;
		}

		@Override
		public Statement complete() {
			return result(new LockStatement(position, object.eval(), content.complete()), compiler);
		}

		@Override
		public CodeBlock getTail() {
			return content.getTail();
		}
	}
}
