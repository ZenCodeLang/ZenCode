package org.openzen.zenscript.tree.ast.generic;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.ASTNode;
import org.openzen.zenscript.tree.ast.type.TypeNode;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

public class GenericBoundNode implements ASTNode {
	private final CodePosition position;
	private final TypeNode type;
	private final boolean isSuper;

	public GenericBoundNode(CodePosition position, TypeNode type, boolean isSuper) {
		this.position = position;
		this.type = type;
		this.isSuper = isSuper;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitGenericBound(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public TypeNode type() {
		return type;
	}

	public boolean isSuper() {
		return isSuper;
	}
}
