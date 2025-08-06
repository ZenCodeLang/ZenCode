package org.openzen.zenscript.tree.ast.generic;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.ASTNode;
import org.openzen.zenscript.tree.ast.misc.NameNode;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

import java.util.List;

public class TypeParameterNode implements ASTNode {
	private final CodePosition position;
	private final NameNode name;
	private final List<GenericBoundNode> bounds;

	public TypeParameterNode(CodePosition position, NameNode name, List<GenericBoundNode> bounds) {
		this.position = position;
		this.name = name;
		this.bounds = bounds;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitTypeParameter(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public NameNode name() {
		return name;
	}

	public List<GenericBoundNode> bounds() {
		return bounds;
	}
}
