package org.openzen.zenscript.tree.ast.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

public class ThisNode extends ExpressionNode {
	private final CodePosition position;

	public ThisNode(CodePosition position) {
		this.position = position;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitThis(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}
}
