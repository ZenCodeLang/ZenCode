/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.statement;

import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.ExpressionTransformer;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.shared.CodePosition;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.shared.ConcatMap;

/**
 *
 * @author Hoofdgebruiker
 */
public class ReturnStatement extends Statement {
	public final Expression value;
	
	public ReturnStatement(CodePosition position, Expression value) {
		super(position, value == null ? null : value.thrownType);
		
		this.value = value;
	}
	
	@Override
	public ITypeID getReturnType() {
		return value.type;
	}
	
	@Override
	public Statement withReturnType(TypeScope scope, ITypeID returnType) {
		return new ReturnStatement(position, value == null ? null : value.castImplicit(position, scope, returnType));
	}

	@Override
	public <T> T accept(StatementVisitor<T> visitor) {
		return visitor.visitReturn(this);
	}

	@Override
	public Statement transform(ExpressionTransformer transformer, ConcatMap<LoopStatement, LoopStatement> modified) {
		Expression tValue = value.transform(transformer);
		return tValue == value ? this : new ReturnStatement(position, tValue);
	}
}
