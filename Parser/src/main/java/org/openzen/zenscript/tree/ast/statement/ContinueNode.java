package org.openzen.zenscript.tree.ast.statement;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.misc.NameNode;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

public class ContinueNode extends StatementNode {
	private final CodePosition position;
	private final NameNode name;

	public ContinueNode(CodePosition position, NameNode name) {
		this.position = position;
		this.name = name;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitContinue(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public NameNode name() {
		return name;
	}
}
