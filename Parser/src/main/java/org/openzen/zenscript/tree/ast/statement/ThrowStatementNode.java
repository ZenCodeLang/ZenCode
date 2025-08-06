package org.openzen.zenscript.tree.ast.statement;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.expression.ExpressionNode;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

public class ThrowStatementNode  extends StatementNode {
	private final CodePosition position;
	private final ExpressionNode value;

	public ThrowStatementNode(CodePosition position, ExpressionNode value) {
		this.position = position;
		this.value = value;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitThrow(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public ExpressionNode value() {
		return value;
	}
}
