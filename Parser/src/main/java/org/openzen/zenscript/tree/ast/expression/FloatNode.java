package org.openzen.zenscript.tree.ast.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

public class FloatNode extends ExpressionNode {
	private final CodePosition position;
	private final double value;
	private final String suffix;

	public FloatNode(CodePosition position, double value, String suffix) {
		this.position = position;
		this.value = value;
		this.suffix = suffix;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitFloat(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public double value() {
		return value;
	}

	public String suffix() {
		return suffix;
	}
}
