package org.openzen.zenscript.tree.ast.definition.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.ASTNode;
import org.openzen.zenscript.tree.ast.misc.ModifiersNode;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

public class AutoSetterNode implements ASTNode {
	private final CodePosition position;
	private final ModifiersNode modifiers;

	public AutoSetterNode(CodePosition position, ModifiersNode modifiers) {
		this.position = position;
		this.modifiers = modifiers;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitAutoSetter(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public ModifiersNode modifiers() {
		return modifiers;
	}
}
