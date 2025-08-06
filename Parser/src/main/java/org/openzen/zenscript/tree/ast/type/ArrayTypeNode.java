package org.openzen.zenscript.tree.ast.type;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

public class ArrayTypeNode extends TypeNode {
	//TODO this needs to handle dimensions
	private final CodePosition position;
	private final TypeNode elementType;
	private final int dimensions;

	public ArrayTypeNode(CodePosition position, TypeNode elementType, int dimensions) {
		this.position = position;
		this.elementType = elementType;
		this.dimensions = dimensions;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitArrayType(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public TypeNode elementType() {
		return elementType;
	}

	public int dimensions() {
		return dimensions;
	}
}
