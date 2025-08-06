package org.openzen.zenscript.tree.ast.definition.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.tree.ast.ASTNode;
import org.openzen.zenscript.tree.ast.function.FunctionHeaderNode;
import org.openzen.zenscript.tree.ast.misc.AnnotationNode;
import org.openzen.zenscript.tree.ast.misc.ModifiersNode;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

import java.util.List;

public class DestructorNode extends DefinitionMemberNode {
	private final CodePosition position;
	private final List<AnnotationNode> annotations;
	private final ModifiersNode modifiers;
	private final OperatorType operator;
	private final FunctionHeaderNode header;
	private final ASTNode body;

	public DestructorNode(CodePosition position, List<AnnotationNode> annotations, ModifiersNode modifiers, OperatorType operator, FunctionHeaderNode header, ASTNode body) {
		this.position = position;
		this.annotations = annotations;
		this.modifiers = modifiers;
		this.operator = operator;
		this.header = header;
		this.body = body;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitDestructor(this, context);
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

	public OperatorType operator() {
		return operator;
	}

	public FunctionHeaderNode header() {
		return header;
	}

	public ASTNode body() {
		return body;
	}
}
