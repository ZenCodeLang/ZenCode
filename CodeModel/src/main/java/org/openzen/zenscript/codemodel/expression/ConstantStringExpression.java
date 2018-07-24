/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.BasicTypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public class ConstantStringExpression extends Expression {
	public final String value;
	
	public ConstantStringExpression(CodePosition position, String value) {
		super(position, BasicTypeID.STRING, null);
		
		this.value = value;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitConstantString(this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		return this;
	}
	
	@Override
	public String evaluateStringConstant() {
		return value;
	}

	@Override
	public Expression normalize(TypeScope scope) {
		return this;
	}
}
