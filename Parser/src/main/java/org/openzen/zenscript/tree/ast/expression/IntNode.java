package org.openzen.zenscript.tree.ast.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

public class IntNode extends ExpressionNode {
	private final CodePosition position;
	private final boolean negative;
	private final long value;
	private final String suffix;

	public IntNode(CodePosition position, boolean negative, long value, String suffix) {
		this.position = position;
		this.negative = negative;
		this.value = value;
		this.suffix = suffix;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitInt(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public boolean negative() {
		return negative;
	}

	public long value() {
		return value;
	}

	public String suffix() {
		return suffix;
	}
}
