/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import org.openzen.zenscript.codemodel.member.FieldMember;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class SetStaticFieldExpression extends Expression {
	public final FieldMember field;
	public final Expression value;
	
	public SetStaticFieldExpression(CodePosition position, FieldMember field, Expression value) {
		super(position, field.type, value.thrownType);
		
		this.field = field;
		this.value = value;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitSetStaticField(this);
	}
}
