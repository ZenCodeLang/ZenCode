package org.openzen.zenscript.tree.ast.misc;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.ASTNode;
import org.openzen.zenscript.tree.ast.type.TypeNode;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

public class SuperTypeNode implements ASTNode {
	private final CodePosition position;
	private final TypeNode type;

	public SuperTypeNode(CodePosition position, TypeNode type) {
		this.position = position;
		this.type = type;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitSuperType(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public TypeNode type() {
		return type;
	}
}
