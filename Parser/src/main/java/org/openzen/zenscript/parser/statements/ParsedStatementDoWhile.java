package org.openzen.zenscript.parser.statements;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.WhitespaceInfo;
import org.openzen.zenscript.codemodel.compilation.CompilableExpression;
import org.openzen.zenscript.codemodel.compilation.CompilingExpression;
import org.openzen.zenscript.codemodel.compilation.CompilingVariable;
import org.openzen.zenscript.codemodel.compilation.StatementCompiler;
import org.openzen.zenscript.codemodel.compilation.statement.CompilingExpressionCodeStatement;
import org.openzen.zenscript.codemodel.compilation.statement.CompilingLoopStatement;
import org.openzen.zenscript.codemodel.compilation.statement.CompilingStatement;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.ssa.CodeBlock;
import org.openzen.zenscript.codemodel.statement.DoWhileStatement;
import org.openzen.zenscript.codemodel.statement.LoopStatement;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.parser.ParsedAnnotation;

import java.util.Collections;
import java.util.List;

public class ParsedStatementDoWhile extends ParsedStatement {
	public final String label;
	public final ParsedStatement content;
	public final CompilableExpression condition;

	public ParsedStatementDoWhile(CodePosition position, ParsedAnnotation[] annotations, WhitespaceInfo whitespace, String label, ParsedStatement content, CompilableExpression condition) {
		super(position, annotations, whitespace);

		this.label = label;
		this.content = content;
		this.condition = condition;
	}

	@Override
	public CompilingStatement compile(StatementCompiler compiler, CodeBlock lastBlock) {
		CodeBlock check = new CodeBlock();
		CodeBlock content = new CodeBlock();
		CodeBlock tail = new CodeBlock();

		lastBlock.addSuccessor(content);
		check.addSuccessor(content);
		check.addSuccessor(tail);

		CompilingExpression condition = this.condition.compile(compiler.expressions());
		check.add(new CompilingExpressionCodeStatement(condition));

		Compiling compiling = new Compiling(compiler, condition, check, tail);
		compiling.content = this.content.compile(compiler.forLoop(compiling), content);
		compiling.content.getTail().addSuccessor(check);
		return compiling;
	}

	private class Compiling implements CompilingStatement, CompilingLoopStatement {
		private final StatementCompiler compiler;
		private final CompilingExpression condition;

		private final CodeBlock checkBlock;
		private final CodeBlock tailBlock;

		private CompilingStatement content;
		private DoWhileStatement compiled;

		public Compiling(StatementCompiler compiler, CompilingExpression condition, CodeBlock checkBlock, CodeBlock tailBlock) {
			this.compiler = compiler;
			this.condition = condition;

			this.checkBlock = checkBlock;
			this.tailBlock = tailBlock;
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
			return checkBlock;
		}

		@Override
		public CodeBlock getBreakTarget() {
			return tailBlock;
		}

		@Override
		public LoopStatement getCompiled() {
			return compiled;
		}

		@Override
		public Statement complete() {
			Expression condition = this.condition.as(BasicTypeID.BOOL);
			compiled = new DoWhileStatement(position, label, condition, new LoopStatement.ObjectId());
			compiled.content = this.content.complete();
			return result(compiled, compiler);
		}

		@Override
		public CodeBlock getTail() {
			return tailBlock;
		}
	}
}
