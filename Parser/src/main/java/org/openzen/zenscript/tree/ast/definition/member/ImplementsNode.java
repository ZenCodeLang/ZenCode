package org.openzen.zenscript.tree.ast.definition.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.misc.AnnotationNode;
import org.openzen.zenscript.tree.ast.misc.ModifiersNode;
import org.openzen.zenscript.tree.ast.type.TypeNode;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

import java.util.List;

public class ImplementsNode extends DefinitionMemberNode {
	private final CodePosition position;
	private final List<AnnotationNode> annotations;
	private final ModifiersNode modifiers;
	private final TypeNode type;
	private final List<DefinitionMemberNode> members;

	public ImplementsNode(CodePosition position, List<AnnotationNode> annotations, ModifiersNode modifiers, TypeNode type, List<DefinitionMemberNode> members) {
		this.position = position;
		this.annotations = annotations;
		this.modifiers = modifiers;
		this.type = type;
		this.members = members;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitImplements(this, context);
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

	public TypeNode type() {
		return type;
	}

	public List<DefinitionMemberNode> members() {
		return members;
	}
}
