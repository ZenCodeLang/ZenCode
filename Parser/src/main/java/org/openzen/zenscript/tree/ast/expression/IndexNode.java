package org.openzen.zenscript.tree.ast.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

import java.util.List;

public class IndexNode extends ExpressionNode {
	private final CodePosition position;
	private final ExpressionNode value;
	private final List<ExpressionNode> indices;

	public IndexNode(CodePosition position, ExpressionNode value, List<ExpressionNode> indices) {
		this.position = position;
		this.value = value;
		this.indices = indices;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitIndex(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public ExpressionNode value() {
		return value;
	}

	public List<ExpressionNode> indices() {
		return indices;
	}
}
