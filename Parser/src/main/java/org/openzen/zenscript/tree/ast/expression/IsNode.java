package org.openzen.zenscript.tree.ast.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.type.TypeNode;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

public class IsNode extends ExpressionNode {
	private final CodePosition position;
	private final ExpressionNode expression;
	private final TypeNode type;

	public IsNode(CodePosition position, ExpressionNode expression, TypeNode type) {
		this.position = position;
		this.expression = expression;
		this.type = type;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitIs(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public ExpressionNode expression() {
		return expression;
	}

	public TypeNode type() {
		return type;
	}
}
