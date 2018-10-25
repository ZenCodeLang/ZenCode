/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.StoredType;

/**
 *
 * @author Hoofdgebruiker
 */
public class StorageCastExpression extends Expression {
	public final Expression value;
	
	public StorageCastExpression(CodePosition position, Expression value, StoredType toType) {
		super(position, toType, value.thrownType);
		
		this.value = value;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitStorageCast(this);
	}

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitStorageCast(context, this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		Expression transformed = transformer.transform(value);
		return transformed == this ? this : new StorageCastExpression(position, transformed, type);
	}

	@Override
	public Expression normalize(TypeScope scope) {
		Expression normalized = value.normalize(scope);
		return normalized == value ? this : new StorageCastExpression(position, normalized, type);
	}
}
