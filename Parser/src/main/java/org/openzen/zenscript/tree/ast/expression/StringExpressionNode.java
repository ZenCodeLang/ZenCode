package org.openzen.zenscript.tree.ast.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.lexer.ZSToken;
import org.openzen.zenscript.lexer.ZSTokenType;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;
import org.openzen.zenscript.tree.lexer.PositionedToken;

public class StringExpressionNode extends ExpressionNode {
	private final CodePosition position;
	private final PositionedToken<ZSTokenType, ZSToken> content;

	public StringExpressionNode(CodePosition position, PositionedToken<ZSTokenType, ZSToken> content) {
		this.position = position;
		this.content = content;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitStringExpression(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public PositionedToken<ZSTokenType, ZSToken> content() {
		return content;
	}
}
