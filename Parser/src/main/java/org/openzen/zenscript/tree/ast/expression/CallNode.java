package org.openzen.zenscript.tree.ast.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.function.CallArgumentsNode;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

public class CallNode extends ExpressionNode {
	private final CodePosition position;
	private final ExpressionNode receiver;
	private final CallArgumentsNode arguments;

	public CallNode(CodePosition position, ExpressionNode receiver, CallArgumentsNode arguments) {
		this.position = position;
		this.receiver = receiver;
		this.arguments = arguments;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitCall(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public ExpressionNode receiver() {
		return receiver;
	}

	public CallArgumentsNode arguments() {
		return arguments;
	}
}
