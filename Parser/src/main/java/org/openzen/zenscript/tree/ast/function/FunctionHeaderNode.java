package org.openzen.zenscript.tree.ast.function;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.ASTNode;
import org.openzen.zenscript.tree.ast.generic.TypeParametersNode;
import org.openzen.zenscript.tree.ast.type.TypeNode;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

public class FunctionHeaderNode implements ASTNode {
	private final CodePosition position;
	private final TypeParametersNode genericParameters;
	private final ParametersNode parameters;

	private final TypeNode returnType;
	private final TypeNode thrownType;

	public FunctionHeaderNode(CodePosition position, TypeParametersNode genericParameters, ParametersNode parameters, TypeNode returnType, TypeNode thrownType) {
		this.position = position;
		this.genericParameters = genericParameters;
		this.parameters = parameters;
		this.returnType = returnType;
		this.thrownType = thrownType;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitFunctionHeader(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public TypeParametersNode genericParameters() {
		return genericParameters;
	}

	public ParametersNode parameters() {
		return parameters;
	}

	public TypeNode returnType() {
		return returnType;
	}

	public TypeNode thrownType() {
		return thrownType;
	}
}
