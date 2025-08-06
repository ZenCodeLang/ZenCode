package org.openzen.zenscript.tree.ast.statement;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.expression.ExpressionNode;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

public class ReturnNode  extends StatementNode {
	private final CodePosition position;
	private final ExpressionNode expression;

	public ReturnNode(CodePosition position, ExpressionNode expression) {
		this.position = position;
		this.expression = expression;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitReturn(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public ExpressionNode expression() {
		return expression;
	}
}
