package org.openzen.zenscript.tree.ast.type;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.lexer.ZSToken;
import org.openzen.zenscript.lexer.ZSTokenType;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;
import org.openzen.zenscript.tree.lexer.PositionedToken;

public class BasicTypeNode extends TypeNode {
	private final CodePosition position;
	private final PositionedToken<ZSTokenType, ZSToken> type;

	public BasicTypeNode(CodePosition position, PositionedToken<ZSTokenType, ZSToken> type) {
		this.position = position;
		this.type = type;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitBasicType(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public PositionedToken<ZSTokenType, ZSToken> type() {
		return type;
	}
}
