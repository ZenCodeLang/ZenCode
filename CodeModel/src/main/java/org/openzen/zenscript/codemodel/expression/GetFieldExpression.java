/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import java.util.Collections;
import java.util.List;
import org.openzen.zenscript.codemodel.member.FieldMember;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.shared.CodePosition;
import org.openzen.zenscript.shared.CompileException;
import org.openzen.zenscript.shared.CompileExceptionCode;

/**
 *
 * @author Hoofdgebruiker
 */
public class GetFieldExpression extends Expression {
	public final Expression target;
	public final FieldMember field;
	
	public GetFieldExpression(CodePosition position, Expression target, FieldMember field) {
		super(position, field.type, target.thrownType);
		
		this.target = target;
		this.field = field;
	}
	
	@Override
	public List<ITypeID> getAssignHints() {
		return Collections.singletonList(type);
	}
	
	@Override
	public CapturedExpression capture(CodePosition position, LambdaClosure closure) {
		return new CapturedDirectExpression(position, closure, this);
	}
	
	@Override
	public Expression assign(CodePosition position, TypeScope scope, Expression value) {
		return new SetFieldExpression(position, target, field, value);
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitGetField(this);
	}
}
