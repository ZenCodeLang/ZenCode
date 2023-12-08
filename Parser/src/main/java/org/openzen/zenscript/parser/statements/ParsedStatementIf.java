package org.openzen.zenscript.parser.statements;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.WhitespaceInfo;
import org.openzen.zenscript.codemodel.compilation.CompilableExpression;
import org.openzen.zenscript.codemodel.compilation.CompilingExpression;
import org.openzen.zenscript.codemodel.compilation.StatementCompiler;
import org.openzen.zenscript.codemodel.compilation.statement.CompilingExpressionCodeStatement;
import org.openzen.zenscript.codemodel.compilation.statement.CompilingStatement;
import org.openzen.zenscript.codemodel.ssa.CodeBlock;
import org.openzen.zenscript.codemodel.statement.IfStatement;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.parser.ParsedAnnotation;

public class ParsedStatementIf extends ParsedStatement {
	private final CompilableExpression condition;
	private final ParsedStatement onThen;
	private final ParsedStatement onElse;

	public ParsedStatementIf(CodePosition position, ParsedAnnotation[] annotations, WhitespaceInfo whitespace, CompilableExpression condition, ParsedStatement onThen, ParsedStatement onElse) {
		super(position, annotations, whitespace);

		this.condition = condition;
		this.onThen = onThen;
		this.onElse = onElse;
	}

	@Override
	public CompilingStatement compile(StatementCompiler compiler, CodeBlock lastBlock) {
		CompilingExpression condition = this.condition.compile(compiler.expressions());
		lastBlock.add(new CompilingExpressionCodeStatement(condition));

		CodeBlock thenBlock = lastBlock.createNextAlways();
		CodeBlock tail = new CodeBlock();

		CompilingStatement onThen = this.onThen.compile(compiler, thenBlock);
		onThen.getTail().addSuccessor(tail);
		CompilingStatement onElse = null;
		if (this.onElse != null) {
			CodeBlock elseBlock = new CodeBlock();
			onElse = this.onElse.compile(compiler, elseBlock);
			onElse.getTail().addSuccessor(tail);
			lastBlock.addSuccessor(elseBlock);
		} else {
			lastBlock.addSuccessor(tail);
		}
		return new Compiling(compiler, condition, onThen, onElse, tail);
	}

	private class Compiling implements CompilingStatement {
		private final StatementCompiler compiler;
		private final CompilingExpression condition;
		private final CompilingStatement onThen;
		private final CompilingStatement onElse;
		private final CodeBlock tail;

		public Compiling(
				StatementCompiler compiler,
				CompilingExpression condition,
				CompilingStatement onThen,
				CompilingStatement onElse,
				CodeBlock tail
		) {
			this.compiler = compiler;
			this.condition = condition;
			this.onThen = onThen;
			this.onElse = onElse;
			this.tail = tail;
		}

		@Override
		public Statement complete() {
			return result(new IfStatement(
					position,
					condition.as(BasicTypeID.BOOL),
					onThen.complete(),
					onElse == null ? null : onElse.complete()
			), compiler);
		}

		@Override
		public CodeBlock getTail() {
			return tail;
		}
	}
}
