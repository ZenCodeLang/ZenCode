package org.openzen.zenscript.tree.ast.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

public class USHRNode extends ExpressionNode {
	private final CodePosition position;
	private final ExpressionNode left;
	private final ExpressionNode right;

	public USHRNode(CodePosition position, ExpressionNode left, ExpressionNode right) {
		this.position = position;
		this.left = left;
		this.right = right;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitUSHR(this, context);
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
