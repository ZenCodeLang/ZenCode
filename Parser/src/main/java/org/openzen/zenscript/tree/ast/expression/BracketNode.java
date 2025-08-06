package org.openzen.zenscript.tree.ast.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

import java.util.List;

public class BracketNode extends ExpressionNode {
	private final CodePosition position;
	private final List<ExpressionNode> expressions;

	public BracketNode(CodePosition position, List<ExpressionNode> expressions) {
		this.position = position;
		this.expressions = expressions;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitBracket(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public List<ExpressionNode> expressions() {
		return expressions;
	}
}
