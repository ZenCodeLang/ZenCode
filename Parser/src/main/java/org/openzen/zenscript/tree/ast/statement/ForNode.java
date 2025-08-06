package org.openzen.zenscript.tree.ast.statement;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.ASTNode;
import org.openzen.zenscript.tree.ast.expression.ExpressionNode;
import org.openzen.zenscript.tree.ast.misc.ForKeysNode;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

public class ForNode  extends StatementNode {
	private final CodePosition position;
	private final ForKeysNode keys;
	private final ExpressionNode value;
	private final ASTNode body;

	public ForNode(CodePosition position, ForKeysNode keys, ExpressionNode value, ASTNode body) {
		this.position = position;
		this.keys = keys;
		this.value = value;
		this.body = body;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitFor(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public ForKeysNode keys() {
		return keys;
	}

	public ExpressionNode value() {
		return value;
	}

	public ASTNode body() {
		return body;
	}
}
