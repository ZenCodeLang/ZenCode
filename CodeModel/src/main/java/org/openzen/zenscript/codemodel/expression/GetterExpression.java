/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.member.ref.GetterMemberRef;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;

/**
 *
 * @author Hoofdgebruiker
 */
public class GetterExpression extends Expression {
	public final Expression target;
	public final GetterMemberRef getter;
	
	public GetterExpression(CodePosition position, Expression target, GetterMemberRef getter) {
		super(position, getter.getType(), target.thrownType);
		
		this.target = target;
		this.getter = getter;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitGetter(this);
	}

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitGetter(context, this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		Expression tTarget = target.transform(transformer);
		return target == tTarget ? this : new GetterExpression(position, tTarget, getter);
	}

	@Override
	public Expression normalize(TypeScope scope) {
		return new GetterExpression(position, target.normalize(scope), getter);
	}
	
	@Override
	public Expression assign(CodePosition position, TypeScope scope, Expression value) throws CompileException {
		return scope.getTypeMembers(getter.getOwnerType())
				.getOrCreateGroup(getter.member.name, false)
				.setter(position, scope, target, value, false);
	}
}
