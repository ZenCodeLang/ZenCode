package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.TypeID;

public class InvalidExpression extends Expression {
	public final CompileExceptionCode code;
	public final String message;
	
	public InvalidExpression(CodePosition position, TypeID type, CompileExceptionCode code, String message) {
		super(position, type, null);
		
		this.code = code;
		this.message = message;
	}
	
	public InvalidExpression(TypeID type, CompileException cause) {
		this(cause.position, type, cause.code, cause.message);
	}
	
	@Override
	public Expression assign(CodePosition position, TypeScope scope, Expression value) {
		return new InvalidAssignExpression(position, this, value);
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
	public Expression normalize(TypeScope scope) {
		return this;
	}
}
