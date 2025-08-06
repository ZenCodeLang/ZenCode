package org.openzen.zenscript.tree.ast.statement;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.ASTNode;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

import java.util.List;

public class BlockStatementNode extends StatementNode {
	private final CodePosition position;
	private final List<ASTNode> nodes;

	public BlockStatementNode(CodePosition position, List<ASTNode> nodes) {
		this.position = position;
		this.nodes = nodes;
	}

	public List<ASTNode> nodes() {
		return nodes;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitBlock(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}


}
