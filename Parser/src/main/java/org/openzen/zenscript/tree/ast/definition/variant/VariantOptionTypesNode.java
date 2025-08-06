package org.openzen.zenscript.tree.ast.definition.variant;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.ASTNode;
import org.openzen.zenscript.tree.ast.type.TypeNode;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

import java.util.List;

public class VariantOptionTypesNode implements ASTNode {
	private final CodePosition position;
	private final List<TypeNode> types;

	public VariantOptionTypesNode(CodePosition position, List<TypeNode> types) {
		this.position = position;
		this.types = types;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitVariantOptionTypes(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public List<TypeNode> types() {
		return types;
	}
}
