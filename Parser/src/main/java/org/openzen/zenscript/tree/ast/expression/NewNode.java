package org.openzen.zenscript.tree.ast.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.function.CallArgumentsNode;
import org.openzen.zenscript.tree.ast.type.TypeNode;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

public class NewNode extends ExpressionNode {
	private final CodePosition position;
	private final TypeNode type;
	private final CallArgumentsNode arguments;

	public NewNode(CodePosition position, TypeNode type, CallArgumentsNode arguments) {
		this.position = position;
		this.type = type;
		this.arguments = arguments;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitNew(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public TypeNode type() {
		return type;
	}

	public CallArgumentsNode arguments() {
		return arguments;
	}
}
