package org.openzen.zenscript.tree.ast;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

public class ErrorNode implements ASTNode {

	private final CodePosition position;

	public ErrorNode(CodePosition position) {
		this.position = position;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitError(this, context);
	}

	@Override
	public CodePosition position() {
		return null;
	}
}
