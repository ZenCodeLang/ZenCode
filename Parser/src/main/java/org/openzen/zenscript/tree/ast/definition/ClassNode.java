package org.openzen.zenscript.tree.ast.definition;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.definition.member.MembersNode;
import org.openzen.zenscript.tree.ast.generic.TypeParametersNode;
import org.openzen.zenscript.tree.ast.misc.ModifiersNode;
import org.openzen.zenscript.tree.ast.misc.NameNode;
import org.openzen.zenscript.tree.ast.misc.SuperTypeNode;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

public class ClassNode extends DefinitionNode {
	private final CodePosition position;
	private final NameNode name;
	private final MembersNode members;
	private final TypeParametersNode typeParameters;
	private final SuperTypeNode superType;

	public ClassNode(ModifiersNode modifiers, CodePosition position, NameNode name, MembersNode members, TypeParametersNode typeParameters, SuperTypeNode superType) {
		super(modifiers);
		this.position = position;
		this.name = name;
		this.members = members;
		this.typeParameters = typeParameters;
		this.superType = superType;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitClass(this, context);
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

	public SuperTypeNode superType() {
		return superType;
	}
}
