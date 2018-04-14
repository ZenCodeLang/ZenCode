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
 * Compare expression for basic types. Left and right MUST be of the same type,
 * and MUST be a basic type. (any integer or floating point type, char, string)
 * 
 * @author Hoofdgebruiker
 */
public class BasicCompareExpression extends Expression {
	public final Expression left;
	public final Expression right;
	public final CompareType operator;
	
	public BasicCompareExpression(CodePosition position, Expression left, Expression right, CompareType operator) {
		super(position, BasicTypeID.BOOL);
		
		this.left = left;
		this.right = right;
		this.operator = operator;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitCompare(this);
	}
}
