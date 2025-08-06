package org.openzen.zenscript.tree.ast.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

public class ConditionalNode extends ExpressionNode {
	private final CodePosition position;
	private final ExpressionNode condition;
	private final ExpressionNode ifThen;
	private final ExpressionNode ifElse;

	public ConditionalNode(CodePosition position, ExpressionNode condition, ExpressionNode ifThen, ExpressionNode ifElse) {
		this.position = position;
		this.condition = condition;
		this.ifThen = ifThen;
		this.ifElse = ifElse;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitConditional(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public ExpressionNode condition() {
		return condition;
	}

	public ExpressionNode ifThen() {
		return ifThen;
	}

	public ExpressionNode ifElse() {
		return ifElse;
	}
}
