package org.openzen.zenscript.tree.ast.misc;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.ASTNode;
import org.openzen.zenscript.tree.ast.function.CallArgumentsNode;
import org.openzen.zenscript.tree.ast.type.TypeNode;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

import java.util.ArrayList;
import java.util.List;

public class AnnotationNode implements ASTNode {
	private final CodePosition position;
	public static final List<AnnotationNode> NONE = new ArrayList<>();

	private final TypeNode type;
	private final CallArgumentsNode arguments;

	public AnnotationNode(CodePosition position, TypeNode type, CallArgumentsNode arguments) {
		this.position = position;
		this.type = type;
		this.arguments = arguments;
	}

	@Override
	public CodePosition position() {
		return position;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitAnnotation(this, context);
	}

	public TypeNode type() {
		return type;
	}

	public CallArgumentsNode arguments() {
		return arguments;
	}
}
