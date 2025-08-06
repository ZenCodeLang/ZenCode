package org.openzen.zenscript.tree.ast.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.type.TypeNode;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

public class CastNode extends ExpressionNode {
	private final CodePosition position;
	private final ExpressionNode value;
	private final TypeNode type;
	private final boolean optional;

	public CastNode(CodePosition position, ExpressionNode value, TypeNode type, boolean optional) {
		this.position = position;
		this.value = value;
		this.type = type;
		this.optional = optional;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitCast(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public ExpressionNode value() {
		return value;
	}

	public TypeNode type() {
		return type;
	}

	public boolean optional() {
		return optional;
	}
}
