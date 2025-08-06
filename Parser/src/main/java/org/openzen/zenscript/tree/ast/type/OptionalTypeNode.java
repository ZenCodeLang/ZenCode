package org.openzen.zenscript.tree.ast.type;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

public class OptionalTypeNode extends TypeNode {
	private final CodePosition position;
	private final TypeNode baseType;

	public OptionalTypeNode(CodePosition position, TypeNode baseType) {
		this.position = position;
		this.baseType = baseType;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitOptionalType(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public TypeNode baseType() {
		return baseType;
	}
}
