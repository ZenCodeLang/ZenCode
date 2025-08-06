package org.openzen.zenscript.tree.ast.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

public class BoolNode extends ExpressionNode {
	private final CodePosition position;
	private final boolean value;

	public BoolNode(CodePosition position, boolean value) {
		this.position = position;
		this.value = value;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitBool(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public boolean value() {
		return value;
	}
}
