package org.openzen.zenscript.tree.ast.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

public class AndNode extends ExpressionNode {
	private final CodePosition position;
	private final ExpressionNode left;
	private final ExpressionNode right;

	public AndNode(CodePosition position, ExpressionNode left, ExpressionNode right) {
		this.position = position;
		this.left = left;
		this.right = right;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitAnd(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public ExpressionNode right() {
		return right;
	}

	public ExpressionNode left() {
		return left;
	}
}
