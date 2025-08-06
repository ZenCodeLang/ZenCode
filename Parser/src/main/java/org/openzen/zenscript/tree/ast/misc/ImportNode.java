package org.openzen.zenscript.tree.ast.misc;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.ASTNode;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

import java.util.List;

public class ImportNode implements ASTNode {
	private final CodePosition position;
	private boolean relative;
	private List<NameNode> names;
	private NameNode alias;

	public ImportNode(CodePosition position, boolean relative, List<NameNode> names, NameNode alias) {
		this.position = position;
		this.relative = relative;
		this.names = names;
		this.alias = alias;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitImport(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public boolean relative() {
		return relative;
	}

	public List<NameNode> names() {
		return names;
	}

	public NameNode alias() {
		return alias;
	}
}
