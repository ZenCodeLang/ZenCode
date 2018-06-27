/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class GlobalExpression extends Expression {
	public final String name;
	public final Expression resolution;
	
	public GlobalExpression(CodePosition position, String name, Expression resolution) {
		super(position, resolution.type, resolution.thrownType);
		
		this.name = name;
		this.resolution = resolution;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitGlobal(this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		Expression tResolution = resolution.transform(transformer);
		return resolution == tResolution ? this : new GlobalExpression(position, name, resolution);
	}
}
