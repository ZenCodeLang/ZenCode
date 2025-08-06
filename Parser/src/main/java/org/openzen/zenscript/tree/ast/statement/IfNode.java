package org.openzen.zenscript.tree.ast.statement;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.ASTNode;
import org.openzen.zenscript.tree.ast.expression.ExpressionNode;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

public class IfNode  extends StatementNode {
	private final CodePosition position;
	private final ExpressionNode condition;
	private final ASTNode body;

	private final ElseNode elseNode;

	public IfNode(CodePosition position, ExpressionNode condition, ASTNode body, ElseNode elseNode) {
		this.position = position;
		this.condition = condition;
		this.body = body;
		this.elseNode = elseNode;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitIf(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public ExpressionNode condition() {
		return condition;
	}

	public ASTNode body() {
		return body;
	}

	public ElseNode elseNode() {
		return elseNode;
	}
}
