/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import java.util.Collections;
import java.util.List;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.member.ref.FieldMemberRef;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.ITypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public class GetFieldExpression extends Expression {
	public final Expression target;
	public final FieldMemberRef field;
	
	public GetFieldExpression(CodePosition position, Expression target, FieldMemberRef field) {
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

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		Expression tTarget = target.transform(transformer);
		return tTarget == target ? this : new GetFieldExpression(position, tTarget, field);
	}
}
