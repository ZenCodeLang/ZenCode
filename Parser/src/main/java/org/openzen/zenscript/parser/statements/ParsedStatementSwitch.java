package org.openzen.zenscript.parser.statements;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.WhitespaceInfo;
import org.openzen.zenscript.codemodel.compilation.CompilableExpression;
import org.openzen.zenscript.codemodel.compilation.CompilingExpression;
import org.openzen.zenscript.codemodel.compilation.CompilingVariable;
import org.openzen.zenscript.codemodel.compilation.StatementCompiler;
import org.openzen.zenscript.codemodel.compilation.statement.CompilingLoopStatement;
import org.openzen.zenscript.codemodel.compilation.statement.CompilingStatement;
import org.openzen.zenscript.codemodel.ssa.CodeBlock;
import org.openzen.zenscript.codemodel.statement.LoopStatement;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.statement.SwitchStatement;
import org.openzen.zenscript.parser.ParsedAnnotation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ParsedStatementSwitch extends ParsedStatement {
	private final String name;
	private final CompilableExpression value;
	private final List<ParsedSwitchCase> cases;

	public ParsedStatementSwitch(CodePosition position, ParsedAnnotation[] annotations, WhitespaceInfo whitespace, String name, CompilableExpression value, List<ParsedSwitchCase> cases) {
		super(position, annotations, whitespace);

		this.name = name;
		this.value = value;
		this.cases = cases;
	}

	@Override
	public CompilingStatement compile(StatementCompiler compiler, CodeBlock lastBlock) {

		CodeBlock tail = new CodeBlock();

		CompilingExpression value = this.value.compile(compiler.expressions());

		Compiling result = new Compiling(compiler, value, tail);
		StatementCompiler innerScope = compiler.forLoop(result);

		CodeBlock next = new CodeBlock();
		for (ParsedSwitchCase switchCase : cases) {
			CodeBlock current = next;
			next = new CodeBlock();
			result.continueBlock = next;
			result.cases.add(switchCase.compile(innerScope, current));
		}

		return result;
	}

	private class Compiling implements CompilingStatement, CompilingLoopStatement {
		private final StatementCompiler compiler;
		private final CompilingExpression value;
		private final List<ParsedSwitchCase.Compiling> cases = new ArrayList<>();
		private final CodeBlock tail;

		private CodeBlock continueBlock;
		private SwitchStatement compiled;

		Compiling(StatementCompiler compiler, CompilingExpression value, CodeBlock tail) {
			this.compiler = compiler;
			this.value = value;
			this.tail = tail;
		}

		@Override
		public List<String> getLabels() {
			return name == null ? Collections.emptyList() : Collections.singletonList(name);
		}

		@Override
		public List<CompilingVariable> getLoopVariables() {
			return Collections.emptyList();
		}

		@Override
		public CodeBlock getContinueTarget() {
			return continueBlock;
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
			compiled = new SwitchStatement(position, name, value.eval());

			for (ParsedSwitchCase.Compiling switchCase : cases) {
				compiled.cases.add(switchCase.complete(compiled.value.type));
			}

			return result(compiled, compiler);
		}

		@Override
		public CodeBlock getTail() {
			return tail;
		}
	}
}
