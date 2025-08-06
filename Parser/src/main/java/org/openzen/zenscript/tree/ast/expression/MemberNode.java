package org.openzen.zenscript.tree.ast.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.generic.TypeArgumentsNode;
import org.openzen.zenscript.tree.ast.misc.NameNode;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

public class MemberNode extends ExpressionNode {
	private final CodePosition position;
	private final ExpressionNode expression;
	private final NameNode member;
	private final TypeArgumentsNode genericParameters;

	public MemberNode(CodePosition position, ExpressionNode expression, NameNode member, TypeArgumentsNode genericParameters) {
		this.position = position;
		this.expression = expression;
		this.member = member;
		this.genericParameters = genericParameters;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitMember(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public ExpressionNode expression() {
		return expression;
	}

	public NameNode member() {
		return member;
	}

	public TypeArgumentsNode genericParameters() {
		return genericParameters;
	}
}
