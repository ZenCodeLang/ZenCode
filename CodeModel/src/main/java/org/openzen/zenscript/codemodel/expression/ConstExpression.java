/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import org.openzen.zenscript.codemodel.member.ConstMember;
import org.openzen.zenscript.codemodel.member.EnumConstantMember;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class ConstExpression extends Expression {
	public final ConstMember constant;
	
	public ConstExpression(CodePosition position, ConstMember constant) {
		super(position, constant.type, null);
		
		this.constant = constant;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitConst(this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		return this;
	}
	
	@Override
	public String evaluateStringConstant() {
		return constant.value.evaluateStringConstant();
	}
	
	@Override
	public EnumConstantMember evaluateEnumConstant() {
		return constant.value.evaluateEnumConstant();
	}
}
