package org.openzen.zenscript.tree.ast.definition.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.misc.AnnotationNode;
import org.openzen.zenscript.tree.ast.misc.ModifiersNode;
import org.openzen.zenscript.tree.ast.statement.StatementNode;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

import java.util.List;

public class StaticInitializerNode extends DefinitionMemberNode {
	private final CodePosition position;
	private final List<AnnotationNode> annotations;
	private final ModifiersNode modifiers;
	private final StatementNode body;

	public StaticInitializerNode(CodePosition position, List<AnnotationNode> annotations, ModifiersNode modifiers, StatementNode body) {
		this.position = position;
		this.annotations = annotations;
		this.modifiers = modifiers;
		this.body = body;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitStaticInitializer(this, context);
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

	public StatementNode body() {
		return body;
	}
}
