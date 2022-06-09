package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.member.ref.FieldMemberRef;
import org.openzen.zenscript.codemodel.scope.TypeScope;

public class GetFieldExpression extends Expression {
	public final Expression target;
	public final FieldMemberRef field;

	public GetFieldExpression(CodePosition position, Expression target, FieldMemberRef field) {
		super(position, field.getType(), target.thrownType);

		this.target = target;
		this.field = field;
	}

	@Override
	public CapturedExpression capture(CodePosition position, LambdaClosure closure) {
		return new CapturedDirectExpression(position, closure, this);
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitGetField(this);
	}

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitGetField(context, this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		Expression tTarget = target.transform(transformer);
		return tTarget == target ? this : new GetFieldExpression(position, tTarget, field);
	}

	@Override
	public IDefinitionMember getMember() {
		return field.member;
	}
}
