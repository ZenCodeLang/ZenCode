package org.openzen.zenscript.tree.ast.misc;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.ASTNode;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

import java.util.List;

public class ModifiersNode implements ASTNode {
	private final CodePosition position;
	private final List<ModifierNode> modifiers;

	public ModifiersNode(CodePosition position, List<ModifierNode> modifiers) {
		this.position = position;
		this.modifiers = modifiers;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitModifiers(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public List<ModifierNode> modifiers() {
		return modifiers;
	}
}
