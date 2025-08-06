package org.openzen.zenscript.tree.ast.statement;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

public class EmptyFunctionBodyNode extends StatementNode {
	private final CodePosition position;

	public EmptyFunctionBodyNode(CodePosition position) {
		this.position = position;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitEmptyFunctionBody(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}


}
