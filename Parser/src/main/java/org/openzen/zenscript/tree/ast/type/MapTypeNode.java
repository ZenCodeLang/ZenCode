package org.openzen.zenscript.tree.ast.type;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

public class MapTypeNode extends TypeNode {
	private final CodePosition position;
	private final TypeNode key;
	private final TypeNode value;

	public MapTypeNode(CodePosition position, TypeNode key, TypeNode value) {
		this.position = position;
		this.key = key;
		this.value = value;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitMapType(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public TypeNode key() {
		return key;
	}

	public TypeNode value() {
		return value;
	}
}
