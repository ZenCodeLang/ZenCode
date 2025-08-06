package org.openzen.zenscript.tree.ast.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

public class InvertNode extends ExpressionNode {
	private final CodePosition position;
	private final ExpressionNode expression;

	public InvertNode(CodePosition position, ExpressionNode expression) {
		this.position = position;
		this.expression = expression;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitInvert(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public ExpressionNode expression() {
		return expression;
	}
}
