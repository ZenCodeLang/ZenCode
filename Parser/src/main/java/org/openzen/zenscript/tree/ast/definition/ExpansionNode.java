package org.openzen.zenscript.tree.ast.definition;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.definition.member.MembersNode;
import org.openzen.zenscript.tree.ast.generic.TypeParametersNode;
import org.openzen.zenscript.tree.ast.misc.ModifiersNode;
import org.openzen.zenscript.tree.ast.type.TypeNode;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

public class ExpansionNode extends DefinitionNode {
	private final CodePosition position;
	private final TypeNode type;
	private final MembersNode members;
	private final TypeParametersNode typeParameters;

	public ExpansionNode(ModifiersNode modifiers, CodePosition position, TypeNode type, MembersNode members, TypeParametersNode typeParameters) {
		super(modifiers);
		this.position = position;
		this.type = type;
		this.members = members;
		this.typeParameters = typeParameters;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitExpansion(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}


	public TypeNode type() {
		return type;
	}

	public MembersNode members() {
		return members;
	}

	public TypeParametersNode typeParameters() {
		return typeParameters;
	}
}
