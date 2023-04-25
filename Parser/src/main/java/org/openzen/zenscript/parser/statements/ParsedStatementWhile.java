package org.openzen.zenscript.parser.statements;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.WhitespaceInfo;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.compilation.statement.CompilingExpressionCodeStatement;
import org.openzen.zenscript.codemodel.compilation.statement.CompilingLoopStatement;
import org.openzen.zenscript.codemodel.compilation.statement.CompilingStatement;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.ssa.CodeBlock;
import org.openzen.zenscript.codemodel.statement.LoopStatement;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.statement.WhileStatement;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.parser.ParsedAnnotation;

import java.util.Collections;
import java.util.List;

public class ParsedStatementWhile extends ParsedStatement {
	public final CompilableExpression condition;
	public final ParsedStatement content;
	public final String label;

	public ParsedStatementWhile(CodePosition position, ParsedAnnotation[] annotations, WhitespaceInfo whitespace, String label, CompilableExpression condition, ParsedStatement content) {
		super(position, annotations, whitespace);

		this.condition = condition;
		this.content = content;
		this.label = label;
	}

	@Override
	public CompilingStatement compile(StatementCompiler compiler, CodeBlock lastBlock) {
		CompilingExpression condition = this.condition.compile(compiler.expressions());

		CodeBlock check = new CodeBlock();
		CodeBlock tail = new CodeBlock();
		CodeBlock contentBlock = new CodeBlock();

		lastBlock.addSuccessor(check);
		check.addSuccessor(contentBlock);
		check.addSuccessor(tail);
		contentBlock.addSuccessor(check);

		check.add(new CompilingExpressionCodeStatement(condition));

		Compiling compiling = new Compiling(compiler, condition, check, tail);
		compiling.content = this.content.compile(compiler.forLoop(compiling), contentBlock);
		return compiling;
	}

	private class Compiling implements CompilingStatement, CompilingLoopStatement {
		private final StatementCompiler compiler;
		private final CompilingExpression condition;
		private CompilingStatement content;
		private final CodeBlock check;
		private final CodeBlock tail;
		private WhileStatement compiled;

		public Compiling(StatementCompiler compiler, CompilingExpression condition, CodeBlock check, CodeBlock tail) {
			this.compiler = compiler;
			this.condition = condition;
			this.check = check;
			this.tail = tail;
		}

		@Override
		public List<String> getLabels() {
			return label == null ? Collections.emptyList() : Collections.singletonList(label);
		}

		@Override
		public List<CompilingVariable> getLoopVariables() {
			return Collections.emptyList();
		}

		@Override
		public CodeBlock getContinueTarget() {
			return check;
		}

		@Override
		public CodeBlock getBreakTarget() {
			return tail;
		}

		@Override
		public LoopStatement getCompiled() {
			return compiled;
		}

		@Override
		public Statement complete() {
			Expression condition = this.condition.as(BasicTypeID.BOOL);
			compiled = new WhileStatement(position, label, condition);
			compiled.setContent(content.complete());
			return result(compiled, compiler);
		}

		@Override
		public CodeBlock getTail() {
			return tail;
		}
	}
}
