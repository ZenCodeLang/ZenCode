package org.openzen.zenscript.tree.ast.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

public class SuperNode extends ExpressionNode {
	private final CodePosition position;

	public SuperNode(CodePosition position) {
		this.position = position;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitSuper(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

}
