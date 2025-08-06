package org.openzen.zenscript.tree.ast.definition;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.definition.member.MembersNode;
import org.openzen.zenscript.tree.ast.generic.TypeParametersNode;
import org.openzen.zenscript.tree.ast.misc.ModifiersNode;
import org.openzen.zenscript.tree.ast.misc.NameNode;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

public class StructNode extends DefinitionNode {
	private final CodePosition position;
	private final NameNode name;
	private final TypeParametersNode typeParameters;
	private final MembersNode members;

	public StructNode(ModifiersNode modifiers, CodePosition position, NameNode name, TypeParametersNode typeParameters, MembersNode members) {
		super(modifiers);
		this.position = position;
		this.name = name;
		this.typeParameters = typeParameters;
		this.members = members;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitStruct(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}


	public NameNode name() {
		return name;
	}

	public TypeParametersNode typeParameters() {
		return typeParameters;
	}

	public MembersNode members() {
		return members;
	}
}
