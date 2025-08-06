package org.openzen.zenscript.tree.ast.definition;

import org.openzen.zenscript.tree.ast.ASTNode;
import org.openzen.zenscript.tree.ast.misc.ModifiersNode;

public abstract class DefinitionNode implements ASTNode {

	private final ModifiersNode modifiers;
	//TODO members?
	//TODO annotations

	public DefinitionNode(ModifiersNode modifiers) {
		this.modifiers = modifiers;
	}

	public ModifiersNode modifiers() {
		return modifiers;
	}


}
