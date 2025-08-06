package org.openzen.zenscript.tree.ast.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

public class PanicNode extends ExpressionNode {
	private final CodePosition position;
	private final ExpressionNode value;

	public PanicNode(CodePosition position, ExpressionNode value) {
		this.position = position;
		this.value = value;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitPanic(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public ExpressionNode value() {
		return value;
	}
}
