package org.openzen.zenscript.tree.ast.misc;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.lexer.ZSToken;
import org.openzen.zenscript.lexer.ZSTokenType;
import org.openzen.zenscript.tree.ast.ASTNode;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;
import org.openzen.zenscript.tree.lexer.PositionedToken;

public class NameNode implements ASTNode {
	private final CodePosition position;
	private final PositionedToken<ZSTokenType, ZSToken> identifier;

	public NameNode(PositionedToken<ZSTokenType, ZSToken> identifier) {
		this.position = identifier.position();
		this.identifier = identifier;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitName(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public PositionedToken<ZSTokenType, ZSToken> identifier() {
		return identifier;
	}
}
