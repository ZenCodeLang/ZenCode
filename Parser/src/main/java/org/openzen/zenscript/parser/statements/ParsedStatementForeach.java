package org.openzen.zenscript.parser.statements;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileError;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.WhitespaceInfo;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.compilation.statement.CompilingExpressionCodeStatement;
import org.openzen.zenscript.codemodel.compilation.statement.CompilingLoopStatement;
import org.openzen.zenscript.codemodel.compilation.statement.CompilingStatement;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.InvalidExpression;
import org.openzen.zenscript.codemodel.identifiers.instances.IteratorInstance;
import org.openzen.zenscript.codemodel.ssa.CodeBlock;
import org.openzen.zenscript.codemodel.ssa.IterateStatement;
import org.openzen.zenscript.codemodel.statement.*;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.parser.ParsedAnnotation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ParsedStatementForeach extends ParsedStatement {
	private final String[] varnames;
	private final CompilableExpression list;
	private final ParsedStatement body;

	public ParsedStatementForeach(CodePosition position, ParsedAnnotation[] annotations, WhitespaceInfo whitespace, String[] varnames, CompilableExpression list, ParsedStatement body) {
		super(position, annotations, whitespace);

		this.varnames = varnames;
		this.list = list;
		this.body = body;
	}

	@Override
	public CompilingStatement compile(StatementCompiler compiler, CodeBlock lastBlock) {
		CodeBlock iterate = new CodeBlock();
		CodeBlock content = new CodeBlock();
		CodeBlock tail = new CodeBlock();

		lastBlock.addSuccessor(iterate);
		iterate.addSuccessor(content);
		iterate.addSuccessor(tail);

		List<CompilingVariable> variables = new ArrayList<>();
		for (String varname : varnames)
			variables.add(new CompilingVariable(new VariableID(), varname, null, true));

		CompilingExpression list = this.list.compile(compiler.expressions());
		lastBlock.add(new CompilingExpressionCodeStatement(list));
		lastBlock.add(new IterateStatement(position, compiler, variables, list));

		Compiling compiling = new Compiling(compiler, list, iterate, variables, tail);
		compiling.content = this.body.compile(compiler.forLoop(compiling), content);
		compiling.content.getTail().addSuccessor(iterate);
		return compiling;
	}

	private class Compiling implements CompilingStatement, CompilingLoopStatement {
		private final StatementCompiler compiler;
		private final CompilingExpression list;
		private final CodeBlock iterate;
		private final List<CompilingVariable> variables;
		private final CodeBlock tail;

		private CompilingStatement content;
		private ForeachStatement compiled;

		Compiling(StatementCompiler compiler, CompilingExpression list, CodeBlock iterate, List<CompilingVariable> variables, CodeBlock tail) {
			this.compiler = compiler;
			this.list = list;
			this.iterate = iterate;
			this.variables = variables;
			this.tail = tail;
		}

		@Override
		public List<String> getLabels() {
			return Arrays.stream(varnames).collect(Collectors.toList());
		}

		@Override
		public List<CompilingVariable> getLoopVariables() {
			return variables;
		}

		@Override
		public CodeBlock getContinueTarget() {
			return iterate;
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
			Expression list = this.list.eval();
			if (list.type.isInvalid() && list instanceof InvalidExpression) {
				return new InvalidStatement(position, ((InvalidExpression) list).error);
			}

			ResolvedType listType = compiler.resolve(list.type);
			Optional<IteratorInstance> maybeIterator = listType.findIterator(varnames.length);
			if (!maybeIterator.isPresent()) {
				return new InvalidStatement(position, CompileErrors.noSuchIterator(list.type, varnames.length));
			}

			IteratorInstance iterator = maybeIterator.get();
			TypeID[] loopTypes = iterator.getLoopVariableTypes();
			VarStatement[] variables = new VarStatement[varnames.length];
			for (int i = 0; i < variables.length; i++) {
				variables[i] = new VarStatement(position, this.variables.get(i).id, varnames[i], loopTypes[i], null, true);
				this.variables.get(i).inferredType = loopTypes[i];
			}

			compiled = new ForeachStatement(position, variables, iterator, list, new LoopStatement.ObjectId());
			compiled.setContent(content.complete());
			return result(compiled, compiler);
		}

		@Override
		public CodeBlock getTail() {
			return tail;
		}
	}
}
