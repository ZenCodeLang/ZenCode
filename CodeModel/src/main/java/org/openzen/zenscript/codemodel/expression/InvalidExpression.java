package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileError;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Optional;

public class InvalidExpression extends Expression {
	public final CompileError error;

	public InvalidExpression(CodePosition position, TypeID type, CompileError error) {
		super(position, type, null);

		this.error = error;
	}

	public InvalidExpression(TypeID type, CompileException cause) {
		this(cause.position, type, cause.error);
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitInvalid(this);
	}

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitInvalid(context, this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		return this;
	}

	@Override
	public Optional<InvalidExpression> asInvalid() {
		return Optional.of(this);
	}
}
