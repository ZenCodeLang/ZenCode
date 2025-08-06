package org.openzen.zenscript.tree.ast.definition.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.ASTNode;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

public class AutosNode implements ASTNode {
	private final CodePosition position;
	private final AutoGetterNode autoGetter;
	private final AutoSetterNode autoSetter;

	public AutosNode(CodePosition position, AutoGetterNode autoGetter, AutoSetterNode autoSetter) {
		this.position = position;
		this.autoGetter = autoGetter;
		this.autoSetter = autoSetter;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitAutos(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public AutoGetterNode autoGetter() {
		return autoGetter;
	}

	public AutoSetterNode autoSetter() {
		return autoSetter;
	}
}
