package org.openzen.zenscript.tree.ast.definition;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.ASTNode;
import org.openzen.zenscript.tree.ast.function.FunctionHeaderNode;
import org.openzen.zenscript.tree.ast.misc.ModifiersNode;
import org.openzen.zenscript.tree.ast.misc.NameNode;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

public class FunctionNode extends DefinitionNode {
	private final CodePosition position;
	private final FunctionHeaderNode header;
	private final ASTNode body;
	private final NameNode name;

	public FunctionNode(ModifiersNode modifiers, CodePosition position, FunctionHeaderNode header, ASTNode body, NameNode name) {
		super(modifiers);
		this.position = position;
		this.header = header;
		this.body = body;
		this.name = name;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitFunction(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}


	public FunctionHeaderNode header() {
		return header;
	}

	public ASTNode body() {
		return body;
	}

	public NameNode name() {
		return name;
	}
}
