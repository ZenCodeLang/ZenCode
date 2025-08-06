package org.openzen.zenscript.tree.ast.definition.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.expression.ExpressionNode;
import org.openzen.zenscript.tree.ast.misc.ModifiersNode;
import org.openzen.zenscript.tree.ast.misc.NameNode;
import org.openzen.zenscript.tree.ast.type.TypeNode;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

public class FieldNode extends DefinitionMemberNode {
	private final CodePosition position;
	private final ModifiersNode modifiers;
	private final NameNode name;
	private final TypeNode type;
	private final boolean isFinal;
	private final AutosNode autos;
	private final ExpressionNode expression;

	public FieldNode(CodePosition position, ModifiersNode modifiers, NameNode name, TypeNode type, boolean isFinal, AutosNode autos, ExpressionNode expression) {
		this.position = position;
		this.modifiers = modifiers;
		this.name = name;
		this.type = type;
		this.isFinal = isFinal;
		this.autos = autos;
		this.expression = expression;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitField(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public ModifiersNode modifiers() {
		return modifiers;
	}

	public NameNode name() {
		return name;
	}

	public TypeNode type() {
		return type;
	}

	public boolean isFinal() {
		return isFinal;
	}

	public AutosNode autos() {
		return autos;
	}

	public ExpressionNode expression() {
		return expression;
	}
}
