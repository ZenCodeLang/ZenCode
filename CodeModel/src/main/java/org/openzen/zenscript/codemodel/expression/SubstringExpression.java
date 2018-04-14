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
public class SubstringExpression extends Expression {
	public final Expression value;
	public final Expression range;
	
	public SubstringExpression(CodePosition position, Expression value, Expression range) {
		super(position, value.type);
		
		this.value = value;
		this.range = range;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitSubstring(this);
	}
}
