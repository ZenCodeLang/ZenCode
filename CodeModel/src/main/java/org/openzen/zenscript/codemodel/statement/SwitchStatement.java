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
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.ExpressionTransformer;
import org.openzen.zenscript.codemodel.scope.TypeScope;

/**
 *
 * @author Hoofdgebruiker
 */
public class SwitchStatement extends LoopStatement {
	public final Expression value;
	public final List<SwitchCase> cases = new ArrayList<>();
	
	public SwitchStatement(CodePosition position, String label, Expression value) {
		super(position, label, null); // TODO: thrown type
		
		this.value = value;
	}

	@Override
	public <T> T accept(StatementVisitor<T> visitor) {
		return visitor.visitSwitch(this);
	}
	
	@Override
	public void forEachStatement(Consumer<Statement> consumer) {
		consumer.accept(this);
		for (SwitchCase switchCase : cases) {
			for (Statement statement : switchCase.statements)
				statement.forEachStatement(consumer);
		}
	}

	@Override
	public Statement transform(StatementTransformer transformer, ConcatMap<LoopStatement, LoopStatement> modified) {
		Expression tValue = value.transform(transformer);
		SwitchStatement result = new SwitchStatement(position, label, tValue);
		ConcatMap<LoopStatement, LoopStatement> tModified = modified.concat(this, result);
		for (SwitchCase case_ : cases) {
			result.cases.add(case_.transform(transformer, tModified));
		}
		return result;
	}

	@Override
	public Statement transform(ExpressionTransformer transformer, ConcatMap<LoopStatement, LoopStatement> modified) {
		Expression tValue = value.transform(transformer);
		SwitchStatement result = new SwitchStatement(position, label, tValue);
		ConcatMap<LoopStatement, LoopStatement> tModified = modified.concat(this, result);
		for (SwitchCase case_ : cases) {
			result.cases.add(case_.transform(transformer, tModified));
		}
		return result;
	}

	@Override
	public Statement normalize(TypeScope scope, ConcatMap<LoopStatement, LoopStatement> modified) {
		SwitchStatement result = new SwitchStatement(position, label, value.normalize(scope));
		ConcatMap<LoopStatement, LoopStatement> tModified = modified.concat(this, result);
		for (SwitchCase case_ : cases) {
			result.cases.add(case_.normalize(scope, tModified));
		}
		return result;
	}
}
