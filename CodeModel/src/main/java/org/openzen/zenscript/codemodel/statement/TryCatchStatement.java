/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.statement;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.ConcatMap;
import org.openzen.zenscript.codemodel.expression.ExpressionTransformer;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.StoredType;

/**
 *
 * @author Hoofdgebruiker
 */
public class TryCatchStatement extends Statement {
	public final VarStatement resource;
	public final Statement content;
	public final List<CatchClause> catchClauses;
	public final Statement finallyClause;
	
	public TryCatchStatement(
			CodePosition position,
			VarStatement resource,
			Statement content,
			List<CatchClause> catchClauses,
			Statement finallyClause) {
		super(position, null); // TODO: thrown type
		
		this.resource = resource;
		this.content = content;
		this.catchClauses = catchClauses;
		this.finallyClause = finallyClause;
	}

	@Override
	public <T> T accept(StatementVisitor<T> visitor) {
		return visitor.visitTryCatch(this);
	}
	
	@Override
	public <C, R> R accept(C context, StatementVisitorWithContext<C, R> visitor) {
		return visitor.visitTryCatch(context, this);
	}
	
	@Override
	public void forEachStatement(Consumer<Statement> consumer) {
		consumer.accept(this);
		for (CatchClause catchClause : catchClauses) {
			catchClause.content.forEachStatement(consumer);
		}
		if (finallyClause != null)
			finallyClause.forEachStatement(consumer);
	}

	@Override
	public Statement transform(StatementTransformer transformer, ConcatMap<LoopStatement, LoopStatement> modified) {
		VarStatement tResource = resource == null ? null : resource.transform(transformer, modified);
		Statement tContent = content.transform(transformer, modified);
		List<CatchClause> tCatchClauses = new ArrayList<>();
		for (CatchClause clause : catchClauses)
			tCatchClauses.add(clause.transform(transformer, modified));
		Statement tFinallyClause = finallyClause == null ? null : finallyClause.transform(transformer, modified);
		return new TryCatchStatement(position, tResource, tContent, tCatchClauses, tFinallyClause);
	}

	@Override
	public Statement transform(ExpressionTransformer transformer, ConcatMap<LoopStatement, LoopStatement> modified) {
		VarStatement tResource = resource == null ? null : resource.transform(transformer, modified);
		Statement tContent = content.transform(transformer, modified);
		List<CatchClause> tCatchClauses = new ArrayList<>();
		for (CatchClause clause : catchClauses)
			tCatchClauses.add(clause.transform(transformer, modified));
		Statement tFinallyClause = finallyClause == null ? null : finallyClause.transform(transformer, modified);
		return new TryCatchStatement(position, tResource, tContent, tCatchClauses, tFinallyClause);
	}

	@Override
	public Statement normalize(TypeScope scope, ConcatMap<LoopStatement, LoopStatement> modified) {
		VarStatement tResource = resource == null ? null : resource.normalize(scope, modified);
		Statement tContent = content.normalize(scope, modified);
		List<CatchClause> tCatchClauses = new ArrayList<>();
		for (CatchClause clause : catchClauses)
			tCatchClauses.add(clause.normalize(scope, modified));
		Statement tFinallyClause = finallyClause == null ? null : finallyClause.normalize(scope, modified);
		return new TryCatchStatement(position, tResource, tContent, tCatchClauses, tFinallyClause);
	}

	@Override
	public StoredType getReturnType() {
		if(finallyClause != null && finallyClause.getReturnType() != null)
			return finallyClause.getReturnType();

		final StoredType contentType = content.getReturnType();
		//TODO check catch clauses and do stuff I guess?
		return contentType;
	}
}
