package org.openzen.zenscript.tree.ast.definition.variant;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.ASTNode;
import org.openzen.zenscript.tree.ast.misc.NameNode;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

public class VariantOptionNode implements ASTNode {
	private final CodePosition position;
	private final NameNode name;
	private final VariantOptionTypesNode types;

	public VariantOptionNode(CodePosition position, NameNode name, VariantOptionTypesNode types) {
		this.position = position;
		this.name = name;
		this.types = types;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitVariantOption(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public NameNode name() {
		return name;
	}

	public VariantOptionTypesNode types() {
		return types;
	}
}
