package org.openzen.zenscript.tree.ast.type;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.generic.TypeParameterNode;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

public class GenericMapTypeNode extends TypeNode {
	private final CodePosition position;
	private final TypeParameterNode key;
	private final TypeNode value;

	public GenericMapTypeNode(CodePosition position, TypeParameterNode key, TypeNode value) {
		this.position = position;
		this.key = key;
		this.value = value;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitGenericMapType(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public TypeParameterNode key() {
		return key;
	}

	public TypeNode value() {
		return value;
	}
}
