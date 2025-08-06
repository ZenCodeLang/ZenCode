package org.openzen.zenscript.tree.ast.statement;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.ASTNode;
import org.openzen.zenscript.tree.ast.expression.ExpressionNode;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

public class LockNode extends StatementNode {
	private final CodePosition position;
	private final ExpressionNode object;
	private final ASTNode body;

	public LockNode(CodePosition position, ExpressionNode object, ASTNode body) {
		this.position = position;
		this.object = object;
		this.body = body;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitLock(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public ExpressionNode object() {
		return object;
	}

	public ASTNode body() {
		return body;
	}
}
