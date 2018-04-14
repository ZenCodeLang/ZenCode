/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.statements;

import java.util.ArrayList;
import java.util.List;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.statement.CatchClause;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.statement.TryCatchStatement;
import org.openzen.zenscript.linker.ExpressionScope;
import org.openzen.zenscript.linker.StatementScope;
import org.openzen.zenscript.parser.expression.ParsedExpression;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedStatementTryCatch extends ParsedStatement {
	public final String resourceName;
	public final ParsedExpression resourceInitializer;
	public final ParsedStatement statement;
	public final List<ParsedCatchClause> catchClauses;
	public final ParsedStatement finallyClause;
	
	public ParsedStatementTryCatch(
			CodePosition position,
			String resourceName,
			ParsedExpression resourceInitializer,
			ParsedStatement statement,
			List<ParsedCatchClause> catchClauses,
			ParsedStatement finallyClause) {
		super(position);
		
		this.resourceName = resourceName;
		this.resourceInitializer = resourceInitializer;
		this.statement = statement;
		this.catchClauses = catchClauses;
		this.finallyClause = finallyClause;
	}

	@Override
	public Statement compile(StatementScope scope) {
		Expression resourceInitializer = this.resourceInitializer == null ? null : this.resourceInitializer.compile(new ExpressionScope(scope)).eval();
		Statement statement = this.statement.compile(scope);
		List<CatchClause> catches = new ArrayList<>();
		for (ParsedCatchClause catchClause : catchClauses)
			catches.add(catchClause.compile(scope));
		
		Statement finallyClause = this.finallyClause == null ? null : this.finallyClause.compile(scope);
		return new TryCatchStatement(position, resourceName, resourceInitializer, statement, catches, finallyClause);
	}
}
