/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.statements;

import java.util.ArrayList;
import java.util.List;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.WhitespaceInfo;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.statement.CatchClause;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.statement.TryCatchStatement;
import org.openzen.zenscript.codemodel.statement.VarStatement;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.scope.StatementScope;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.PrecompilationState;
import org.openzen.zenscript.parser.expression.ParsedExpression;

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
			ParsedAnnotation[] annotations,
			WhitespaceInfo whitespace,
			String resourceName,
			ParsedExpression resourceInitializer,
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
	public Statement compile(StatementScope scope) {
		Expression resourceInitializer = this.resourceInitializer == null ? null : this.resourceInitializer.compile(new ExpressionScope(scope)).eval();
		Statement statement = this.statement.compile(scope);
		List<CatchClause> catches = new ArrayList<>();
		for (ParsedCatchClause catchClause : catchClauses) {
			catches.add(catchClause.compile(scope));
		}
		
		Statement finallyClause = this.finallyClause == null ? null : this.finallyClause.compile(scope);
		VarStatement resource = null;
		if (resourceName != null) {
			resource = new VarStatement(position, resourceName, resourceInitializer.type, resourceInitializer, true);
		}
		return result(new TryCatchStatement(position, resource, statement, catches, finallyClause), scope);
	}

	@Override
	public ITypeID precompileForResultType(StatementScope scope, PrecompilationState precompileState) {
		ITypeID result = statement.precompileForResultType(scope, precompileState);
		for (ParsedCatchClause catchClause : catchClauses) {
			result = union(scope, result, catchClause.content.precompileForResultType(scope, precompileState));
		}
		if (finallyClause != null) {
			result = union(scope, result, finallyClause.precompileForResultType(scope, precompileState));
		}
		return result;
	}
}
