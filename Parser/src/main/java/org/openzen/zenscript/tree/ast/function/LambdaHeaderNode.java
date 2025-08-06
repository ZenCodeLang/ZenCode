package org.openzen.zenscript.tree.ast.function;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.ASTNode;
import org.openzen.zenscript.tree.ast.type.TypeNode;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

public class LambdaHeaderNode implements ASTNode {
	private final CodePosition position;
	private final TypeNode returnType;
	private final ParametersNode parameters;

	public LambdaHeaderNode(CodePosition position, TypeNode returnType, ParametersNode parameters) {
		this.position = position;
		this.returnType = returnType;
		this.parameters = parameters;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitLambdaHeader(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public TypeNode returnType() {
		return returnType;
	}

	public ParametersNode parameters() {
		return parameters;
	}
}
