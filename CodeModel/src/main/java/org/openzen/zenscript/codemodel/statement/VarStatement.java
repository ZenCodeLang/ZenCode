/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.statement;

import java.util.function.Consumer;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.ConcatMap;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.ExpressionTransformer;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.StoredType;

/**
 *
 * @author Hoofdgebruiker
 */
public class VarStatement extends Statement {
	public final String name;
	public final StoredType type;
	public final Expression initializer;
	public final VariableID variable;
	public final boolean isFinal;
	
	public VarStatement(CodePosition position, VariableID variable, String name, StoredType type, Expression initializer, boolean isFinal) {
		super(position, initializer == null ? null : initializer.thrownType);
		
		this.name = name;
		this.type = type;
		this.initializer = initializer;
		this.variable = variable;
		this.isFinal = isFinal;
	}

	@Override
	public <T> T accept(StatementVisitor<T> visitor) {
		return visitor.visitVar(this);
	}
	
	@Override
	public <C, R> R accept(C context, StatementVisitorWithContext<C, R> visitor) {
		return visitor.visitVar(context, this);
	}
	
	@Override
	public void forEachStatement(Consumer<Statement> consumer) {
		consumer.accept(this);
	}

	@Override
	public VarStatement transform(StatementTransformer transformer, ConcatMap<LoopStatement, LoopStatement> modified) {
		Expression tInitializer = initializer == null ? null : initializer.transform(transformer);
		return tInitializer == initializer ? this : new VarStatement(position, variable, name, type, tInitializer, isFinal);
	}

	@Override
	public VarStatement transform(ExpressionTransformer transformer, ConcatMap<LoopStatement, LoopStatement> modified) {
		Expression tInitializer = initializer == null ? null : initializer.transform(transformer);
		return tInitializer == initializer ? this : new VarStatement(position, variable, name, type, tInitializer, isFinal);
	}

	@Override
	public VarStatement normalize(TypeScope scope, ConcatMap<LoopStatement, LoopStatement> modified) {
		return new VarStatement(position, variable, name, type.getNormalized(), initializer == null ? null : initializer.normalize(scope), isFinal);
	}
}
