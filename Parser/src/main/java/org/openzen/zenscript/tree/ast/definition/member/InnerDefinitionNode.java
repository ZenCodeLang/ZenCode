package org.openzen.zenscript.tree.ast.definition.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.misc.AnnotationNode;
import org.openzen.zenscript.tree.ast.misc.ModifiersNode;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

import java.util.List;

public class InnerDefinitionNode extends DefinitionMemberNode {
	private final CodePosition position;
	private final List<AnnotationNode> annotations;
	private final ModifiersNode modifiers;
	private final DefinitionMemberNode definition;

	public InnerDefinitionNode(CodePosition position, List<AnnotationNode> annotations, ModifiersNode modifiers, DefinitionMemberNode definition) {
		this.position = position;
		this.annotations = annotations;
		this.modifiers = modifiers;
		this.definition = definition;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitInnerDefinition(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public List<AnnotationNode> annotations() {
		return annotations;
	}

	public ModifiersNode modifiers() {
		return modifiers;
	}

	public DefinitionMemberNode definition() {
		return definition;
	}
}
