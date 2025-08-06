package org.openzen.zenscript.tree.ast.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

public class SameNode extends ExpressionNode {
	private final CodePosition position;
	private final ExpressionNode left;
	private final ExpressionNode right;

	public SameNode(CodePosition position, ExpressionNode left, ExpressionNode right) {
		this.position = position;
		this.left = left;
		this.right = right;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitSame(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public ExpressionNode left() {
		return left;
	}

	public ExpressionNode right() {
		return right;
	}
}
