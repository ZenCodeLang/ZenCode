package org.openzen.zenscript.tree.ast.statement;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.ASTNode;
import org.openzen.zenscript.tree.ast.expression.ExpressionNode;
import org.openzen.zenscript.tree.ast.misc.LabelNode;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

public class WhileNode extends StatementNode {
	private final CodePosition position;
	private final LabelNode label;
	private final ExpressionNode condition;
	private final ASTNode body;

	public WhileNode(CodePosition position, LabelNode label, ExpressionNode condition, ASTNode body) {
		this.position = position;
		this.label = label;
		this.body = body;
		this.condition = condition;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitWhile(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public LabelNode label() {
		return label;
	}

	public ExpressionNode condition() {
		return condition;
	}

	public ASTNode body() {
		return body;
	}
}
