package org.openzen.zenscript.tree.ast.definition;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.ASTNode;
import org.openzen.zenscript.tree.ast.definition.member.MembersNode;
import org.openzen.zenscript.tree.ast.expression.ExpressionNode;
import org.openzen.zenscript.tree.ast.function.CallArgumentsNode;
import org.openzen.zenscript.tree.ast.misc.ModifiersNode;
import org.openzen.zenscript.tree.ast.misc.NameNode;
import org.openzen.zenscript.tree.ast.type.TypeNode;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

import java.util.List;

public class EnumNode extends DefinitionNode {
	private final CodePosition position;
	private final NameNode name;
	private final List<EnumConstantNode> constants;
	private final TypeNode asType;
	private final MembersNode members;

	public EnumNode(ModifiersNode modifiers, CodePosition position, NameNode name, List<EnumConstantNode> constants, TypeNode asType, MembersNode members) {
		super(modifiers);
		this.position = position;
		this.name = name;
		this.constants = constants;
		this.asType = asType;
		this.members = members;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitEnum(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}


	public NameNode name() {
		return name;
	}

	public List<EnumConstantNode> constants() {
		return constants;
	}

	public TypeNode asType() {
		return asType;
	}

	public MembersNode members() {
		return members;
	}

	public static class EnumConstantNode implements ASTNode {
		private final NameNode name;
		private final CallArgumentsNode arguments;
		private final ExpressionNode value;

		public EnumConstantNode(NameNode name, CallArgumentsNode arguments, ExpressionNode value) {
			this.name = name;
			this.arguments = arguments;
			this.value = value;
		}

		@Override
		public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
			return visitor.visitEnumConstant(this, context);
		}

		@Override
		public CodePosition position() {
			return null;
		}

		public NameNode name() {
			return name;
		}

		public CallArgumentsNode arguments() {
			return arguments;
		}

		public ExpressionNode value() {
			return value;
		}
	}

}
