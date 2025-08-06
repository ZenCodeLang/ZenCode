package org.openzen.zenscript.tree.ast.function;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.ASTNode;
import org.openzen.zenscript.tree.ast.expression.ExpressionNode;
import org.openzen.zenscript.tree.ast.misc.NameNode;
import org.openzen.zenscript.tree.ast.type.TypeNode;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

public class ParameterNode implements ASTNode {
	private final CodePosition position;
	private final NameNode name;
	private final TypeNode type;
	private final ExpressionNode defaultValue;
	private final boolean variadic;

	public ParameterNode(CodePosition position, NameNode name, TypeNode type, ExpressionNode defaultValue, boolean variadic) {
		this.position = position;
		this.name = name;
		this.type = type;
		this.defaultValue = defaultValue;
		this.variadic = variadic;
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public NameNode name() {
		return name;
	}

	public TypeNode type() {
		return type;
	}

	public ExpressionNode defaultValue() {
		return defaultValue;
	}

	public boolean variadic() {
		return variadic;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitParameter(this, context);
	}


}
