package org.openzen.zenscript.tree.ast.type;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.function.FunctionHeaderNode;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

public class FunctionTypeNode extends TypeNode {
	private final CodePosition position;
	private final FunctionHeaderNode header;

	public FunctionTypeNode(CodePosition position, FunctionHeaderNode header) {
		this.position = position;
		this.header = header;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitFunctionType(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public FunctionHeaderNode header() {
		return header;
	}
}
