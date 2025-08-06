package org.openzen.zenscript.tree.ast.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.type.TypeNode;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

public class TypeExpressionNode extends ExpressionNode {
	private final CodePosition position;
	private final TypeNode type;

	public TypeExpressionNode(CodePosition position, TypeNode type) {
		this.position = position;
		this.type = type;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitTypeExpression(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public TypeNode type() {
		return type;
	}
}
