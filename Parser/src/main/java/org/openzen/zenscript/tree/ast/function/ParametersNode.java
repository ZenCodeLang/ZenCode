package org.openzen.zenscript.tree.ast.function;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.ASTNode;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

import java.util.List;

public class ParametersNode implements ASTNode {
	private final CodePosition position;
	private final List<ParameterNode> parameters;

	public ParametersNode(CodePosition position, List<ParameterNode> parameters) {
		this.position = position;
		this.parameters = parameters;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitParameters(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public List<ParameterNode> parameters() {
		return parameters;
	}
}
