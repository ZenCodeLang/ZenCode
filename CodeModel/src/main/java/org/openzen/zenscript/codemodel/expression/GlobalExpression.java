package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;

public class GlobalExpression extends Expression {
	public final String name;
	public final Expression resolution;

	public GlobalExpression(CodePosition position, String name, Expression resolution) {
		super(position, resolution.type, resolution.thrownType);

		this.name = name;
		this.resolution = resolution;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitGlobal(this);
	}

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitGlobal(context, this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		Expression tResolution = resolution.transform(transformer);
		return resolution == tResolution ? this : new GlobalExpression(position, name, resolution);
	}
}
