/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import org.openzen.zenscript.codemodel.CompareType;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class GenericCompareExpression extends Expression {
	public final Expression value;
	public final CompareType operator;
	
	public GenericCompareExpression(CodePosition position, Expression value, CompareType operator) {
		super(position, BasicTypeID.BOOL);
		
		this.value = value;
		this.operator = operator;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitGenericCompare(this);
	}
}
