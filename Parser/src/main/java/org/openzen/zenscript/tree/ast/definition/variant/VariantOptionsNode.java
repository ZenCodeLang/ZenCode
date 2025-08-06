package org.openzen.zenscript.tree.ast.definition.variant;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.ASTNode;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

import java.util.List;

public class VariantOptionsNode implements ASTNode {
	private final CodePosition position;
	private final List<VariantOptionNode> variants;

	public VariantOptionsNode(CodePosition position, List<VariantOptionNode> variants) {
		this.position = position;
		this.variants = variants;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitVariantOptionsNode(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public List<VariantOptionNode> variants() {
		return variants;
	}
}
