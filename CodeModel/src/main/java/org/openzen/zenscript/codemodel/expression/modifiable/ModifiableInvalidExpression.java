package org.openzen.zenscript.codemodel.expression.modifiable;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileError;
import org.openzen.zenscript.codemodel.expression.ExpressionTransformer;
import org.openzen.zenscript.codemodel.type.TypeID;

public class ModifiableInvalidExpression implements ModifiableExpression {
	public final CodePosition position;
	public final TypeID type;
	public final CompileError error;

	public ModifiableInvalidExpression(CodePosition position, TypeID type, CompileError error) {
		this.position = position;
		this.type = type;
		this.error = error;
	}

	@Override
	public TypeID getType() {
		return type;
	}

	@Override
	public <T> T accept(ModifiableExpressionVisitor<T> visitor) {
		return visitor.visitInvalid(this);
	}

	@Override
	public <C, R> R accept(C context, ModifiableExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitInvalid(context, this);
	}

	@Override
	public ModifiableExpression transform(ExpressionTransformer transformer) {
		return this;
	}
}
