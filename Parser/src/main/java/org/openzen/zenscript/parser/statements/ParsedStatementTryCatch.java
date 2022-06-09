package org.openzen.zenscript.parser.statements;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.WhitespaceInfo;
import org.openzen.zenscript.codemodel.compilation.CompilableExpression;
import org.openzen.zenscript.codemodel.compilation.StatementCompiler;
import org.openzen.zenscript.codemodel.expression.Expression;
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
	public Statement compile(StatementCompiler compiler) {
		Expression resourceInitializer = this.resourceInitializer == null ? null : compiler.compile(this.resourceInitializer);
		Statement statement = this.statement.compile(compiler.forBlock());
		List<CatchClause> catches = new ArrayList<>();
		for (ParsedCatchClause catchClause : catchClauses) {
			catches.add(catchClause.compile(compiler.forBlock()));
		}

		Statement finallyClause = this.finallyClause == null ? null : this.finallyClause.compile(compiler.forBlock());
		VarStatement resource = null;
		if (resourceName != null) {
			resource = new VarStatement(position, new VariableID(), resourceName, resourceInitializer.type, resourceInitializer, true);
		}
		return result(new TryCatchStatement(position, resource, statement, catches, finallyClause), compiler);
	}
}
