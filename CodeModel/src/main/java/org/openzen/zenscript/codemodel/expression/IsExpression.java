/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public class IsExpression extends Expression {
	public final Expression value;
	public final TypeID isType;
	
	public IsExpression(CodePosition position, Expression value, TypeID type) {
		super(position, BasicTypeID.BOOL.stored, value.thrownType);
		
		this.value = value;
		this.isType = type;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitIs(this);
	}

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitIs(context, this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		Expression tValue = value.transform(transformer);
		return tValue == value ? this : new IsExpression(position, tValue, isType);
	}

	@Override
	public Expression normalize(TypeScope scope) {
		return new IsExpression(position, value.normalize(scope), isType.getNormalizedUnstored());
	}
}
