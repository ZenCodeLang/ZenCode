package org.openzen.zenscript.tree.ast.function;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.ASTNode;
import org.openzen.zenscript.tree.ast.expression.ExpressionNode;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

import java.util.List;

public class CallArgumentsNode implements ASTNode {
	private final CodePosition position;
	private final List<ExpressionNode> arguments;

	public CallArgumentsNode(CodePosition position, List<ExpressionNode> arguments) {
		this.position = position;
		this.arguments = arguments;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitCallArguments(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public List<ExpressionNode> arguments() {
		return arguments;
	}
}
