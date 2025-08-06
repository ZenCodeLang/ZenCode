package org.openzen.zenscript.tree.ast;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

import java.util.List;

public class FileNode implements ASTNode {
	private final CodePosition position;
	private final List<ASTNode> children;

	public FileNode(CodePosition position, List<ASTNode> children) {
		this.position = position;
		this.children = children;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitFile(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public List<ASTNode> children() {
		return children;
	}
}
