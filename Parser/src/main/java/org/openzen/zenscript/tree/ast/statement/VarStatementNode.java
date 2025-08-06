package org.openzen.zenscript.tree.ast.statement;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.expression.ExpressionNode;
import org.openzen.zenscript.tree.ast.misc.AnnotationNode;
import org.openzen.zenscript.tree.ast.misc.NameNode;
import org.openzen.zenscript.tree.ast.type.TypeNode;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

import java.util.List;

public class VarStatementNode  extends StatementNode {
	private final CodePosition position;
	private final List<AnnotationNode> annotations;
	private final NameNode name;
	private final TypeNode type;
	private final ExpressionNode initializer;
	private final boolean isFinal;

	public VarStatementNode(CodePosition position, List<AnnotationNode> annotations, NameNode name, TypeNode type, ExpressionNode initializer, boolean isFinal) {
		this.position = position;
		this.annotations = annotations;
		this.name = name;
		this.type = type;
		this.initializer = initializer;
		this.isFinal = isFinal;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitVar(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public NameNode name() {
		return name;
	}

	public List<AnnotationNode> annotations() {
		return annotations;
	}

	public TypeNode type() {
		return type;
	}

	public ExpressionNode initializer() {
		return initializer;
	}

	public boolean isFinal() {
		return isFinal;
	}
}
