/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.TypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public class MakeConstExpression extends Expression {
	public final Expression value;
	
	public MakeConstExpression(CodePosition position, Expression value, TypeID constType) {
		super(position, constType, value.thrownType);
		
		this.value = value;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitMakeConst(this);
	}

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitMakeConst(context, this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		Expression tValue = value.transform(transformer);
		return tValue == value ? this : new MakeConstExpression(position, tValue, type);
	}

	@Override
	public Expression normalize(TypeScope scope) {
		return new MakeConstExpression(position, value.normalize(scope), type.getNormalized());
	}
}
