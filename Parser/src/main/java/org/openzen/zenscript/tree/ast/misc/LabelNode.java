package org.openzen.zenscript.tree.ast.misc;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.ASTNode;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

public class LabelNode implements ASTNode {
	private final CodePosition position;
	private final NameNode label;

	public LabelNode(CodePosition position, NameNode label) {
		this.position = position;
		this.label = label;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitLabel(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public NameNode label() {
		return label;
	}
}
