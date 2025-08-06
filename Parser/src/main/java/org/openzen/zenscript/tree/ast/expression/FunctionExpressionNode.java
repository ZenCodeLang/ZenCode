package org.openzen.zenscript.tree.ast.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.ASTNode;
import org.openzen.zenscript.tree.ast.function.LambdaHeaderNode;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

public class FunctionExpressionNode extends ExpressionNode {
	private final CodePosition position;
	private final LambdaHeaderNode header;
	private final ASTNode body;

	public FunctionExpressionNode(CodePosition position, LambdaHeaderNode header, ASTNode body) {
		this.position = position;
		this.header = header;
		this.body = body;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitFunctionExpression(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public LambdaHeaderNode header() {
		return header;
	}

	public ASTNode body() {
		return body;
	}
}
