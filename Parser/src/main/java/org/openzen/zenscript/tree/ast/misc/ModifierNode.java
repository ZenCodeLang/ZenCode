package org.openzen.zenscript.tree.ast.misc;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.lexer.ZSToken;
import org.openzen.zenscript.lexer.ZSTokenType;
import org.openzen.zenscript.tree.ast.ASTNode;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;
import org.openzen.zenscript.tree.lexer.PositionedToken;

public class ModifierNode implements ASTNode {
	private final CodePosition position;
	private final PositionedToken<ZSTokenType, ZSToken> modifier;

	public ModifierNode(PositionedToken<ZSTokenType, ZSToken> modifier) {
		this.position = modifier.position();
		this.modifier = modifier;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitModifier(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public PositionedToken<ZSTokenType, ZSToken> modifier() {
		return modifier;
	}
}
