package org.openzen.zenscript.tree.ast.type;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

public class RangeTypeNode extends TypeNode {
	private final CodePosition position;
	private final TypeNode from;
	private final TypeNode to;

	public RangeTypeNode(CodePosition position, TypeNode from, TypeNode to) {
		this.position = position;
		this.from = from;
		this.to = to;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitRangeType(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public TypeNode from() {
		return from;
	}

	public TypeNode to() {
		return to;
	}
}
