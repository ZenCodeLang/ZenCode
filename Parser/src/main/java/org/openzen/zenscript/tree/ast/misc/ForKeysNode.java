package org.openzen.zenscript.tree.ast.misc;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.ASTNode;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

import java.util.List;

public class ForKeysNode implements ASTNode {
	private final CodePosition position;
	private final List<NameNode> keys;

	public ForKeysNode(CodePosition position, List<NameNode> keys) {
		this.position = position;
		this.keys = keys;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitForKeys(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public List<NameNode> keys() {
		return keys;
	}
}
