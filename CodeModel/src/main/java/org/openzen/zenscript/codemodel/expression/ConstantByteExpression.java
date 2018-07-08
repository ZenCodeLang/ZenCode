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
public class ConstantByteExpression extends Expression {
	public final int value;
	
	public ConstantByteExpression(CodePosition position, int value) {
		super(position, BasicTypeID.BYTE, null);
		
		this.value = value;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitConstantByte(this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		return this;
	}
}
