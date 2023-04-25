package org.openzen.zenscript.parser.statements;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.WhitespaceInfo;
import org.openzen.zenscript.codemodel.compilation.CompilableExpression;
import org.openzen.zenscript.codemodel.compilation.CompilingExpression;
import org.openzen.zenscript.codemodel.compilation.StatementCompiler;
import org.openzen.zenscript.codemodel.compilation.statement.AbstractCompilingStatement;
import org.openzen.zenscript.codemodel.compilation.statement.CompilingStatement;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.ssa.CodeBlock;
import org.openzen.zenscript.codemodel.ssa.VarBlockStatement;
import org.openzen.zenscript.codemodel.statement.*;
import org.openzen.zenscript.parser.ParsedAnnotation;

import java.util.ArrayList;
import java.util.List;

public class ParsedStatementTryCatch extends ParsedStatement {
	public final String resourceName;
	public final CompilableExpression resourceInitializer;
	public final ParsedStatement statement;
	public final List<ParsedCatchClause> catchClauses;
	public final ParsedStatement finallyClause;

	public ParsedStatementTryCatch(
			CodePosition position,
			ParsedAnnotation[] annotations,
			WhitespaceInfo whitespace,
			String resourceName,
			CompilableExpression resourceInitializer,
			ParsedStatement statement,
			List<ParsedCatchClause> catchClauses,
			ParsedStatement finallyClause) {
		super(position, annotations, whitespace);

		this.resourceName = resourceName;
		this.resourceInitializer = resourceInitializer;
		this.statement = statement;
		this.catchClauses = catchClauses;
		this.finallyClause = finallyClause;
	}

	@Override
	public CompilingStatement compile(StatementCompiler compiler, CodeBlock lastBlock) {
		CodeBlock contentBlock;
		CompilingExpression resourceInitializer = null;
		VarStatement resource = null;
		if (this.resourceInitializer != null) {
			/*CodeBlock resourceBlock = lastBlock.createNextAlways();
			resourceInitializer = this.resourceInitializer.compile(compiler.expressions());
			resource = new VarStatement(position, new VariableID(), resourceName, resourceInitializer.type, resourceInitializer, true);
			resourceBlock.add(new VarBlockStatement(resourceInitializer));
			contentBlock = resourceBlock.createNext();*/
			throw new UnsupportedOperationException("Not yet supported"); // need to fix type inference for this
		} else {
			contentBlock = lastBlock.createNextAlways();
		}
		CodeBlock tail = new CodeBlock();
		CompilingStatement statement = this.statement.compile(compiler.forBlock(), contentBlock);
		statement.getTail().addSuccessor(tail);

		List<ParsedCatchClause.Compiling> catches = new ArrayList<>();
		for (ParsedCatchClause catchClause : catchClauses) {
			catches.add(catchClause.compile(compiler.forBlock(), statement.getTail()));
		}

		CompilingStatement finallyClause = null;
		if (this.finallyClause != null) {
			finallyClause = this.finallyClause.compile(compiler.forBlock(), tail);
			tail = finallyClause.getTail();
		}
		return new Compiling(compiler, resourceInitializer, statement, catches, finallyClause, tail);
	}

	private class Compiling extends AbstractCompilingStatement {
		private final CompilingExpression resourceInitializer;
		private final CompilingStatement statement;
		private final List<ParsedCatchClause.Compiling> catches;
		private final CompilingStatement finallyClause;

		public Compiling(
				StatementCompiler compiler,
				CompilingExpression resourceInitializer,
				CompilingStatement statement,
				List<ParsedCatchClause.Compiling> catches,
				CompilingStatement finallyClause,
				CodeBlock tail
		) {
			super(compiler, tail);

			this.resourceInitializer = resourceInitializer;
			this.statement = statement;
			this.catches = catches;
			this.finallyClause = finallyClause;
		}

		@Override
		public Statement complete() {
			Expression resourceInitializer = null;
			VarStatement resource = null;
			if (this.resourceInitializer != null) {
				resourceInitializer = this.resourceInitializer.eval();
				resource = new VarStatement(position, new VariableID(), resourceName, resourceInitializer.type, resourceInitializer, true);
			}
			Statement statement = this.statement.complete();

			List<CatchClause> catches = new ArrayList<>();
			for (ParsedCatchClause.Compiling catchClause : this.catches) {
				catches.add(catchClause.complete());
			}

			Statement finallyClause = null;
			if (this.finallyClause != null) {
				finallyClause = this.finallyClause.complete();
			}
			return result(new TryCatchStatement(position, resource, statement, catches, finallyClause), compiler);
		}

		@Override
		public CodeBlock getTail() {
			return tail;
		}
	}
}
