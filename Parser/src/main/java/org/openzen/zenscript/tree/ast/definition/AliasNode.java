package org.openzen.zenscript.tree.ast.definition;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.generic.TypeParametersNode;
import org.openzen.zenscript.tree.ast.misc.ModifiersNode;
import org.openzen.zenscript.tree.ast.misc.NameNode;
import org.openzen.zenscript.tree.ast.type.TypeNode;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

public class AliasNode extends DefinitionNode {
	private final CodePosition position;
	private final NameNode name;
	private final TypeParametersNode typeParameters;
	private final TypeNode type;

	public AliasNode(ModifiersNode modifiers, CodePosition position, NameNode name, TypeParametersNode typeParameters, TypeNode type) {
		super(modifiers);
		this.position = position;
		this.name = name;
		this.typeParameters = typeParameters;
		this.type = type;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitAlias(this, context);
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

	public TypeNode type() {
		return type;
	}
}
