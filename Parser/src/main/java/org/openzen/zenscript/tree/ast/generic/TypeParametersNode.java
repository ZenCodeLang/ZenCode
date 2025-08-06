package org.openzen.zenscript.tree.ast.generic;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.ASTNode;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

import java.util.List;

public class TypeParametersNode implements ASTNode {
	private final CodePosition position;
	private final List<TypeParameterNode> typeParameters;

	public TypeParametersNode(CodePosition position, List<TypeParameterNode> typeParameters) {
		this.position = position;
		this.typeParameters = typeParameters;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitTypeParameters(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public List<TypeParameterNode> typeParameters() {
		return typeParameters;
	}
}
