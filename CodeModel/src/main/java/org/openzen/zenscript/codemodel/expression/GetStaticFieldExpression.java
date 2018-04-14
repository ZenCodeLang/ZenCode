/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import java.util.Collections;
import java.util.List;
import org.openzen.zenscript.codemodel.member.FieldMember;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class GetStaticFieldExpression extends Expression {
	public final FieldMember field;
	
	public GetStaticFieldExpression(CodePosition position, FieldMember field) {
		super(position, field.type);
		
		this.field = field;
	}
	
	@Override
	public List<ITypeID> getAssignHints() {
		return Collections.singletonList(field.type);
	}
	
	@Override
	public CapturedExpression capture(CodePosition position, LambdaClosure closure) {
		return new CapturedDirectExpression(position, closure, this);
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitGetStaticField(this);
	}
}
