package org.openzen.zenscript.tree.ast.definition.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.ASTNode;
import org.openzen.zenscript.tree.ast.misc.AnnotationNode;
import org.openzen.zenscript.tree.ast.misc.ModifiersNode;
import org.openzen.zenscript.tree.ast.type.TypeNode;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

import java.util.List;

public class IteratorNode extends DefinitionMemberNode {
	private final CodePosition position;
	private final List<AnnotationNode> annotations;
	private final ModifiersNode modifiers;
	private final List<TypeNode> loopVariableTypes;
	private final ASTNode body;

	public IteratorNode(CodePosition position, List<AnnotationNode> annotations, ModifiersNode modifiers, List<TypeNode> loopVariableTypes, ASTNode body) {
		this.position = position;
		this.annotations = annotations;
		this.modifiers = modifiers;
		this.loopVariableTypes = loopVariableTypes;
		this.body = body;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitIterator(this, context);
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

	public List<TypeNode> loopVariableTypes() {
		return loopVariableTypes;
	}

	public ASTNode body() {
		return body;
	}
}
