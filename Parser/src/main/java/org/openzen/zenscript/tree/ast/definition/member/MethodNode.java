package org.openzen.zenscript.tree.ast.definition.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.ASTNode;
import org.openzen.zenscript.tree.ast.function.FunctionHeaderNode;
import org.openzen.zenscript.tree.ast.misc.AnnotationNode;
import org.openzen.zenscript.tree.ast.misc.ModifiersNode;
import org.openzen.zenscript.tree.ast.misc.NameNode;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

import java.util.List;

public class MethodNode extends DefinitionMemberNode {
	private final CodePosition position;
	private final List<AnnotationNode> annotations;
	private final ModifiersNode modifiers;
	private final NameNode name;
	private final FunctionHeaderNode header;
	private final ASTNode body;

	public MethodNode(CodePosition position, List<AnnotationNode> annotations, ModifiersNode modifiers, NameNode name, FunctionHeaderNode header, ASTNode body) {
		this.position = position;
		this.annotations = annotations;
		this.modifiers = modifiers;
		this.name = name;
		this.header = header;
		this.body = body;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitMethod(this, context);
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

	public NameNode name() {
		return name;
	}

	public FunctionHeaderNode header() {
		return header;
	}

	public ASTNode body() {
		return body;
	}
}
