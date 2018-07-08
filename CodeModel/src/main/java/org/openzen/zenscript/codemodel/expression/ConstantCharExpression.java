/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.type.BasicTypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public class ConstantCharExpression extends Expression {
	public final char value;
	
	public ConstantCharExpression(CodePosition position, char value) {
		super(position, BasicTypeID.CHAR, null);
		
		this.value = value;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitConstantChar(this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		return this;
	}
}
