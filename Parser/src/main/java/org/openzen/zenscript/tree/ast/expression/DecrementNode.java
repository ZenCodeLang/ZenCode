package org.openzen.zenscript.tree.ast.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

public class DecrementNode extends ExpressionNode {
	private final CodePosition position;
	private final ExpressionNode expression;
	private final boolean pre;

	public DecrementNode(CodePosition position, ExpressionNode expression, boolean pre) {
		this.position = position;
		this.expression = expression;
		this.pre = pre;
	}

	public boolean pre() {
		return pre;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitDecrement(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public ExpressionNode expression() {
		return expression;
	}
}
