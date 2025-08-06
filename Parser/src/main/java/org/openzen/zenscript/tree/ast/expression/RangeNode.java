package org.openzen.zenscript.tree.ast.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

public class RangeNode extends ExpressionNode {
	private final CodePosition position;
	private final ExpressionNode from;
	private final ExpressionNode to;

	public RangeNode(CodePosition position, ExpressionNode from, ExpressionNode to) {
		this.position = position;
		this.from = from;
		this.to = to;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitRange(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public ExpressionNode from() {
		return from;
	}

	public ExpressionNode to() {
		return to;
	}
}
