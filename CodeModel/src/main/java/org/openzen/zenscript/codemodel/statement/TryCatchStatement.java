/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.statement;

import java.util.ArrayList;
import java.util.List;
import org.openzen.zenscript.codemodel.expression.ExpressionTransformer;
import org.openzen.zenscript.shared.CodePosition;
import org.openzen.zenscript.shared.ConcatMap;

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
	public Statement transform(ExpressionTransformer transformer, ConcatMap<LoopStatement, LoopStatement> modified) {
		VarStatement tResource = resource == null ? null : resource.transform(transformer, modified);
		Statement tContent = content.transform(transformer, modified);
		List<CatchClause> tCatchClauses = new ArrayList<>();
		for (CatchClause clause : catchClauses)
			tCatchClauses.add(clause.transform(transformer, modified));
		Statement tFinallyClause = finallyClause == null ? null : finallyClause.transform(transformer, modified);
		return new TryCatchStatement(position, tResource, tContent, tCatchClauses, tFinallyClause);
	}
}
