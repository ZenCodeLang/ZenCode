package org.openzen.zenscript.tree.ast.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

public class DollarNode extends ExpressionNode {
	private final CodePosition position;

	public DollarNode(CodePosition position) {
		this.position = position;
	}

	@Override
	public CodePosition position() {
		return position;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitDollar(this, context);
	}

}
