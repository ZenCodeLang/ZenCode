/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class ConstantDoubleExpression extends Expression {
	public final double value;
	
	public ConstantDoubleExpression(CodePosition position, double value) {
		super(position, BasicTypeID.DOUBLE);
		
		this.value = value;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitConstantDouble(this);
	}
}
