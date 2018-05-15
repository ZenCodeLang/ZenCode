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
public class GlobalCallExpression extends Expression {
	public final String name;
	public final CallArguments arguments;
	public final Expression resolution;
	
	public GlobalCallExpression(CodePosition position, String name, CallArguments arguments, Expression resolution) {
		super(position, resolution.type, resolution.thrownType);
		
		this.name = name;
		this.arguments = arguments;
		this.resolution = resolution;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitGlobalCall(this);
	}
}
