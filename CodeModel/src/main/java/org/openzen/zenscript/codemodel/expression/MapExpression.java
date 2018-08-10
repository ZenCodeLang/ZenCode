/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.ITypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public class MapExpression extends Expression {
	public final Expression[] keys;
	public final Expression[] values;
	
	public MapExpression(
			CodePosition position,
			Expression[] keys,
			Expression[] values,
			ITypeID type) {
		super(position, type, binaryThrow(position, multiThrow(position, keys), multiThrow(position, values)));
		
		this.keys = keys;
		this.values = values;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitMap(this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		Expression[] tKeys = Expression.transform(keys, transformer);
		Expression[] tValues = Expression.transform(values, transformer);
		return tKeys == keys && tValues == values ? this : new MapExpression(position, tKeys, tValues, type);
	}

	@Override
	public Expression normalize(TypeScope scope) {
		Expression[] normalizedKeys = new Expression[keys.length];
		for (int i = 0; i < normalizedKeys.length; i++)
			normalizedKeys[i] = keys[i].normalize(scope);
		
		Expression[] normalizedValues = new Expression[values.length];
		for (int i = 0; i < normalizedValues.length; i++)
			normalizedValues[i] = values[i].normalize(scope);
		
		return new MapExpression(position, normalizedKeys, normalizedValues, type.getNormalized());
	}
}
