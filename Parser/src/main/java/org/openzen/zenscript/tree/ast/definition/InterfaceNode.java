package org.openzen.zenscript.tree.ast.definition;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.definition.member.MembersNode;
import org.openzen.zenscript.tree.ast.generic.TypeParametersNode;
import org.openzen.zenscript.tree.ast.misc.ModifiersNode;
import org.openzen.zenscript.tree.ast.misc.NameNode;
import org.openzen.zenscript.tree.ast.misc.SuperTypesNode;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

public class InterfaceNode extends DefinitionNode {
	private final CodePosition position;
	private final NameNode name;
	private final MembersNode members;
	private final TypeParametersNode typeParameters;
	private final SuperTypesNode superTypes;

	public InterfaceNode(ModifiersNode modifiers, CodePosition position, NameNode name, MembersNode members, TypeParametersNode typeParameters, SuperTypesNode superTypes) {
		super(modifiers);
		this.position = position;
		this.name = name;
		this.members = members;
		this.typeParameters = typeParameters;
		this.superTypes = superTypes;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitInterface(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}


	public NameNode name() {
		return name;
	}

	public MembersNode members() {
		return members;
	}

	public TypeParametersNode typeParameters() {
		return typeParameters;
	}

	public SuperTypesNode superTypes() {
		return superTypes;
	}
}
