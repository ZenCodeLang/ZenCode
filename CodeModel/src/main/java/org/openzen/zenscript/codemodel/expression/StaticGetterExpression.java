package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.member.ref.GetterMemberRef;
import org.openzen.zenscript.codemodel.scope.TypeScope;

public class StaticGetterExpression extends Expression {
	public final GetterMemberRef getter;

	public StaticGetterExpression(CodePosition position, GetterMemberRef getter) {
		super(position, getter.getType(), null);

		this.getter = getter;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitStaticGetter(this);
	}

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitStaticGetter(context, this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		return this;
	}

	@Override
	public Expression normalize(TypeScope scope) {
		return this;
	}

	@Override
	public Expression assign(CodePosition position, TypeScope scope, Expression value) throws CompileException {
		return scope.getTypeMembers(getter.getOwnerType())
				.getOrCreateGroup(getter.member.name, false)
				.staticSetter(position, scope, value);
	}
}
