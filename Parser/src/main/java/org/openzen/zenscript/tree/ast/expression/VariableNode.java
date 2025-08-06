package org.openzen.zenscript.tree.ast.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.generic.TypeArgumentsNode;
import org.openzen.zenscript.tree.ast.misc.NameNode;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

public class VariableNode extends ExpressionNode {
	private final CodePosition position;
	private final NameNode name;
	private final TypeArgumentsNode typeArguments;

	public VariableNode(CodePosition position, NameNode name, TypeArgumentsNode typeArguments) {
		this.position = position;
		this.name = name;
		this.typeArguments = typeArguments;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitVariable(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public NameNode name() {
		return name;
	}

	public TypeArgumentsNode typeArguments() {
		return typeArguments;
	}
}
