package org.openzen.zenscript.tree.ast.definition.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.ASTNode;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

import java.util.List;

public class MembersNode implements ASTNode {
	private final CodePosition position;
	private final List<DefinitionMemberNode> members;

	public MembersNode(CodePosition position, List<DefinitionMemberNode> members) {
		this.position = position;
		this.members = members;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitMembers(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public List<DefinitionMemberNode> members() {
		return members;
	}
}
