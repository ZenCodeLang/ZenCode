package org.openzen.zenscript.tree.ast.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

import java.util.List;

public class MapNode extends ExpressionNode {
	private final CodePosition position;
	private final List<ExpressionNode> keys;
	private final List<ExpressionNode> values;

	public MapNode(CodePosition position, List<ExpressionNode> keys, List<ExpressionNode> values) {
		this.position = position;
		this.keys = keys;
		this.values = values;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitMap(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public List<ExpressionNode> keys() {
		return keys;
	}

	public List<ExpressionNode> values() {
		return values;
	}
}
