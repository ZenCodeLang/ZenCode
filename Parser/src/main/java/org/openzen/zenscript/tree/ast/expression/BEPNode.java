package org.openzen.zenscript.tree.ast.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.misc.NameNode;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

import java.util.List;

public class BEPNode extends ExpressionNode {
	private final CodePosition position;
	//TODO redo
	private final List<NameNode> names;

	public BEPNode(CodePosition position, List<NameNode> names) {
		this.position = position;
		this.names = names;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitBEP(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public List<NameNode> names() {
		return names;
	}
}
