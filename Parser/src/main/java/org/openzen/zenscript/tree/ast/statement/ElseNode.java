package org.openzen.zenscript.tree.ast.statement;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.ASTNode;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

public class ElseNode  extends StatementNode {
	private final CodePosition position;
	private final ASTNode body;

	public ElseNode(CodePosition position, ASTNode body) {
		this.position = position;
		this.body = body;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitElse(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public ASTNode body() {
		return body;
	}
}
