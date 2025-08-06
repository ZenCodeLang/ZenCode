package org.openzen.zenscript.tree.ast.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

import java.util.List;

public class ArrayNode extends ExpressionNode {
	private final CodePosition position;
	private final List<ExpressionNode> contents;

	public ArrayNode(CodePosition position, List<ExpressionNode> contents) {
		this.position = position;
		this.contents = contents;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitArray(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public List<ExpressionNode> contents() {
		return contents;
	}
}
