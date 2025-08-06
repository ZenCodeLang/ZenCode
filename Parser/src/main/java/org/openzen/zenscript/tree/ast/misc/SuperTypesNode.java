package org.openzen.zenscript.tree.ast.misc;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.ASTNode;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

import java.util.List;

public class SuperTypesNode implements ASTNode {
	private final CodePosition position;
	private final List<SuperTypeNode> superTypes;

	public SuperTypesNode(CodePosition position, List<SuperTypeNode> superTypes) {
		this.position = position;
		this.superTypes = superTypes;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitSuperTypes(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public List<SuperTypeNode> superTypes() {
		return superTypes;
	}
}
