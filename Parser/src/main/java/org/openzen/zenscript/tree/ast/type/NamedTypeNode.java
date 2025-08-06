package org.openzen.zenscript.tree.ast.type;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.generic.TypeArgumentsNode;
import org.openzen.zenscript.tree.ast.misc.NameNode;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

import java.util.List;

public class NamedTypeNode extends TypeNode {
	private final CodePosition position;
	private final List<TypeName> names;

	public NamedTypeNode(CodePosition position, List<TypeName> names) {
		this.position = position;
		this.names = names;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitNamedType(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public List<TypeName> names() {
		return names;
	}

	public static class TypeName {
		private final NameNode name;
		private final TypeArgumentsNode typeArguments;

		public TypeName(NameNode name, TypeArgumentsNode typeArguments) {
			this.name = name;
			this.typeArguments = typeArguments;
		}

		public NameNode name() {
			return name;
		}

		public TypeArgumentsNode typeArguments() {
			return typeArguments;
		}
	}
}
