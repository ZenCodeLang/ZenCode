/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.shared.CodePosition;

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
}
