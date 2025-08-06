package org.openzen.zenscript.tree.ast;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

public interface ASTNode {

	default <T extends ASTNode> T cast() {
		return (T) this;
	}

	<C, R> R accept(ASTVisitor<C, R> visitor, C context);

	CodePosition position();
}
